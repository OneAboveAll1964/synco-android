# Contributing to Synco (Android)

Synco is one product built from two repositories. `synco-android` is this one;
`synco-macos` is the Mac half. They share `PROTOCOL.md`, which is normative and identical
in both trees. Everything here assumes you have read it.

## Module boundaries

Dependencies point one way only. Nothing in `core` knows about `sync`, nothing below `app`
knows about the UI, and no `core` module depends on a sibling `core` module except through
the ones listed below. If a change needs an arrow that does not already exist, that is a
design discussion, not a build-file edit.

### `:core:protocol` — the wire, and nothing else

A Kotlin JVM module with no Android dependency, deliberately. It holds the types and
constants that must be provably identical to the Mac: `DeviceId`, `Fingerprint`,
`Platform`, frame and blob-chunk codecs, every JSON message class, the canonical clip hash,
base32/base64/hex encoding, `ProtocolConstants`, `DiscoveryConstants`, `HandshakeConstants`,
and `SyncoError` with its close reasons.

Belongs here: anything `PROTOCOL.md` names.
Does not belong here: I/O, sockets, files, coroutine plumbing beyond `Flow` types,
anything that would need `android.*`.

Keeping it Android-free is the point. It is what makes the protocol testable off-device and
what stops platform behaviour from leaking into the contract.

### `:core:crypto` — key material and session ciphers

JVM module over Bouncy Castle. Static and ephemeral X25519 keys, the §4.2 key schedule,
HKDF, the HMAC confirmation tag, the ChaCha20-Poly1305 record ciphers and their nonce
counters, and `HandshakeRole`.

Belongs here: primitives and the derivation described in §4 and §5.
Does not belong here: where keys are stored (that is `:core:storage`), when a handshake
runs (that is `:core:transport`).

### `:core:transport` — one connection at a time

JVM module over Ktor sockets. Length-prefixed framing, plaintext-to-encrypted upgrade, the
`hello`/`auth` exchange, the pairing exchange, heartbeat, read-timeout watchdog, close
reasons, and reconnect backoff. `PeerSession` owns the lifetime of exactly one TCP
connection and emits `SessionEvent`s.

Belongs here: anything true of a single connection.
Does not belong here: which peers exist, whether to dial, policy about what may be sent.

### `:core:discovery` — mDNS

Android library over `NsdManager`. Advertising `_synco._tcp`, browsing, resolving, TXT
encode/decode, the registry of currently visible peers, and the connectivity callback.

Belongs here: everything about *finding* a peer.
Does not belong here: connecting to one.

### `:core:clipboard` — the platform clipboard

Android library over `ClipboardManager`. Reading the primary clip into `ClipRep`s, writing
`ClipRep`s back as a single `ClipData`, the change monitor, and the loop-suppression window.

Belongs here: translation between Android clipboard types and protocol representations.
Does not belong here: whether a clip is allowed to be sent.

### `:core:transfer` — blobs

Android library. Staging files, chunking, SHA-256 verification, safe file naming and
collision handling, `FileProvider` paths, and progress reporting.

Belongs here: bytes on disk and their integrity.
Does not belong here: the `clip` message that announces them.

### `:core:storage` — persistence

Android library. The encrypted identity key store, DataStore-backed settings, the
trusted-peer set, and `SyncPolicy`, which is the single place that answers "may this
representation be sent / accepted".

Belongs here: anything that survives a process restart.
Does not belong here: anything that needs a live connection.

### `:sync` — the engine

Android library and the only module the app and the service talk to. Peer registry,
dial-versus-wait decision, per-peer clip routing, pairing coordination, transfer
orchestration, and the observable `SyncState`. `SyncoGraph.create(context, scope)` builds
the whole object graph; `SyncCommands` is the only way to change anything from outside.

Belongs here: policy and coordination across peers.
Does not belong here: Android component lifecycles, notifications, UI state shaping.

### `:service` — Android components

