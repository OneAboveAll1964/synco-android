# Synco Wire Protocol v1

Normative specification shared by `synco-android` and `synco-macos`. Every byte layout,
string literal, and JSON key here is part of the contract. Implementations must not
deviate. All integers on the wire are unsigned big-endian. All binary values embedded in
JSON are standard base64 with padding (RFC 4648 §4). All hashes rendered as lowercase hex.

Protocol version: `1`. Service name: `synco`.

---

## 1. Identity

On first launch a device generates a long-term **X25519** key pair (the *static* key,
`s` / `S`). The private key is stored in the platform secure store and never leaves the
device.

The **device id** (`did`) is derived from the static public key:

```
did = base32_nopad_lowercase( sha256(S)[0..10] )      # 10 bytes -> 16 chars
```

`base32_nopad_lowercase` is RFC 4648 base32 using the alphabet `abcdefghijklmnopqrstuvwxyz234567`
with no `=` padding. A `did` is therefore always exactly 16 characters and is safe for use
in DNS TXT records, filenames, and log lines.

The **fingerprint** shown to humans during pairing is derived from the same hash:

```
fp = uppercase_hex( sha256(S)[0..8] ) grouped in 4-char blocks
   = "A1B2-C3D4-E5F6-0718"
```

A device id is not a secret. A static public key is not a secret. Trust comes from the
user confirming the fingerprint once, per peer, on both devices.

---

## 2. Discovery

Discovery is mDNS / DNS-SD only. There is no broker, no cloud, no static IP anywhere in
the system. Because a peer is always resolved from its `did` at connect time, IP address
changes, DHCP lease renewals, Wi-Fi network switches, and sleep/wake cycles require no
user action.

- Service type: `_synco._tcp`
- Domain: `local.`
- Instance name: the `did` (16 chars). Never the human name — instance names must be
  stable and collision-free, and human names are neither.
- Port: ephemeral, chosen by the OS at listener start, re-advertised on every change.

TXT record keys:

| Key  | Value                                                        |
|------|--------------------------------------------------------------|
| `v`  | `1` — protocol version                                        |
| `did`| device id, 16 chars                                           |
| `dn` | display name, UTF-8, truncated to 63 bytes                    |
| `pl` | platform: `macos` \| `android`                                 |
| `fp` | fingerprint hex without separators, 16 chars                  |

Every device simultaneously **advertises** and **browses**. On discovering a peer whose
`did` is in the trusted set, and to which no live session exists, the device decides
whether to dial or wait:

```
initiator  <=>  self.did < peer.did      (byte-wise comparison of the ASCII did)
```

The device with the lexicographically smaller `did` dials; the other one listens. This
makes connection establishment deterministic and prevents the duplicate-session race that
symmetric dialing would otherwise produce. If both sides somehow end up with a session,
the one whose initiator role is wrong is closed.

Reconnect uses exponential backoff with full jitter: base 1s, factor 2, cap 30s, reset to
base on a successful handshake. A browse result that disappears cancels pending backoff
immediately — there is nothing to dial until mDNS sees the peer again.

---

## 3. Framing

Every frame, in both directions, for the entire life of the connection:

```
+--------+------------------+
| len:4  | payload: len     |
+--------+------------------+
```

`len` is a big-endian `uint32` counting only the payload bytes. Maximum `len` is
`1_048_576` (1 MiB); a peer announcing or sending more must be disconnected with
`frameTooLarge`. A `len` of `0` is invalid.

Frames are **plaintext** until the handshake completes (§4), and **encrypted** after
(§5). The transition is exact: the two `hello` frames and the two `auth` frames are
plaintext; every frame after a side has sent and verified `auth` is encrypted.

A decrypted (or plaintext) payload is:

```
+---------+------------------+
| kind:1  | body             |
+---------+------------------+
```

| kind   | meaning                                                      |
|--------|--------------------------------------------------------------|
| `0x01` | JSON control message — body is UTF-8 JSON, an object with `t` |
| `0x02` | Blob chunk — body is the layout below                         |
| `0x03` | Media frame — encoded remote-desktop video, body below (§11) |

Blob chunk body:

```
+------------------+-------------+------------------+
| transferId: 16   | offset: 8   | data: remainder  |
+------------------+-------------+------------------+
```

`transferId` is a raw 16-byte UUID (the same value that appears in JSON as its canonical
lowercase hyphenated string form). `offset` is a big-endian `uint64` byte offset into the
logical blob. Chunk `data` is at most `262_144` bytes (256 KiB). Chunks for a single
transfer are sent in ascending offset order and must be contiguous; a receiver that sees a
gap aborts the transfer. Chunks belonging to different transfers may interleave freely.

