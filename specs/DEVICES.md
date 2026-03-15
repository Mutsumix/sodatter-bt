# デバイス通信仕様書

## 1. Decent Scale (BLE)

### 概要

| 項目 | 値 |
|------|-----|
| 通信方式 | Bluetooth Low Energy (GATT) |
| デバイス名 | "Decent Scale" |
| データ送信頻度 | 10Hz |
| 公式API | https://decentespresso.com/decentscale_api |

### UUID

| 用途 | UUID |
|------|------|
| Notify (重量受信) | `0000FFF4-0000-1000-8000-00805F9B34FB` |
| Write (コマンド送信) | `000036F5-0000-1000-8000-00805F9B34FB` |

### コマンド

| 機能 | バイト列 (Hex) |
|------|----------------|
| Tare (ゼロリセット) | `03 0F 00 00 00 01 0E` |
| LED ON (グラム) | `03 0A 01 01 00 01 08` |
| LED OFF | `03 0A 00 00 00 00 09` |
| Timer Start | `03 0B 03 00 00 00 0B` |
| Timer Stop | `03 0B 00 00 00 00 08` |
| Timer Reset | `03 0B 02 00 00 00 0A` |

### 受信データフォーマット

#### Firmware v1.0/v1.1 (7バイト)

| Byte | 内容 |
|------|------|
| 0 | Model (03 = Decent) |
| 1 | Type (CE = 安定, CA = 変動) |
| 2-3 | Weight (signed short, big-endian) ÷ 10.0 = グラム |
| 4-5 | Change (変化量) |
| 6 | XOR検証 |

#### Firmware v1.2+ (10バイト)

| Byte | 内容 |
|------|------|
| 0 | Model (03) |
| 1 | Type (CE/CA) |
| 2-3 | Weight |
| 4 | Minutes |
| 5 | Seconds (0-59) |
| 6 | Deciseconds (0-9) |
| 7-8 | 将来用 |
| 9 | XOR検証 |

### 重量パース

```kotlin
fun parseWeight(data: ByteArray): Float {
    if (data.size < 7) return 0f
    val highByte = data[2].toInt() and 0xFF
    val lowByte = data[3].toInt() and 0xFF
    val rawWeight = (highByte shl 8) or lowByte
    val signedWeight = if (rawWeight > 32767) rawWeight - 65536 else rawWeight
    return signedWeight / 10.0f
}
```

### XOR計算

```kotlin
fun calculateXor(bytes: ByteArray): Byte {
    var xor = 0
    for (i in 0 until minOf(6, bytes.size)) {
        xor = xor xor (bytes[i].toInt() and 0xFF)
    }
    return xor.toByte()
}
```

### 接続フロー

1. BLEスキャンで "Decent Scale" を検出
2. GATT接続
3. FFF4 CharacteristicのNotificationを有効化
4. LED ONまたはTareコマンドを送信（APPモードに移行）
5. Notificationで重量データの受信開始

### 注意事項

- ペアリング不要（BLEなのでGATT接続のみ）
- minSdk 31のため `neverForLocation` により位置情報パーミッション不要
- 必要なパーミッション: `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`

---

## 2. スター精密 SM-S210i (StarXpand SDK)

### 概要

| 項目 | 値 |
|------|-----|
| 通信方式 | Bluetooth Classic (SPP)、StarXpand SDK経由 |
| SDK | `com.starmicronics:stario10` |
| GitHub | https://github.com/star-micronics/StarXpand-SDK-Android |
| ドキュメント | https://www.star-m.jp/starxpandsdk-oml.html |
| デフォルトPIN | 1234 |

### 接続の仕組み

- Bluetooth Classicのため、初回接続時にシステムのペアリングダイアログが自動表示される（設定アプリへの遷移は不要）
- ペアリング完了後は自動接続可能
- MACアドレスはStarDeviceDiscoveryManagerで自動取得
- 毎回接続→印刷→切断のフローを推奨（消費電力とエラー防止）

### Discovery（プリンター検索）

```kotlin
val manager = StarDeviceDiscoveryManagerFactory.create(
    listOf(InterfaceType.Bluetooth),
    context
)
manager.discoveryTime = 10000
manager.callback = object : StarDeviceDiscoveryManager.Callback {
    override fun onPrinterFound(printer: StarPrinter) {
        val identifier = printer.connectionSettings.identifier
        // identifierを保存して次回以降使用
    }
    override fun onDiscoveryFinished() { }
}
manager.startDiscovery()
```

