# Blade Ink Template

This is a template to create interactive fiction games using the [Ink](https://www.inklestudios.com/ink/) language.

Features:

- Publish your games on Android, iOS and Desktop (Windows, Mac and Linux) with no effort.
- No coding is necessary.
- Fully customizable fonts, colors and images.

# How to generate your own interactive fiction game with the Blade Ink Template

## 1. Write your story

- Download [Inky](https://github.com/inkle/inky/releases) and write your story.
- If you don't know Ink, you can read the tutorial [Writing with Ink](https://github.com/inkle/ink/blob/master/Documentation/WritingWithInk.md).

## 2. Export your story to Blade Ink Template

- Make sure you have installed the [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Download the [Blade Ink Template](https://github.com/bladecoder/blade-ink-template).
- Export your story to JSON from Inky as `story.ink.json` and copy it to the `assets` folder in the Blade Ink Template.
- You can now test your story with `./gradlew desktop:run` in Mac and Linux, or `gradlew.bat desktop:run` in Windows.

## 3. Customize your fonts, colors, images and titles.

 - Edit the `ui/ui.json` with your text editor and customize the skin of your game.
 - Overwrite the images in `ui/1` with yours.
 - Change the `title` property in the `inkplayer.properties` with your app title.

## 4. Prepare for Android

- Overwrite all icons in `android/res/drawable-*` with your app icons.
- Put your app. name in the `android/res/values/strings.xml` file.
- Change the `applicationId` property in the `android/build.gradle` file with yours.


## 3. Prepare for Desktop

- Overwrite all icons in `desktop/src/main/resources/icons` with your app icons.

## 4. Prepare for iOS

- Overwrite all icons in `ios/data/Media.xcassets/AppIcon.appiconset` with your app icons.
- Overwrite all launch images in `ios/data` with your app launch images.
- Change `app.name` and `app.id`properties in the `ios/robovm.properties` file with yours.

## 5. Releasing your game

Releasing your game is very easy with the `release.sh` script.

Unfortunately this is an Unix script and only works on Macos and Linux. Windows users can install a bash shell to execute the script or use the `gradlew.bat` directly to generate the packages. More info about using `gradlew`  [here](https://github.com/libgdx/libgdx/wiki/Gradle-on-the-Commandline).

The first step is to edit the `release.sh` script and set the next parameters:

```sh
# The destination folder for the packaged application
DIST_DIR=$HOME"/PACKAGES"

# IOS signing
IOS_SIGN_IDENTITY="iPhone Distribution"
IOS_PROVISIONING_PROFILE="your app provisioning profile"

# Android signing
ANDROID_KEYSTORE=$HOME/folder/to/the/android.keystore
ANDROID_KEY_ALIAS=androidkeyalias
```

And now you can:

Generate a `.ipa` for iPhone and iPad:

```sh
./release.sh ios
```

Generate a `.apk` for Android:

```sh
./release.sh android
```

Generate a runnable `.jar` for desktop:

```sh
./release.sh desktop
```

### Create native executables for desktop platforms

The normal way to distribute desktop applications is to embed the Java VM inside the package to not force the user to install Java.

There are several tools to create native executables for Windows, Macos and Linux with your runnable .jar. Some examples:

- [packr](https://github.com/libgdx/packr): Generates Windows, Macos and Linux packages.
- [launch4j](https://sourceforge.net/projects/launch4j/): Generates packages for Windows.