---

## 4. Handshake

Both sides hold their own static pair (`s`, `S`) and, for a trusted peer, the peer's
static public key `S_peer` learned at pairing time.

### 4.1 Exchange

Immediately on connection each side sends exactly one plaintext `hello`:

```json
{ "t": "hello", "v": 1, "did": "...", "dn": "...", "pl": "macos", "ePub": "<base64 32B>" }
```

`ePub` is a freshly generated per-connection ephemeral X25519 public key. It is never
reused, not even across reconnects to the same peer.

A side must not wait for the peer's `hello` before sending its own — both are sent
eagerly, so the handshake costs one round trip.

On receiving `hello`:

1. Reject with `versionMismatch` unless `v == 1`.
2. Reject with `selfConnection` if `did == self.did`.
3. If `did` is **not** in the trusted set, this is an unpaired peer: go to §6 (pairing)
   instead of continuing the handshake.
4. Reject with `unknownKey` if the stored `S_peer` for that `did` does not hash to that
   `did` — this catches a peer whose identity key was regenerated.

### 4.2 Key schedule

Let the *initiator* be the side determined by §2 (smaller `did`), the other the
*responder*. Define:

```
dh_ee = X25519(e_self,  E_peer)
dh_se = X25519(s_init,  E_resp)        # initiator's static, responder's ephemeral
dh_es = X25519(e_init,  S_resp)        # initiator's ephemeral, responder's static
```

Both roles can compute all three:

| value   | initiator computes    | responder computes    |
|---------|-----------------------|-----------------------|
| `dh_ee` | `X25519(e_i, E_r)`    | `X25519(e_r, E_i)`    |
| `dh_se` | `X25519(s_i, E_r)`    | `X25519(e_r, S_i)`    |
| `dh_es` | `X25519(e_i, S_r)`    | `X25519(s_r, E_i)`    |

Then:

```
ikm  = dh_ee || dh_se || dh_es                        # 96 bytes
salt = sha256( did_lo || did_hi )                     # 32 bytes
```

where `did_lo` is the lexicographically smaller of the two `did` strings and `did_hi` the
larger, both as raw ASCII — so both sides derive an identical salt without depending on
role.

```
okm      = HKDF-SHA256(ikm, salt, info = "synco-v1-session", length = 64)
k_i2r    = okm[0..32]      # key for frames sent by the initiator
k_r2i    = okm[32..64]     # key for frames sent by the responder
```

HKDF is RFC 5869 (extract-then-expand) with SHA-256. `info` is the ASCII bytes of
`synco-v1-session`, no terminator.

A side encrypts with the key for *its own* direction and decrypts with the other.

Any DH output that is all-zero bytes indicates a low-order point; abort with
`badHandshake`.

### 4.3 Confirmation

After deriving keys each side sends one plaintext `auth`:

```json
{ "t": "auth", "tag": "<base64 32B>" }
```

```
tag = HMAC-SHA256( key = k_own_direction,
                   msg = "synco-v1-confirm" || self.did )
```

with `"synco-v1-confirm"` as raw ASCII and `self.did` as raw ASCII, concatenated. Each
side recomputes the peer's expected tag using the peer's direction key and the peer's
`did` from `hello`, and compares in constant time. A mismatch aborts with `badAuth` and,
because it proves the peer does not hold the expected static key, must not silently
re-pair.

The session is established once a side has both sent its `auth` and verified the peer's.
Nonce counters (§5) start at 0 for both directions at this exact point.

---

## 5. Record encryption

AEAD: **ChaCha20-Poly1305** (RFC 8439), 32-byte key, 12-byte nonce, 16-byte tag appended
to the ciphertext. No associated data — the empty byte string.

Nonce for direction *d*, record *n* (`n` starting at 0, incremented once per frame sent):

```
nonce = 0x00 0x00 0x00 0x00 || uint64_be(n)
```

Each direction has an independent counter. A sender must tear down the connection before
`n` would reach `2^64 - 1`; in practice sessions are rekeyed by reconnecting long before
this. A receiver keeps its own expected counter and does not accept out-of-order or
replayed records — frames arrive in TCP order, so any deviation is an attack or a bug and
is fatal (`replay`).

The encrypted frame payload is `ciphertext || tag`, wrapped in the §3 length prefix. The
1-byte `kind` and body are *inside* the ciphertext.

