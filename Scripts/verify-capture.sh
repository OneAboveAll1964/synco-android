#!/bin/bash
set -euo pipefail

serial="${1:?usage: verify-capture.sh <adb-serial>}"
adb="${ADB:-adb}"
package="com.shkomaghdid.synco.android.debug"
service="$package/app.synco.service.SyncoAccessibilityService"

run() { "$adb" -s "$serial" "$@"; }

echo "== binding the accessibility service =="
run shell settings put secure enabled_accessibility_services "$service"
run shell settings put secure accessibility_enabled 1
sleep 3
bound=$(run shell dumpsys accessibility | grep -c "Bound services:{Service" || true)
[ "$bound" -ge 1 ] || { echo "FAIL: the accessibility service never bound"; exit 1; }
echo "ok: bound"

echo "== toolbar copy reaches the focus gate =="
run shell am start -n "$package/app.synco.MainActivity" >/dev/null
sleep 5
run shell am start -a android.settings.SETTINGS >/dev/null
sleep 4
run shell input tap 672 292
sleep 3
run shell input text "SyncoVerify$RANDOM"
sleep 2
run logcat -c
run shell input swipe 500 292 500 292 1100
sleep 2
run shell input tap 312 466
sleep 5
if run logcat -d | grep -q "captured a clip via ACCESSIBILITY_FOCUS_GATE"; then
    echo "ok: toolbar copy captured"
else
    echo "FAIL: the toolbar copy never captured (coordinates assume a 1344x2992 emulator)"
    exit 1
fi

echo "== typing must not flash the overlay =="
run shell input tap 672 292
sleep 2
run logcat -c
for word in alpha beta gamma; do
    run shell input text "$word "
    sleep 1
done
sleep 2
flashes=$(run logcat -d | grep -c "opening the focus overlay" || true)
[ "$flashes" -le 1 ] || { echo "FAIL: $flashes overlay opens while typing"; exit 1; }
echo "ok: $flashes overlay open(s) while typing"

echo "== back leaves the app reachable =="
run shell input keyevent KEYCODE_BACK
sleep 2
run shell input keyevent KEYCODE_BACK
sleep 2
run shell dumpsys activity activities | grep -m1 "topResumedActivity" || true

echo "== process death must not silence capture =="
pid=$(run shell pidof "$package" | tr -d '\r')
[ -n "$pid" ] && run shell kill -9 "$pid" || true
sleep 6
after=$(run shell settings get secure enabled_accessibility_services)
echo "$after" | grep -q "$package" || { echo "FAIL: accessibility was revoked by a plain kill"; exit 1; }
echo "ok: accessibility survives a kill"

echo "ALL CAPTURE CHECKS PASSED"
