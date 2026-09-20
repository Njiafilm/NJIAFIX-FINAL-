#!/data/data/com.termux/files/usr/bin/bash
# Huwasha server ya NJIAFIX 24/7: wake-lock + adb + pm2 (haijirudii ikiwa tayari inaendesha)
termux-wake-lock
cd "$(dirname "$0")" || exit 1
adb start-server >/dev/null 2>&1
if pm2 describe njiafix 2>/dev/null | grep -q "online"; then
  :
elif pm2 describe njiafix >/dev/null 2>&1; then
  pm2 restart njiafix >/dev/null 2>&1
else
  pm2 start server.js --name njiafix >/dev/null 2>&1
fi
pm2 save >/dev/null 2>&1
