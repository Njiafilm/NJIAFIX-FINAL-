#!/usr/bin/env bash
cd "$(dirname "$0")" || exit 1
command -v node >/dev/null || { echo "Weka Node.js kwanza"; exit 1; }
[ -d node_modules ] || npm install
command -v adb >/dev/null || echo "ONYO: adb haipo - weka android-tools/platform-tools"
echo "Server itasikiliza 127.0.0.1:5555. Kwa simu nyingine: HOST=0.0.0.0 ./start-pc.sh"
exec node server.js