---

## 6. Pairing

Pairing happens over the same TCP connection, before any session keys exist, and is
entirely plaintext. That is safe because nothing secret is exchanged: static public keys
are public, and trust is established by a human comparing a fingerprint out of band.

When a side receives `hello` from a `did` it does not trust, it replies:

```json
{ "t": "pairRequest", "did": "...", "dn": "...", "pl": "macos",
  "sPub": "<base64 32B>", "fp": "A1B2-C3D4-E5F6-0718", "tok": "<optional>" }
```

and, without waiting, surfaces the peer to its own user for approval. Each side shows the
peer's `dn` and `fp` and requires an explicit approve. `sPub` must hash to the claimed
`did` per §1 or the request is dropped as `didMismatch`.

`tok` is optional and carries a one-time QR pairing token. A device that is displaying a
QR pairing code treats a `pairRequest` whose `tok` equals the displayed token as already
approved by its user — showing the code is the consent — and consumes the token. Devices
never persist tokens and ignore an unknown or absent `tok`, falling back to the normal
approval flow. The QR payload itself is a URI:

```
synco://pair?v=1&did=<deviceId>&key=<base64url sPub>&fp=<fingerprint>&name=<urlencoded>&port=<tcp port>&hosts=<ip[,ip...]>&tok=<token>
```

The scanning device verifies that `key` hashes to `did` and to `fp` per §1, stores the peer
as trusted, and dials `hosts:port` directly, which also covers networks where mDNS is
blocked.

Once its user approves, a side sends:

```json
{ "t": "pairResponse", "accepted": true, "did": "...", "sPub": "<base64 32B>" }
```

A side that receives `accepted: true` **and** has itself approved persists the peer
(`did`, `sPub`, `dn`, `pl`, first-paired timestamp) to its trusted set, closes the
connection, and reconnects. The fresh connection runs a normal §4 handshake, now with both
static keys known. Splitting pairing from the authenticated session keeps §4 free of
conditional branches.

`accepted: false` closes the connection and marks the peer rejected; the local side must
not re-prompt until the user clears the rejection. A pairing exchange that receives no
response within 120s times out.

---

## 7. Session messages

All of these are `kind = 0x01` JSON, encrypted, and carry `t`. Unknown `t` values are
ignored, not fatal — this is the forward-compatibility hinge.

### 7.1 `caps`

Sent by both sides immediately after the session is established, and again whenever the
local user changes a toggle. Advertising capability lets each UI explain *why* something
did not sync instead of silently dropping it.

```json
{ "t": "caps",
  "accepts": { "text": true, "image": true, "file": true },
  "sends":   { "text": true, "image": true, "file": false },
  "maxBlob": 1073741824,
  "adbShizuku": false }
```

`accepts` is what this device will apply to its own clipboard (its receive toggles).
`sends` is what it will originate (its send toggles). `maxBlob` is the largest single blob
it will accept, in bytes; `9223372036854775807` means no limit.

`adbShizuku` is optional and defaults to `false` when absent. It is `true` only when this
device can run the Shizuku start command over adb on behalf of a paired peer and its user
has turned that on. See §7.8.

### 7.2 `ping` / `pong`

```json
{ "t": "ping", "seq": 12 }
{ "t": "pong", "seq": 12 }
```

A side sends `ping` every 15s of write idleness. If 45s pass with no frame of any kind
received, the connection is considered dead and closed — a TCP socket on a device that
walked out of Wi-Fi range can otherwise stay open for minutes.

### 7.3 `clip`

One clipboard event, carrying every representation the source could offer:

```json
{ "t": "clip",
  "id": "3f2a...-...",
  "ts": 1730300000000,
  "origin": "abcdefghij234567",
  "hash": "<sha256 hex of the canonical form>",
  "reps": [ { "k": "text", "text": "hello" } ] }
```

`id` is a UUID string, unique per clipboard event. `ts` is Unix milliseconds at the
source. `origin` is the `did` where the copy physically happened — preserved across
forwarding so a clip can never loop back to its origin.

`hash` is the loop-suppression key, computed over a canonical form so that both platforms
agree on it: see §8.

`reps` is ordered **most specific first**. A receiver applies as many representations as
its platform supports onto a single clipboard write, so pasting into a rich editor keeps
formatting while pasting into a terminal degrades to plain text.

Representation objects:

