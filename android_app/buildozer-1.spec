[app]

# (str) Title of your application
title = Njiafix Fundi

# (str) Package name
package.name = njiafixfundi

# (str) Package domain (needed for android/ios packaging)
package.domain = org.njiafix

# (str) Source code where the main.py live
source.dir = .

# (list) Source files to include (let empty to include all the files)
source.include_exts = py,png,jpg,kv,atlas,txt,xml

# (str) Application versioning
version = 0.1

# (list) Application requirements
requirements = python3,kivy,pyjnius

# (str) Presplash / Icon (hiari - ongeza baadaye ukiwa na logo)
# presplash.filename = %(source.dir)s/logo_phone.png
# icon.filename = %(source.dir)s/logo_phone.png

# (str) Supported orientation
orientation = portrait

# (bool) Indicate if the application should be fullscreen or not
fullscreen = 0

# ---------------- ANDROID SPECIFIC ----------------

# (list) Permissions
android.permissions = INTERNET,ACCESS_NETWORK_STATE,ACCESS_WIFI_STATE,BIND_DEVICE_ADMIN,TRANSMIT_IR

# (int) Target Android API, should be as high as possible.
android.api = 33

# (int) Minimum API your APK / AAB will support.
android.minapi = 24

# (str) Android NDK version to use
android.ndk = 25b

# (bool) Use --private data storage (True) or --dir public storage (False)
android.private_storage = True

# (str) Android additional Java source folder (Device Admin + Adb Bridge)
android.add_src = java

# (str) Additional Android resources folder (device_admin_receiver.xml)
android.add_resources = xml:res/xml

# (list) Gradle dependencies (AdbLib kupitia JitPack - angalia WIFI_ADB_SETUP.txt)
android.gradle_dependencies = com.github.cgutman:AdbLib:master-SNAPSHOT

# (list) Gradle repositories za ziada (JitPack kwa AdbLib)
android.add_gradle_repositories = maven { url "https://jitpack.io" }

# (bool) If True, then skip trying to update the Android sdk
# android.skip_update = False

# (str) The Android arch to build for
android.archs = arm64-v8a, armeabi-v7a

# (bool) enables Android auto backup feature (Android API >=23)
android.allow_backup = True

[buildozer]

# (int) Log level (0 = error only, 1 = info, 2 = debug)
log_level = 2

# (int) Display warning if buildozer is run as root
warn_on_root = 1
