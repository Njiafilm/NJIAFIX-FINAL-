#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# NjiaFix - Jenga APK moja kwa moja kwenye simu kwa TERMUX
# ============================================================
# Weka faili hii NDANI ya folda ya NjiaFixApp (kando ya build.gradle),
# kisha ndani ya Termux:  cd  <folda-ya-NjiaFixApp>  &&  bash termux_jenga_apk.sh
#
# Script hii inajitambua mahali ilipo, kwa hiyo inaweza kuendeshwa kutoka
# folda yoyote (hata kama umeihamisha).
#
# APK ya mwisho: app/build/outputs/apk/debug/app-debug.apk
# (nakala pia itawekwa: ~/storage/shared/Download/NjiaFix.apk)
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
WORK_DIR="$HOME/njiafix-build-tools"
mkdir -p "$WORK_DIR"

echo ">>> Mradi: $PROJECT_DIR"
echo ">>> Hatua 1/6: Kusasisha vifurushi vya Termux..."
pkg update -y && pkg upgrade -y

echo ">>> Hatua 2/6: Kusakinisha zana (git, wget, aapt2, gradle, java)..."
pkg install -y git wget aapt aapt2 gradle openjdk-21 unzip

cd "$WORK_DIR"
if [ ! -d "termux-packages" ]; then
  echo ">>> Hatua 3/6: Kupakua termux-packages (kwa script ya Android SDK)..."
  git clone --depth=1 https://github.com/termux/termux-packages.git
else
  echo ">>> Hatua 3/6: termux-packages tayari ipo, inaruka..."
fi

echo ">>> Hatua 4/6: Kuweka Android SDK (hii ndiyo sehemu kubwa - subiri)..."
cd termux-packages
export TERMUX_PKG_TMPDIR="$PREFIX/tmp"
export TERMUX_JAVA_HOME="$PREFIX/lib/jvm/java-21-openjdk"
mkdir -p "$TERMUX_PKG_TMPDIR"
bash scripts/setup-android-sdk.sh

echo ">>> Hatua 5/6: Kuunganisha Gradle na SDK hiyo..."
mkdir -p ~/.gradle
echo "android.aapt2FromMavenOverride=$PREFIX/bin/aapt2" > ~/.gradle/gradle.properties
. scripts/properties.sh
echo "sdk.dir=$ANDROID_HOME" > "$PROJECT_DIR/local.properties"

echo ">>> Hatua 6/6: Kujenga APK (gradle assembleDebug)..."
cd "$PROJECT_DIR"
gradle assembleDebug --no-daemon

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
  echo ""
  echo "============================================="
  echo " HONGERA! APK imetengenezwa:"
  echo " $PROJECT_DIR/$APK_PATH"
  echo "============================================="
  termux-setup-storage 2>/dev/null || true
  mkdir -p ~/storage/shared/Download 2>/dev/null || true
  if cp "$APK_PATH" ~/storage/shared/Download/NjiaFix.apk 2>/dev/null; then
    echo "Imenakiliwa: Download/NjiaFix.apk - fungua kwa meneja wa faili kuisakinisha."
  fi
else
  echo "Hitilafu: APK haikuonekana. Angalia ujumbe wa makosa hapo juu."
fi
