# MyDailyCash

This repository currently contains three versions of the project:

- `index.html`: browser version
- `python_gui/`: desktop subproject built with `tkinter`
- `android_apk/`: Android app subproject that can be built into an APK

## Python desktop subproject

The `python_gui/` folder is a standalone Python desktop app.

- Local macOS build helper: `python_gui/build_macos.sh`
- GitHub Actions release workflow: `.github/workflows/release-macos-python.yml`
- Tag release trigger: push a tag like `v1.0.3`

When the workflow runs on GitHub, it builds `dist/MyDailyCash.app`, zips it as `dist/MyDailyCash-macos.zip`, and attaches that zip to the GitHub release.

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
