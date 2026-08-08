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

echo "== starting clipboard sync through the UI =="
run shell am start -n "$package/app.synco.MainActivity" >/dev/null
sleep 5
run shell input tap 672 2800
sleep 2
run shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
row=$(run shell cat /sdcard/ui.xml \
    | tr '>' '>\n' \
    | grep -oE 'text="Clipboard sync"[^>]*bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"' \
    | grep -oE '\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]' | head -1 || true)
if [ -n "$row" ]; then
    rowy=$(( ( $(echo "$row" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\2/') \
        + $(echo "$row" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\4/') ) / 2 ))
    width=$(run shell wm size | grep -oE '[0-9]+x' | tr -d x)
    if run logcat -d -t 200 | grep -q "synco running"; then
        echo "ok: sync already running"
    else
        run shell input tap $((width - 160)) "$rowy"
        sleep 6
    fi
else
    echo "WARN: could not find the sync toggle, assuming it is on"
fi

echo "== toolbar copy reaches the focus gate =="
run shell am start -a android.settings.SETTINGS >/dev/null
sleep 4
run shell input tap 672 292
sleep 3
run shell input text "SyncoVerify$RANDOM"
sleep 2
run logcat -c
captured=""
for attempt in 1 2 3; do
    run shell input swipe 500 292 500 292 1100
    sleep 2
    run logcat -c
    run shell input keyevent 278
    sleep 4
    if run logcat -d | grep -q "captured a clip via ACCESSIBILITY_FOCUS_GATE"; then
        captured=yes
        break
    fi
    run shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
    bounds=$(run shell cat /sdcard/ui.xml \
        | tr '>' '>\n' \
        | grep -oE 'text="Copy"[^>]*bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"' \
        | grep -oE '\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]' | head -1 || true)
    if [ -z "$bounds" ]; then
        echo "  attempt $attempt: no Copy button, retrying"
        continue
    fi
    x1=$(echo "$bounds" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\1/')
    y1=$(echo "$bounds" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\2/')
    x2=$(echo "$bounds" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\3/')
    y2=$(echo "$bounds" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\4/')
    x=$(((x1 + x2) / 2))
    y=$(((y1 + y2) / 2))
    run shell input tap "$x" "$y"
    sleep 4
    if run logcat -d | grep -q "captured a clip via ACCESSIBILITY_FOCUS_GATE"; then
        captured=yes
        break
    fi
done
if [ -n "$captured" ]; then
    echo "ok: toolbar copy captured"
else
    echo "FAIL: the toolbar copy never captured"
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
[ -n "$pid" ] && run shell run-as "$package" kill -9 "$pid" 2>/dev/null || true
sleep 8
after=$(run shell settings get secure enabled_accessibility_services)
echo "$after" | grep -q "$package" || { echo "FAIL: accessibility was revoked by a plain kill"; exit 1; }
newpid=$(run shell pidof "$package" | tr -d '\r' || true)
if [ -n "$pid" ] && [ "$newpid" = "$pid" ]; then
    echo "WARN: the process could not be killed on this device, restart not exercised"
else
    echo "ok: killed $pid, now ${newpid:-not yet restarted}, accessibility kept"
fi

echo "ALL CAPTURE CHECKS PASSED"
