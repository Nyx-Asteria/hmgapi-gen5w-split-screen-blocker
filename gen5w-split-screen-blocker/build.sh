#!/bin/sh
# Builds Split Screen Blocker into out/: the hooks (hooks.jar), the signed mod zip and the USB zips.
#
# Needs a JDK (javac --release 8), and these environment variables:
#   ANDROID_JAR    android.jar for API 19 (Android 4.4)
#   BUILD_TOOLS    an Android build-tools folder (d8)
#   HMGAPI_CP      classpath holding hmgapi.engine.Signing (HMG-API's mod SDK)
#   MOD_KEY        optional HMG-API author key (otherwise a disposable test key is generated)
#   MKNEMESISMOD   HMG-API's tools/mknemesismod.sh (optional: the NEMESIS zips)
# Keys never go in this repository.
set -eu
cd "$(dirname "$0")"
: "${ANDROID_JAR:?}" "${BUILD_TOOLS:?}" "${HMGAPI_CP:?}"

ID=gen5w-split-screen-blocker
V=$(sed -n 's/.*"version": *"\([^"]*\)".*/\1/p' mod.json | head -1)
B=out/build
rm -rf out && mkdir -p $B/stubs $B/hooks $B/dex $B/mod $B/usb/HMGAPI/updates
if [ -z "${MOD_KEY:-}" ]; then
    mkdir -p out
    MOD_KEY="$PWD/out/test-author.key"
    java -cp "$HMGAPI_CP" hmgapi.engine.Signing keygen "$MOD_KEY"
    echo "WARNING: disposable test identity; do not use for stable updates" >&2
fi

# hooks.jar: runs inside the car's split-screen app; compiled against stubs of HMG-API's hook API
javac --release 8 -Xlint:-options -encoding UTF-8 -nowarn -cp "$ANDROID_JAR" -d $B/stubs $(find tools/stubs -name '*.java')
javac --release 8 -Xlint:-options -encoding UTF-8 -nowarn -cp "$ANDROID_JAR:$B/stubs" -d $B/hooks $(find src -name '*.java')
(cd $B/stubs && jar cf ../stubs.jar .) && (cd $B/hooks && jar cf ../hooks-classes.jar .)
"$BUILD_TOOLS/d8" --release --min-api 19 --lib "$ANDROID_JAR" --classpath $B/stubs.jar --output $B/mod/hooks.jar $B/hooks-classes.jar

# signed mod zip, fixed timestamps so the same sources give the same bytes
cp mod.json settings.json $B/mod/
java -cp "$HMGAPI_CP" hmgapi.engine.Signing sign $B/mod "$MOD_KEY"
java -cp "$HMGAPI_CP" hmgapi.engine.Signing verify $B/mod
(cd $B/mod && find . -exec touch -d '2026-01-01 00:00:00' {} + && \
    zip -X -q ../../$ID-v$V.zip META-HMGAPI/manifest.json META-HMGAPI/signature hooks.jar mod.json settings.json)

cp out/$ID-v$V.zip $B/usb/HMGAPI/updates/
(cd $B/usb && zip -r -q ../../$ID-v$V-hmgapi-usb.zip HMGAPI)
if [ -n "${MKNEMESISMOD:-}" ]; then
    sh "$MKNEMESISMOD" "$PWD/out/$ID-v$V.zip" "$PWD/$B/nemesis" >/dev/null
    mv $B/nemesis/*-nemesis.zip out/$ID-v$V-nemesis-usb.zip
    mv $B/nemesis/*-nemesis-package.zip out/$ID-v$V-nemesis-package.zip
fi
(cd out && sha256sum $ID-v$V*.zip > SHA256SUMS && cat SHA256SUMS)
