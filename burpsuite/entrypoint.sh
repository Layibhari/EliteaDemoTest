#!/usr/bin/env sh
set -eu

export DISPLAY=:99

Xvfb "$DISPLAY" -screen 0 1280x900x24 &
fluxbox >/tmp/fluxbox.log 2>&1 &
x11vnc -display "$DISPLAY" -forever -shared -nopw -listen 0.0.0.0 -rfbport 5900 >/tmp/x11vnc.log 2>&1 &

exec java -jar /opt/burp/burpsuite_community.jar