| `k`     | fields                                                         |
|---------|----------------------------------------------------------------|
| `text`  | `text` — plain UTF-8                                            |
| `html`  | `html` — HTML fragment                                          |
| `rtf`   | `b64` — RTF bytes                                               |
| `url`   | `url`, optional `title`                                         |
| `image` | `mime`, `name`, `size`, `sha256`, `transferId`                  |
| `file`  | `mime`, `name`, `size`, `sha256`, `transferId`, optional `rel`  |

`text`, `html`, `rtf`, and `url` are inline and only used when the encoded
representation is under `65536` bytes; anything larger is demoted to a `file`
representation so it streams instead of occupying a control frame. `image` and `file`
always stream — the `clip` frame only announces them.

`rel` is a forward-slash relative path used when a copy contained a directory, so the
receiver can rebuild the tree.

### 7.4 Transfers

```json
{ "t": "transferStart", "transferId": "...", "clipId": "...",
  "name": "shot.png", "mime": "image/png", "size": 91234,
  "sha256": "<hex>" }

{ "t": "transferEnd",   "transferId": "...", "ok": true }
{ "t": "transferAbort", "transferId": "...", "reason": "tooLarge" }
```

Order is strict: `clip` → `transferStart` → `0x02` chunks in ascending offset →
`transferEnd`. A receiver that has all bytes verifies SHA-256 before touching the
clipboard; a mismatch is an abort, never a partial paste.

A receiver may reply `transferAbort` at any point (over budget, disk full, user turned the
toggle off mid-flight); the sender must stop sending chunks for that `transferId`
immediately.

A `clip` whose blob representations have not all completed is **not** applied to the
clipboard. Either the whole event lands or none of it does — a half-written clipboard is
worse than a missed sync.

### 7.6 `policy`

Direction and per-type choices are a property of the *pair*, not of one device, so a change
made on either side is mirrored to the other.

```json
{ "t": "policy",
  "rev": 1785450000000,
  "send": { "text": true, "image": true, "file": true },
  "recv": { "text": true, "image": true, "file": false },
  "paused": false,
  "maxBlob": 1073741824 }
```

`rev` is the Unix millisecond timestamp of the local edit that produced this policy. It is
opaque and only ever compared, never interpreted.

Each side sends `policy` once the session is established, and again whenever its user
changes a toggle for that peer. On receiving one:

- If `rev` is not greater than the revision already stored for that peer, ignore it. A
  device that has been offline therefore cannot undo a newer change made elsewhere.
- Otherwise adopt it **mirrored**: the sender's `send` becomes the receiver's `recv`, and
  the sender's `recv` becomes the receiver's `send`. "Do not send me images" on one device
  is the same fact as "do not send images to that device" on the other.
- `paused` and `maxBlob` are adopted as-is; they are properties of the pair.
- Store the received `rev` unchanged, so the two devices converge on one revision.

If both devices are edited while disconnected, the later edit wins on reconnect. Equal
revisions are resolved in favour of the lexicographically smaller `did`, so both sides pick
the same winner without another round trip.

### 7.5 `ack`

```json
{ "t": "ack", "id": "<clip id>", "applied": true, "reason": null }
```

Sent by a receiver once a `clip` has been fully resolved. `applied: false` with a `reason`
(`typeDisabled`, `receiveDisabled`, `tooLarge`, `hashMismatch`, `userCancelled`) is what
lets the sending UI show "not synced — Mac isn't accepting files" rather than nothing.

---

### 7.7 `transferProgress`

A sender knows how many bytes it has written, but not how many the other device has
actually taken. For a large blob the two diverge, so the receiver reports its own count and
both devices can show the same progress.

```json
{ "t": "transferProgress", "transferId": "...", "received": 8388608 }
```

Sent by the receiver while a blob is arriving, at most once every 500 ms per transfer, and
never after `transferEnd`. `received` is the number of contiguous bytes written so far.

This message is advisory. A device that never sends it is not at fault, and a sender that
ignores it behaves exactly as before, so an implementation may skip it entirely.

### 7.8 `shizukuStart` / `shizukuStartResult`

Shizuku gives the Android side a reliable clipboard read, but it has to be started again
after every reboot and starting it needs a computer with adb. When a phone is plugged into
a paired desktop that advertises `adbShizuku`, it can ask that desktop to run the start
command instead of the user reaching for a terminal.

```json
{ "t": "shizukuStart" }
```

```json
{ "t": "shizukuStartResult", "started": true }
{ "t": "shizukuStartResult", "started": false, "reason": "adbMissing" }
```

