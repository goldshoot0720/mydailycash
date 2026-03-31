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

## 打包 EXE

在專案根目錄執行：

```powershell
python -m PyInstaller --noconfirm --clean --onefile --windowed --name mydailycash-python-gui --add-data "index.html;." .\python_gui\app.py
```

輸出檔案會在：

`dist\mydailycash-python-gui.exe`

## 說明

- GUI 版會讀取專案中的 `index.html` 內建開獎資料
- 打包成 EXE 時，也會把 `index.html` 一起封裝進執行檔
