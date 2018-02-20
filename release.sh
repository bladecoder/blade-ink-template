#!/bin/bash
set -e

DIST_DIR=$HOME"/PACKAGES"
IOS_SIGN_IDENTITY="iPhone Distribution"
IOS_PROVISIONING_PROFILE=XXX
ANDROID_KEYSTORE=$HOME/Dropbox/docs/ids/rgarcia_android.keystore
ANDROID_KEY_ALIAS=bladecoder

PROJECT_NAME=`cat gradle.properties | grep appName | cut -d'=' -f2`

if [ "$#" -eq 0 ]
then
  echo "Release type param needed: android, ios or desktop"
  exit 0
else
  RELEASE_MODE=$1
fi

echo -n "Version: "
read VERSION
echo

if [[ "$OSTYPE" == 'darwin'* ]]; then
  sed -i .bak 's/version=.*/version='$VERSION'/' gradle.properties
else
  sed -i 's/version=.*/version='$VERSION'/' gradle.properties
fi

if [ "$RELEASE_MODE" == "android" ]; then
  echo -n "Version Code: "
  read VERSION_CODE
  echo
  echo -n "Keystore Password: "
  read -s KEYSTORE_PASSWD
  echo
  echo -n "Key Password: "
  read -s KEY_PASSWD
  echo

  RELFILENAME="$DIST_DIR"/$PROJECT_NAME-$VERSION.apk

  ./gradlew -Pkeystore=$ANDROID_KEYSTORE -PstorePassword=$KEYSTORE_PASSWD -Palias=$ANDROID_KEY_ALIAS -PkeyPassword=$KEY_PASSWD android:assembleRelease -Pversion=$VERSION  -PversionCode=$VERSION_CODE
  cp android/build/outputs/apk/android-release.apk "$RELFILENAME"
elif [[ "$RELEASE_MODE" == "desktop" ]]; then
  RELFILENAME="$DIST_DIR"/$PROJECT_NAME-steam-$VERSION.jar
  ./gradlew desktop:dist -Pversion=$VERSION
  cp desktop/build/libs/$PROJECT_NAME-desktop-$VERSION.jar "$RELFILENAME"
elif [[ "$RELEASE_MODE" == "ios" ]]; then
  RELFILENAME="$DIST_DIR"/$PROJECT_NAME-$VERSION.ipa

  echo -n "Version Code: "
  read VERSION_CODE
  echo

  # Update ios/robovm.properties
  sed -i .bak 's/app.version=.*/app.version='$VERSION'/' ios/robovm.properties
  sed -i .bak 's/app.build=.*/app.build='$VERSION_CODE'/' ios/robovm.properties

  ./gradlew -Probovm.iosSignIdentity="$IOS_SIGN_IDENTITY" -Probovm.iosProvisioningProfile="$IOS_PROVISIONING_PROFILE" ios:clean ios:createIPA
  cp ios/build/robovm/IOSLauncher.ipa "$RELFILENAME"
else
  echo Release type param not valid: $RELEASE_MODE. Valid options: android, ios or desktop.
  exit -1
fi

echo -- RELEASE OK: $RELFILENAME --