The request carries no fields. A receiver runs exactly one fixed command and nothing else:

```
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```

Nothing from the message reaches that command line, so a peer cannot choose what runs. A
receiver must refuse the request unless the sender is a paired peer *and* its user has
turned the option on; it answers `notAllowed` rather than staying silent.

`reason` is absent when `started` is `true`. Otherwise it is one of `notAllowed`,
`adbMissing` (no adb on that machine), `noDevice` (adb cannot see the phone), or `failed`.

Both messages are optional. A device that never sends `shizukuStart` and one that answers
every request with `notAllowed` are both conformant.

## 8. Canonical hash and loop suppression

Two devices writing to each other's clipboards is a feedback loop unless suppression is
exact. Three mechanisms combine:

1. **Origin check.** A device never applies a `clip` whose `origin` equals its own `did`.
2. **Canonical hash.** `hash` is computed identically on both platforms:

   ```
   canonical = for each rep in reps, in order:
                 utf8(k) || 0x1F || repBytes || 0x1E
   hash      = sha256_hex(canonical)
   ```

   where `repBytes` is, per kind, and `esc(...)` is the escape below:

   | `k`     | `repBytes`                                        |
   |---------|---------------------------------------------------|
   | `text`  | `esc(utf8(text))`                                 |
   | `html`  | `esc(utf8(html))`                                 |
   | `rtf`   | `esc(raw RTF bytes)`                              |
   | `url`   | `esc(utf8(url))`                                  |
   | `image` | `esc(utf8(sha256 hex))`                           |
   | `file`  | `esc(utf8(name))` `0x1F` `esc(utf8(sha256 hex))`  |

   `0x1F` is the unit separator and `0x1E` the record separator. Content is not trusted to
   be free of them: clipboard bytes may contain either, and a plain concatenation would let
   a single `text` rep whose content embeds `0x1E`/`0x1F` reproduce the exact canonical
   bytes of a multi-rep clip. `esc` therefore byte-stuffs every value that originates from
   content, using `0x1D` as the escape:

   ```
   esc(b) = for each byte x in b:
              if x in { 0x1D, 0x1E, 0x1F }: emit 0x1D
              emit x
   ```

   Only the separators emitted by the canonical form itself are left unescaped, so the
   framing is unambiguous and no rep boundary can be forged by content. Escaping is the
   identity on any value containing none of those three bytes, so hashes of ordinary
   clipboard content are unchanged.

3. **Suppression window.** Before writing a received clip locally, a device records
   `hash` in a bounded set (last 32 hashes, entries expiring after 10s). Its own clipboard
   watcher drops any local change whose canonical hash is in that set. The window exists
   because platform clipboard notifications are asynchronous — the watcher will observe the
   write we just made, and it must recognise it as ours.

Additionally a device drops a local clipboard change whose hash equals the hash of the
last clip it sent, regardless of window, so idle polling never re-sends.

---

## 9. Direction and type policy

Sync direction is enforced **on both sides**, independently, on every clip. A single
toggle is never trusted to a single device.

Per peer, each device stores:

```
send.text  send.image  send.file          # may this device originate to that peer
recv.text  recv.image  recv.file          # may that peer's clips land here
```

Sender-side: a clip is not sent at all if every representation it contains is disabled by
`send.*`. Representations individually disabled are stripped before sending; if stripping
empties `reps`, nothing is sent.

Receiver-side: a clip whose surviving representations are all disabled by `recv.*` is
answered with `ack{applied:false, reason:"typeDisabled"}` and dropped.

"Android → Mac only" is therefore Android `send.* = true` + Mac `recv.* = true` with Mac
`send.* = false` + Android `recv.* = false`, and either device alone refusing is enough to
stop the flow. `caps` (§7.1) keeps both UIs honest about the effective state.

A global `paused` switch on either device suppresses both directions without discarding
per-type configuration.

---

## 10. Limits and errors

| Constant                | Value        |
|-------------------------|--------------|
| max frame payload       | 1 MiB        |
| max blob chunk          | 256 KiB      |
| inline rep threshold    | 64 KiB       |
| default max blob        | 1 GiB        |
| ping interval           | 15 s         |
| read timeout            | 45 s         |
| pair timeout            | 120 s        |
| reconnect backoff       | 1s → 30s     |
| suppression window      | 10 s, 32 entries |

