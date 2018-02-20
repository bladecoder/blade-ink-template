# ink-libgdx-template

This is a template to create interactive fiction games using the [Ink](https://www.inklestudios.com/ink/) language.

Features:

- Publish your games on Android, iOS and Desktop (Windows, Mac and Linux) with no effort.
- No coding is necessary.
- Fully customizable fonts, colors and images.

## How to generate your own interactive fiction game using the Blade Ink Template

### 1. Write your story

- Download [Inky](https://github.com/inkle/inky/releases) and write your story.

### 2. Export your story to Blade Ink Template

- Download the [Blade Ink Template](https://github.com/bladecoder/ink-libgdx-template/releases).
- Export your story to JSON from Inky as `story.ink.json` and copy it to the `assets` folder in the Blade Ink Template.
- You can now test your story with `./gradlew desktop:run`.

### 3. Customize your fonts colors, images and titles.
 
 - Edit the `ui/ui.json` with your text editor and customize the skin of your game.
 - Overwrite the images in `ui/1` with yours.
 - Change the `title` property in the `inkplayer.properties` with your app title.

### 4. Prepare for Android

- Overwrite all icons in `android/res/drawable-*` with your app icons.
- Put your app. name in the `android/res/values/strings.xml` file.
- Change the `applicationId` property in the `android/build.gradle` file with yours.


### 3. Prepare for Desktop

- Overwrite all icons in `desktop/src/main/resources/icons` with your app icons.

### 4. Prepare for iOS

### 5. Releasing your game

Releasing your game is very easy with the `release.sh` script.






-