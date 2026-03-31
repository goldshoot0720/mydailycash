# Python GUI 版本

這個子項目提供 `tkinter` 桌面版，支援：
- 單注 / 包牌
- 6連碰
- 7連碰
- 8連碰
- 9連碰

## 執行方式

在專案根目錄執行：

```powershell
python .\python_gui\app.py
```

若要略過快取重新啟動，可使用：

```powershell
python -B .\python_gui\app.py
```

## macOS 打包成 App

在專案根目錄執行：

```bash
python3 -m pip install -r python_gui/requirements-build.txt
zsh python_gui/build_macos.sh
```

輸出 App 會在：

`dist/MyDailyCash.app`

打包前會自動用下列素材產生 macOS `.icns` 圖示：

`python_gui/assets/macos_app_icon.svg`

若要產生可上傳到 GitHub Release 的壓縮檔，可再執行：

```bash
ditto -c -k --sequesterRsrc --keepParent dist/MyDailyCash.app dist/MyDailyCash-macos.zip
```

## 打包 EXE

在專案根目錄執行：

```powershell
python -m PyInstaller --noconfirm --clean --onefile --windowed --name mydailycash-python-gui --add-data "index.html;." .\python_gui\app.py
```

輸出檔案會在：

`dist\mydailycash-python-gui.exe`

## GitHub Release 自動發佈 macOS App

專案已提供 GitHub Actions workflow：

`../.github/workflows/release-macos-python.yml`

使用方式：

1. 將專案推到 GitHub repository
2. 建立並推送新 tag，例如 `v1.0.3`
3. GitHub Actions 會在 macOS runner 上打包 `MyDailyCash.app`
4. Workflow 會自動建立 `MyDailyCash-macos.zip` 並附加到該 tag 的 GitHub Release

若已在 GitHub repository secrets 設定下列資料，workflow 也會自動簽名與 notarize：

- `MACOS_CERTIFICATE_P12_BASE64`
- `MACOS_CERTIFICATE_PASSWORD`
- `MACOS_SIGNING_IDENTITY`
- `APPLE_ID`
- `APPLE_APP_SPECIFIC_PASSWORD`
- `APPLE_TEAM_ID`

若 secrets 尚未設定，workflow 仍會正常產出 unsigned 的 macOS App。

## 說明

- GUI 版會讀取專案中的 `index.html` 內建開獎資料
- 打包成 EXE 時，也會把 `index.html` 一起封裝進執行檔
- 打包成 macOS App 時，也會把 `index.html` 一起封裝進 App
- `build_macos.sh` 會使用 `com.goldshoot0720.mydailycash` 作為預設 bundle identifier，可透過 `MACOS_BUNDLE_ID` 覆寫