### 接続と印刷（テキスト + QRコード）

```kotlin
val settings = StarConnectionSettings(
    InterfaceType.Bluetooth,
    identifier // Discovery or 保存済みMACアドレス
)
val printer = StarPrinter(settings, context)

try {
    printer.openAsync().await()

    val builder = StarXpandCommandBuilder()
    builder.addDocument(
        DocumentBuilder()
            .addPrinter(
                PrinterBuilder()
                    // 日本語設定
                    .styleInternationalCharacter(InternationalCharacterType.Japan)
                    .styleCJKCharacterPriority(listOf(CJKCharacterType.Japanese))
                    // テキスト印刷
                    .actionPrintText("サニーレタス\n")
                    .actionPrintText("播種: 2026/01/04\n")
                    .actionPrintText("収穫: 2026/02/15\n")
                    .actionPrintText("重量: 142.5g\n")
                    .actionFeedLine(1)
                    // QRコード印刷
                    .actionPrintQRCode(
                        QRCodeParameter("https://example.com/cultivation/123")
                            .setLevel(QRCodeLevel.L)
                            .setCellSize(8)
                    )
                    .actionFeedLine(2)
                    // カット
                    .actionCut(CutType.Partial)
            )
    )

    printer.printAsync(builder.getCommands()).await()
} catch (e: StarIO10Exception) {
    // エラーハンドリング
} finally {
    printer.closeAsync().await()
}
```

### ステータス取得

```kotlin
val status = printer.getStatusAsync().await()
// status.coverOpen — カバー開閉
// status.paperEmpty — 用紙切れ
```

### エラーハンドリング

```kotlin
try {
    printer.openAsync().await()
} catch (e: StarIO10UnprintableException) {
    // 印刷不可（用紙切れ等）
} catch (e: StarIO10CommunicationException) {
    // 通信エラー
} catch (e: StarIO10NotFoundException) {
    // プリンター未発見
}
```

### 必要なパーミッション

- `BLUETOOTH_CONNECT`（Android 12+）
- `BLUETOOTH_SCAN`（Android 12+）

---

## 3. Gicisky 2.9インチ電子ペーパー (HTTP → ESP32)

### 概要

| 項目 | 値 |
|------|-----|
| 通信方式 | HTTP → ESP32 (OpenEPaperLink AP) → BLE → タグ |
| 画像サイズ | 296 x 128 px |
| 画像形式 | JPEG |
| タグ枚数 | 4枚（装置ごとに1枚） |
| ESP32ソフトウェア | OpenEPaperLink |

### API

| 項目 | 値 |
|------|-----|
| エンドポイント | `POST http://{AP_IP}/imgupload` |
| Content-Type | multipart/form-data |
| パラメータ: file | 画像ファイル (JPEG, 296x128) |
| パラメータ: mac | タグのMACアドレス |
| パラメータ: dither | ディザリング設定 ("0" = なし) |

### Kotlinでの実装例（OkHttp）

```kotlin
val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

// 画像をBitmapで生成 → JPEG ByteArrayに変換
val imageBytes = generateTagImage(cultivationRecord)

val requestBody = MultipartBody.Builder()
    .setType(MultipartBody.FORM)
    .addFormDataPart(
        "file", "image.jpg",
        imageBytes.toRequestBody("image/jpeg".toMediaType())
    )
    .addFormDataPart("mac", tagMacAddress)
    .addFormDataPart("dither", "0")
    .build()

val request = Request.Builder()
    .url("http://${apIpAddress}/imgupload")
    .post(requestBody)
    .build()

client.newCall(request).execute().use { response ->
    if (!response.isSuccessful) throw IOException("Upload failed: ${response.code}")
}
```

### タグ画像の内容

タグには以下の情報を表示する:
- 播種日
- 品種名
- QRコード（スキャンで写真記録を開始できる）

### 参考: Pythonでの実装（動作確認済み）

```python
url = f"http://{AP_IP}/imgupload"
with open("tag_image.jpg", 'rb') as f:
    files = {'file': ('image.jpg', f, 'image/jpeg')}
    data = {'mac': TAG_MAC, 'dither': '0'}
    response = requests.post(url, files=files, data=data, timeout=30)
```

### 注意事項

- ESP32はWi-Fi接続のため、INTERNET パーミッションが必要
- BLEの処理はESP32が担当するため、アプリ側のBLE実装は不要
- AP IPアドレスとタグMACアドレスはアプリの設定画面で管理
- 画像更新後、タグへの反映には数秒〜数十秒かかる場合がある
