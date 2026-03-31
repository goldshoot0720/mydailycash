# MyDailyCash

This repository currently contains three versions of the project:

- `index.html`: browser version
- `python_gui/`: desktop subproject built with `tkinter`
- `android_apk/`: Android app subproject that can be built into an APK

## Android APK subproject

The `android_apk/` folder is a standalone Android Studio project with:

- five number inputs
- draw matching
- cost, prize, and net summary
- recent draw result cards

## Build the Android APK

1. Open `android_apk/` in Android Studio
2. Wait for Gradle Sync to finish
3. Run `Build > Build Bundle(s) / APK(s) > Build APK(s)`

The debug APK is expected at:

`android_apk/app/build/outputs/apk/debug/`