Close reasons, sent best-effort as `{"t":"bye","reason":"..."}` before the socket closes:
`versionMismatch`, `selfConnection`, `unknownKey`, `didMismatch`, `badHandshake`,
`badAuth`, `frameTooLarge`, `replay`, `timeout`, `duplicateSession`, `shutdown`,
`unpaired`.

---

## 11. Remote control

Remote control lets a paired phone view and drive the Mac's screen over the same
encrypted connection. It is off until the phone requests it, and the Mac never grants it
without the operating-system permissions it needs (screen recording to capture, and
accessibility to inject input). Only the phone may drive the Mac; the Mac is always the
host.

### 11.1 Session lifecycle

The phone opens a session:

```json
{ "t": "remoteStart", "maxWidth": 1600, "maxHeight": 2560, "fps": 60, "input": true }
```

`maxWidth`/`maxHeight` bound the streamed resolution (the Mac scales its display down to
fit while keeping aspect ratio); `fps` is the desired frame rate the Mac may cap; `input`
asks for input injection as well as viewing.

The Mac replies with exactly one of:

```json
{ "t": "remoteAccept", "codec": "h264", "width": 1600, "height": 1000, "fps": 60,
  "input": true }
```

```json
{ "t": "remoteReject", "reason": "screenPermission" }
```

`remoteReject` reasons: `screenPermission` (screen recording not granted),
`inputPermission` (accessibility not granted — sent only when the phone asked for input
and viewing is still offered via a follow-up `remoteAccept` with `"input": false`),
`busy` (a session is already active), `unsupported` (the host build has no remote
support). On `remoteAccept` the Mac begins sending media frames (§11.3) immediately.

Either side ends the session:

```json
{ "t": "remoteStop" }
```

The Mac also emits `remoteStop` if screen capture stops (display asleep, permission
revoked). A `bye` or a dropped connection ends any active remote session implicitly.

### 11.2 Input

While a session with `"input": true` is active, the phone sends input in batches so a
gesture is one frame rather than many:

```json
{ "t": "remoteInput", "ev": [ { "k": "pa", "x": 0.42, "y": 0.31 }, { "k": "b", "btn": "left", "down": true } ] }
```

Each event `k` is one of:

| `k`  | meaning            | fields                                             |
|------|--------------------|----------------------------------------------------|
| `pa` | pointer, absolute  | `x`,`y` in `0..1` of the streamed frame            |
| `pm` | pointer, relative  | `dx`,`dy` in streamed-frame pixels                 |
| `b`  | button             | `btn` = `left`\|`right`\|`middle`, `down` bool     |
| `s`  | scroll / wheel     | `dx`,`dy` (positive `dy` scrolls content up)       |
| `z`  | magnify (pinch)    | `scale` — incremental factor, `1.0` is no change   |
| `k`  | key                | `code` (§11.4), `down` bool, `mods` bitmask        |
| `txt`| text              | `s` — a UTF-8 string typed as-is                    |

The Mac clamps `pa` to `0..1`, maps into current display coordinates, and injects each
event in array order. Unknown `k` values are skipped, never fatal.

### 11.3 Media frames

Media frames are frame kind `0x03` (§3). One encoded picture may be split across several
media frames when it exceeds the frame cap; fragments of one picture share `seq` and
arrive in order.

```
+-----------+---------+----------+-------------+------------------+
| stream:1  | flags:1 | seq:4    | pts:8       | data: remainder  |
+-----------+---------+----------+-------------+------------------+
```

`stream` is the media stream id (`0` = primary display). `seq` is a big-endian `uint32`
picture sequence number. `pts` is a big-endian `uint64` presentation time in microseconds.
`flags` is a bitmask:

| bit    | meaning                                                        |
|--------|----------------------------------------------------------------|
| `0x01` | keyframe (IDR)                                                 |
| `0x02` | codec config (H.264 SPS/PPS, sent before the first keyframe)   |
| `0x04` | last fragment of this `seq` (the decoder may now assemble it)  |

`data` is Annex-B H.264 (start-code delimited NAL units). The receiver concatenates all
fragments of a `seq` in order until it sees `0x04`, then feeds the assembled access unit
to its decoder. Config frames (`0x02`) carry SPS/PPS and are resent with every keyframe so
a phone that joins mid-stream can start on the next keyframe.

### 11.4 Key codes

`code` is the USB HID usage id of the key (HID Usage Page 0x07, Keyboard/Keypad), the same
namespace both platforms can map to and from without a shared table. `mods` is a bitmask:
`0x01` shift, `0x02` control, `0x04` alt/option, `0x08` meta/command.