Android library. `SyncoForegroundService`, its notification and actions, `BootReceiver`,
`SyncoTileService`, the wake and multicast locks, and `SyncoAccessibilityService`.

The accessibility service is deliberately minimal and must stay that way. It reacts to the
*fact* of a window event by asking the clipboard monitor to re-read, and it flips a
connected flag. It declares `canRetrieveWindowContent="false"` and
`canRequestFilterKeyEvents="false"`. Do not add capabilities to it, do not inspect event
contents, and do not call `getRootInActiveWindow()`. Users are being asked for one of the
most invasive permissions Android has; the honesty of the README depends on this file
staying trivial and auditable.

### `:app` — UI only

Compose screens, `HomeViewModel`, the state mapper, and the permission flow. It reads
`SyncoGraph.state` and calls `SyncoGraph.commands`. It contains no networking, no
persistence, and no protocol knowledge beyond the types it displays.

### `build-logic`

An included build holding the convention plugins (`synco.android.application`,
`synco.android.library`, `synco.android.compose`, `synco.jvm.library`,
`synco.serialization`) and `BuildConstants`. SDK levels, Java version, and compiler
arguments are set there once, not repeated per module. A module's own `build.gradle.kts`
should be a plugin list, a namespace, and a dependency list — nothing more.

## Conventions

### No comments

Not one. No line comments, no block comments, no KDoc, no file headers, no `MARK:`
sections, no `TODO`/`FIXME` markers. This applies to Kotlin, XML, Gradle scripts, and shell
scripts alike.

Express intent through naming instead. A comment explaining what a block does is a signal
that the block wants to be a named private function; a comment explaining what a magic
number means is a signal that it wants to be a named constant. `RepOrder.rank`,
`SafeFileName.unique`, `DialRule.intentOf` and `SuppressionWindow.consume` each say what
they are without a word of prose.

The exceptions are the documentation files — this one, `README.md`, and `PROTOCOL.md` —
which are prose by definition.

### Small files, one type each

Target under 120 lines per file, hard ceiling 200. One primary type per file, named after
the file. When a file grows past the ceiling, split it along a real seam into a new file in
the same package rather than shaving lines off.

The seam matters more than the count. `PeerSession` delegates to `SessionHandshake`,
`PairingExchange`, `SessionReceiveLoop`, `SessionHeartbeat`, `ReadTimeoutWatchdog` and
`SessionTermination` because those are genuinely separate concerns, not because the file
was long. Splitting a class into `FooPart1`/`FooPart2` is worse than leaving it long.

### Components, not god objects

Each type has one responsibility and depends on an interface where a sibling could
reasonably be swapped: `ClipboardSource`, `ClipboardSink`, `TransferGateway`,
`DiscoveryService`, `SettingsStore`, `TrustedPeerStore`, `PairingApproval`, `TrustedPeers`.
The tests substitute fakes for exactly these. New collaborators should follow the same
shape.

### No placeholders

No stubbed bodies, no `TODO()`, no sample or demo code. Everything committed is real and
working.

### Style

Kotlin official code style, four-space indent, trailing commas. Prefer `runCatching` for
recovering from platform APIs that throw, and let `CancellationException` propagate — see
the `catch (cancellation: CancellationException) { throw cancellation }` pattern used
throughout `:sync` and `:core:discovery`.

## Testing

```sh
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
./gradlew test
```

Unit tests live beside the module they cover. Two suites matter more than the rest, because
they are the cross-repository contract made executable:

- `core/crypto/src/test/kotlin/app/synco/crypto/SharedHandshakeVectorTest.kt` with its
  vectors in `SharedHandshakeVectors.kt` — fixed static and ephemeral private keys, the
  device ids they derive to, the two directional session keys, and both confirmation tags.
- `core/protocol/src/test/kotlin/app/synco/protocol/clip/SharedClipHashVectorTest.kt` —
  canonical byte lengths and digests for representation lists, including the separator and
  escape-byte cases that prove content cannot forge a representation boundary.

