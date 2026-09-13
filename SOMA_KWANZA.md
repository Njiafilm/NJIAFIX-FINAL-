# NjiaFix - Programu ya Android (APK)

Hii ni programu ya Android (WebView wrapper) inayofungua dashibodi yako ya wavuti ya NjiaFix
(backend ya Spring Boot uliyopakia). Inafanya kazi kwa njia mbili, bila kubadilisha msimbo wowote
wa backend:

- **Offline (ndani ya mtandao wako)**: Endesha `njiafix-java` kwenye kompyuta/laptop yako
  (kama ilivyowekwa kwenye `application.properties`, inasikiliza `127.0.0.1:5555`). Badilisha
  hiyo iwe `server.address=0.0.0.0` ili simu ipate kuifikia kwenye WiFi ile ile / hotspot,
  kisha weka kwenye programu anwani kama `http://192.168.x.x:5555` (IP ya kompyuta yako).
  Hii HAIHITAJI intaneti - inafanya kazi kwenye mtandao wa ndani/hotspot tu.
- **Mtandaoni (Web)**: Ukishatuma (deploy) backend Render au sehemu nyingine, weka URL yake
  kamili (mfano `https://njiafix.onrender.com`) kwenye Mipangilio ya programu.

Programu inakumbuka anwani uliyoweka, na kitufe cha ⚙ (Mipangilio) kwenye programu
kinakuruhusu kubadilisha wakati wowote — hakuna haja ya kutengeneza APK mpya ukibadilisha seva.

## Jinsi ya kutengeneza APK (hatua chache)

1. Pakua/fungua folda hii (`NjiaFixApp`) kwenye **Android Studio** (Open an existing project).
2. Acha Android Studio i-download Gradle/SDK zinazohitajika (itaonyesha "Sync Now" - bonyeza).
3. Chagua **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. APK itapatikana kwenye: `app/build/outputs/apk/debug/app-debug.apk`
   (hii ni ya kujaribu - unaweza kuisakinisha moja kwa moja kwenye simu yako kwa USB
   au kuituma WhatsApp/faili).
5. Ukitaka kuichapisha Play Store au kuisambaza rasmi, tengeneza **signed APK/AAB**
   kupitia **Build > Generate Signed Bundle / APK** (utahitaji kutengeneza keystore).

## Kwa nini sikutengeneza APK yenyewe moja kwa moja?

Mazingira ninayofanyia kazi hayana Android SDK wala muunganisho wa intaneti (kuvuta Gradle/SDK),
kwa hiyo siwezi ku-*compile* faili ya .apk moja kwa moja. Nimekuandalia mradi kamili, sahihi,
uliopangwa vizuri (Gradle + Java + layouts) ili ubonyeze "Build" tu upate APK.

## Vipengele vilivyowekwa

- WebView inayofungua seva yako (LAN au mtandaoni) — kurasa zako zote (dashboard, diagnose,
  diagnose-other) zinafanya kazi kama kwenye kivinjari, ikiwa ni pamoja na `/api/...` zote.
- Ruhusa ya **kamera** imeunganishwa moja kwa moja na `CameraDiagnosticService` (getUserMedia
  kwenye diagnose-other.html) — programu itauliza ruhusa ya kamera kiotomatiki inapohitajika.
- Ukurasa wa "Hakuna muunganisho" wenye kitufe cha "Jaribu Tena" na "Badilisha Seva" endapo
  simu haiwezi kufika kwenye seva uliyoweka.
- `usesCleartextTraffic="true"` imewezeshwa ili http:// (anwani za IP za ndani/LAN) zifanye kazi.

## Kikomo kimoja muhimu

Uchunguzi wa OBD2 (jSerialComm, Bluetooth SPP/COM port) unahitaji uwezo wa serial port wa
kompyuta - hauwezi kufanya kazi ndani ya programu ya Android moja kwa moja. Backend
lazima iendelee kukimbia kwenye kompyuta iliyounganishwa na adapta ya OBD; programu ya
simu inafanya kazi kama "kidhibiti cha mbali" (remote control) kupitia mtandao huo.
