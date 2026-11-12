#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT to your Android SDK}"
BUILD_TOOLS="${BUILD_TOOLS:-$ANDROID_SDK_ROOT/build-tools/35.0.0}"
ANDROID_JAR="${ANDROID_JAR:-$ANDROID_SDK_ROOT/platforms/android-35/android.jar}"
JAVAC="${JAVAC:-javac}"
mkdir -p build/classes build/dex build/generated
"$BUILD_TOOLS/aapt" package -f -M app/src/main/AndroidManifest.xml -S app/src/main/res -A app/src/main/assets -I "$ANDROID_JAR" -F build/base.apk -J build/generated -0 wav
"$JAVAC" -encoding UTF-8 -source 8 -target 8 -bootclasspath "$ANDROID_JAR" -d build/classes $(find app/src/main/java build/generated -name '*.java')
"$BUILD_TOOLS/d8" --min-api 23 --lib "$ANDROID_JAR" --output build/dex $(find build/classes -name '*.class')
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q -u ../unsigned.apk classes.dex)
"$BUILD_TOOLS/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
if [ ! -f signing.p12 ]; then
 keytool -genkeypair -keystore signing.p12 -storepass android -keypass android -alias shanhai -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Shanhai Tongqi Development,O=Shanhai,C=CN' >/dev/null 2>&1
fi
"$BUILD_TOOLS/apksigner" sign --ks signing.p12 --ks-pass pass:android --key-pass pass:android --ks-key-alias shanhai --out build/shanhai-tongqi-0.2.0.apk build/aligned.apk
"$BUILD_TOOLS/apksigner" verify --verbose build/shanhai-tongqi-0.2.0.apk