The Mac repository has the same vectors. If you change either side's derivation and only
one suite is updated, the two apps will handshake and then fail authentication — the vectors
exist so that failure happens in CI instead of on a user's desk.

## Adding a new message type to the protocol

Unknown `t` values are ignored rather than fatal (§7). `EnvelopeCodec` decodes an unknown
discriminator into `UnknownMessage` and the session carries on. That is the forward
compatibility hinge, and it is what makes a staged rollout across two independently
shipped apps possible. Do not remove it, and do not add a strict-mode switch that turns
unknown messages into errors.

Land a new message in this order.

**1. Specify it first.** Add the message to `PROTOCOL.md` §7 in both repositories, in the
same commit-worthy form: the exact `t` string, every JSON key, field types, whether it is
sent before or after the session is established, and what a receiver does when a field it
expects is absent. If the message announces bytes, say where it sits in the strict
`clip` → `transferStart` → chunks → `transferEnd` ordering. The two copies of `PROTOCOL.md`
must stay identical; diff them before you push.

**2. Make it optional by construction.** A new message must never be required for a session
to work. A peer that has never heard of it must be able to complete a handshake, sync a
clip, and disconnect cleanly. If the feature genuinely cannot degrade — if one side must
know the other understands it — negotiate it by extending `caps` with a new optional
boolean field rather than by bumping the protocol version. `caps` is a JSON object with
lenient decoding on both sides, so an added key is invisible to an older peer, and an
absent key on the wire must be read as "not supported".

Bump the protocol version only for a breaking change to framing, the handshake, or the
canonical hash. A version bump makes every older peer refuse the connection with
`versionMismatch`, so it is a last resort, and it has to ship on both platforms
simultaneously.

**3. Android side, in this order:**

- Add the `t` constant to `MessageType` and include it in `MessageType.KNOWN`. A type
  absent from `KNOWN` decodes to `UnknownMessage` no matter what else you write.
- Add a `@Serializable` data class in `core/protocol/src/main/kotlin/app/synco/protocol/message/`,
  one file named after the type, implementing `ControlMessage`, with `@SerialName` on the
  class matching the `t` string and on every property matching the JSON key exactly. Give
  every field that older peers may omit a default value.
- Add a round-trip case to `EnvelopeCodecTest`, and confirm
  `EnvelopeForwardCompatibilityTest` still passes — it is the test that proves an unknown
  type survives decoding.
- Handle it where it belongs. Session-level messages are dispatched in `:sync`
  (`ClipRouter` and its collaborators); handshake and pairing messages belong in
  `:core:transport`. Ignoring a message you do not yet handle is acceptable; crashing on it
  is not.
- If the message carries a new user-visible outcome, extend `SyncEvent.Kind` or
  `AckReason` rather than smuggling meaning into a free-text field.

**4. Mac side.** Mirror the same three things: the type constant, the codable type with
identical coding keys and defaults, and the dispatch site. The Swift and Kotlin decoders
must agree on optionality — a field with a Kotlin default and a non-optional Swift property
is a decode failure on one platform only, which is the hardest class of bug this project
has.

**5. Verify across the boundary.** Before merging either side, run the older build of one
platform against the newer build of the other, in both directions:

- new Android against old Mac,
- new Mac against old Android.

Both must complete a handshake, sync text, sync a file, and disconnect with `shutdown`
rather than an error close reason. If the new message is ignored by the old peer, you have
done it right. If the old peer disconnects, something you added is not actually optional.

**6. Ship the receiver first.** Release the version that *understands* the new message
before the version that *sends* it, on both platforms. Users update the two apps at
different times, and there is no server to coordinate them.

## Pull requests

- One concern per pull request. A protocol change and a UI change are two pull requests.
- Say which module boundaries the change crosses, and why.
- If the change touches the wire, state explicitly what an un-updated peer on the other
  platform does when it meets it.
- Build files (`settings.gradle.kts`, any `build.gradle.kts`,
  `gradle/libs.versions.toml`) are shared surface: describe the change and why the existing
  convention plugins could not carry it.
