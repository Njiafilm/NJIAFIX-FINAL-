# NjiaFix Native Android

App ya **native** – UI ya Android + server ya Node.js inayoendesha **ndani ya simu hiyo hiyo (Termux)**.
Kila jibu linatokana na amri/kipimo halisi; bila data halisi hakuna hitimisho.

## Screens
- Home (hali halisi ya server + kitufe cha WASHA SERVER)
- Simu (chunguza, reboot, cache, Wi-Fi, taarifa, recovery)
- Malipo (REKODI tu - server haithibitishi na mtoa huduma)
- Unlock / Passkey (maelekezo)
- Software Tools (maelekezo + amri halisi za ADB + WASHA SERVER)
- Vifaa vingine (printer, camera, router, PC - mtandao wa ndani)
- Settings (Server IP; chaguo-msingi 127.0.0.1)

## Server (Termux)
Tazama `njiafix-server.zip` (server.js, start.sh, hali.sh, unganisha.sh). Inasikiliza 127.0.0.1:5555.

## Jenga APK
1. Android Studio → Open folder `NjiaFixNative`
2. Build → Build APK(s)
3. Baada ya kusakinisha: toa ruhusa ya Termux (RUN_COMMAND) unapobonyeza WASHA SERVER.

## Vifaa vinavyosaidiwa (kila kimoja kwa njia halisi)
| Kifaa | Njia | Inahitaji |
|---|---|---|
| Simu za Android | ADB (info, chunguza, reboot, cache, Wi-Fi) | USB debugging; kifaa kiunganishwe |
| Printer / photocopier | TCP + SNMP (hali, hitilafu, kurasa, toner) | Mtandao wa ndani; SNMP ON |
| Gari | OBD-II (ELM327 Wi-Fi): RPM, joto, volti, DTC, VIN | Adapta ya Wi-Fi ELM327; simu kwenye Wi-Fi yake |
| Umeme | Modbus TCP (read registers) | IP ya kifaa (port 502) |
| Camera / TV / Router / PC | TCP/HTTP/RTSP | Mtandao wa ndani |
| PC yenyewe | RAM, hifadhi, CPU (server ikiendeshwa kwenye PC) | start-pc.bat / start-pc.sh |
| Simu za button | Kutambua hali ya USB (MTK/SPD/Qualcomm/Samsung) | Server iwe PC |

Developer: Godwin Dotto
