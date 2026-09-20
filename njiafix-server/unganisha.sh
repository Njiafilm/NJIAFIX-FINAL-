#!/data/data/com.termux/files/usr/bin/bash
echo "1) Android 11 au mpya (Wireless debugging, kuoanisha)"
echo "2) Android 10 au chini (baada ya 'adb tcpip 5555')"
read -p "Chagua 1 au 2: " C
if [ "$C" = "1" ]; then
  read -p "IP:PORT ya kuoanisha (skrini ya pairing): " P
  read -p "Namba ya kuoanisha (tarakimu 6): " N
  adb pair "$P" "$N" || { echo "Kuoanisha kumeshindwa"; exit 1; }
  read -p "IP:PORT ya kuunganisha (skrini kuu ya Wireless debugging): " T
else
  read -p "IP ya kifaa (mfano 192.168.1.20): " I
  T="$I:5555"
fi
adb connect "$T"
adb devices
