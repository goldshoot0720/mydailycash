# Android APK 子項目

這個子項目是 `MyDailyCash` 的 Android App 版本，可由 Android Studio 編譯成 APK。

## 目前內容

- Kotlin + Jetpack Compose
- 5 碼選號輸入
- 最近期數比對
- 成本 / 獎金 / 淨損益摘要

## 開啟方式

1. 用 Android Studio 開啟這個資料夾
2. 等待 Gradle Sync 完成
3. 執行 APK 建置

## APK 輸出位置

除錯版 APK 預設會輸出到：

`app/build/outputs/apk/debug/`

正式簽名版 APK 預設會輸出到：

`app/build/outputs/apk/release/`

## GitHub Release 簽名設定

若要透過 GitHub Actions 發布可安裝的正式 APK，請先在 GitHub repository secrets 設定以下值：

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

其中 `ANDROID_KEYSTORE_BASE64` 需為 keystore 檔案的 Base64 內容。
