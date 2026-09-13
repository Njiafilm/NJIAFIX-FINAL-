# Kujenga APK ya NjiaFix kwa TERMUX (moja kwa moja simuni)

Njia hii HAIHITAJI kompyuta wala Android Studio - inafanya kazi ndani ya simu yako
kupitia programu ya **Termux**.

## Maandalizi
1. Sakinisha **Termux** kutoka **F-Droid** (SIYO Google Play - toleo la Play Store
   halisasishwi tena): https://f-droid.org/en/packages/com.termux/
2. Pakua/hamishia folda hii nzima (`NjiaFixApp`, ikiwa na `termux_jenga_apk.sh`
   ndani yake, kando ya `build.gradle`) kwenye simu yako, mfano kwenye `Download/`.
3. Fungua Termux na uruhusu ufikiaji wa hifadhi:
   ```
   termux-setup-storage
   ```
   (itauliza ruhusa - kubali)
4. Nenda kwenye folda ya `NjiaFixApp` uliyoihamishia, mfano:
   ```
   cd ~/storage/shared/Download/NjiaFixApp
   ```
   (Badilisha njia hiyo kulingana na pale ulipoweka faili. Script inajitambua
   yenyewe iko wapi, kwa hiyo muhimu ni kuwa ndani ya folda ya `NjiaFixApp`
   unapoendesha amri - yaani folda yenye `build.gradle`, `app/`, n.k.)

## Kujenga

Ukiwa ndani ya folda ya `NjiaFixApp`:
```
bash termux_jenga_apk.sh
```

- Hatua ya kwanza (kuweka Android SDK) ni kubwa - inaweza kuchukua **dakika 15-40**
  kutegemea kasi ya intaneti. **Tumia WiFi**, sio data ya simu, kama inawezekana.
- Ukimalizika bila hitilafu, APK itakuwa:
  `NjiaFixApp/app/build/outputs/apk/debug/app-debug.apk`
  na nakala itawekwa moja kwa moja `Download/NjiaFix.apk`.
- Fungua faili hiyo kwa "Files" app ya simu yako kuisakinisha (itakuuliza kuruhusu
  "install from unknown sources" mara ya kwanza - hii ni kawaida kwa APK
  zisizotoka Play Store).

## Ukikwama

- `pkg install` ikishindwa kupata kifurushi (mfano `aapt2` au `gradle`), jaribu
  `pkg update -y` tena kisha rudia amri.
- Ukiona hitilafu ya "SDK license not accepted" wakati wa
  `scripts/setup-android-sdk.sh`, endesha tena script - mara nyingi inakubali
  leseni kiotomatiki lakini wakati mwingine inahitaji kuendeshwa mara mbili.
- Ukitaka kuanza upya kabisa: futa folda `termux-packages` na jaribu tena.
- Njia hii inabadilika mara kwa mara kadri Termux inavyosasishwa - ukikwama
  kwenye hatua fulani, tuma ujumbe wa hitilafu (error) uliopata na tutatatua
  pamoja.

## Kwa nini script hii ni kubwa/inachukua muda?

Termux haina Android SDK kwa default. Script inapakua vipande muhimu vya SDK
(sawa na vile Android Studio ingepakua) ili Gradle iweze kujenga programu yenye
maktaba za AndroidX/Material ulizoziona kwenye mradi huu (WebView, vitufe,
mandhari). Hii inafanyika **mara moja tu** - ukijenga tena baadaye (baada ya
mabadiliko), hatua hiyo kubwa haitarudiwa, `gradle assembleDebug` peke yake
itatosha.
