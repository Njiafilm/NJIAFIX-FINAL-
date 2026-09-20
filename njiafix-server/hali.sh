#!/data/data/com.termux/files/usr/bin/bash
echo "== Server =="; curl -s -m 3 localhost:5555/health || echo "HAIKO HAI: endesha ~/njiafix-server/start.sh"
echo; echo "== ADB =="; adb devices
echo; echo "== USB inayoonekana na Android =="; termux-usb -l
