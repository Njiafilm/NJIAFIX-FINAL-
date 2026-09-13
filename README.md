# NjiaFix Native Android

App ya **native** (si WebView) – UI ya Android + HTTP API kwenye Spring Boot server.

## Screens
- Home
- Simu (ADB diagnose, reboot, cache, wifi)
- Malipo
- Unlock / Passkey guides
- Software Tools (Odin, SP Flash, FRP guides + ADB commands)
- Vifaa vingine (printer, camera, router, PC)
- Settings (Server IP)

## Jenga APK
1. Android Studio → Open folder `NjiaFixNative`
2. Settings → weka IP ya PC (au badilisha default `10.0.2.2` kwa emulator)
3. Build → Build APK(s)
4. `app/build/outputs/apk/debug/app-debug.apk`

## Server
```
server.address=0.0.0.0
server.port=5555
```
PC na simu → Wi-Fi/LAN moja.

## Kumbuka
ADB/Odin/flash bado zinaendesha kwenye **PC** (server). App ni remote control + guides.
