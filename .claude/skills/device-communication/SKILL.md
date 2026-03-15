---
name: device-communication
description: Decent Scale (BLE), Star SM-S210i printer (StarXpand SDK), Gicisky e-paper (HTTP/ESP32) の通信実装に使用。Bluetooth、BLE、GATT、プリンター、印刷、電子ペーパー、ESP32、OpenEPaperLink に関するコード実装時に自動読み込み。
---

# デバイス通信仕様

3つのデバイスの詳細な通信仕様は `specs/DEVICES.md` を参照すること。

## 実装時の注意

### Decent Scale (BLE)
- Notify UUID: `0000FFF4-...` / Write UUID: `000036F5-...`
- 重量は byte[2-3] の signed short / 10.0 でグラム変換
- minSdk 31 のため `neverForLocation` で位置情報パーミッション不要
- 詳細: @specs/DEVICES.md の「1. Decent Scale」セクション

### SM-S210i (StarXpand SDK)
- IMPORTANT: 生のESC/POSコマンドではなく StarXpand SDK を使うこと
- 依存: `com.starmicronics:stario10`
- QRコード印刷: `actionPrintQRCode(QRCodeParameter(...))` を使用
- 毎回 open → print → close のフローで接続する
- 詳細: @specs/DEVICES.md の「2. スター精密 SM-S210i」セクション

### Gicisky E-Paper (HTTP)
- ESP32 AP に対して `POST /imgupload` (multipart/form-data)
- パラメータ: file (JPEG 296x128), mac (MACアドレス), dither ("0")
- OkHttp で実装。BLE実装は不要（ESP32が担当）
- 詳細: @specs/DEVICES.md の「3. Gicisky 2.9インチ電子ペーパー」セクション
