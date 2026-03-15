# データベース定義

Room (SQLite) を使用。

## テーブル

### devices（栽培装置）

4台固定。アプリ初回起動時にシードデータとして挿入。

| カラム | 型 | 説明 |
|--------|-----|------|
| id | Int (PK) | 1〜4 |
| name | String | 表示名 ("A", "B", "C", "D") |
| tag_mac_address | String? | 電子ペーパータグのMACアドレス。未割当はnull |

### cultivations（栽培記録）

装置ごとの栽培サイクル。播種で1レコード作成、収穫で完了。

| カラム | 型 | 説明 |
|--------|-----|------|
| id | Long (PK, autoGenerate) | |
| device_id | Int (FK → devices.id) | |
| variety_name | String | 品種名 |
| manufacturer | String | メーカー名 |
| seeding_date | Long | 播種日 (epoch millis) |
| harvest_date | Long? | 収穫日。未収穫はnull |
| harvest_weight_gram | Float? | 収穫重量(g)。未収穫はnull |
| seed_photo_uri | String? | 種袋の写真URI |
| is_active | Boolean | true=栽培中, false=収穫済み |
| created_at | Long | レコード作成日時 |

### growth_photos（生育写真）

栽培記録に紐づく写真。0枚以上。

| カラム | 型 | 説明 |
|--------|-----|------|
| id | Long (PK, autoGenerate) | |
| cultivation_id | Long (FK → cultivations.id) | |
| photo_uri | String | 写真のURI |
| taken_at | Long | 撮影日時 (epoch millis) |
| note | String? | メモ（任意） |

### device_settings（デバイス設定）

BT機器・ESP32の接続情報。Key-Value形式。

| カラム | 型 | 説明 |
|--------|-----|------|
| key | String (PK) | 設定キー |
| value | String | 設定値 |

設定キーの例:
- `scale_identifier` — Decent ScaleのBLEアドレス
- `printer_identifier` — SM-S210iのMACアドレス
- `esp32_ip` — ESP32 APのIPアドレス

> **実装注意**: DAOの`@Insert`には必ず`onConflict = OnConflictStrategy.REPLACE`を指定すること。
> デフォルトの`ABORT`では既存キーへの上書きがクラッシュになる。

## リレーション

```
devices (1) ──── (*) cultivations (1) ──── (*) growth_photos
```

- 1つの装置に複数の栽培記録（過去の履歴）
- 1つの栽培記録に複数の生育写真
- 装置にアクティブな栽培記録は最大1つ（is_active = true は device_id ごとに1件）

## 主要クエリ

```
-- ホーム画面: 各装置の現在の栽培状況
SELECT d.*, c.* FROM devices d
LEFT JOIN cultivations c ON d.id = c.device_id AND c.is_active = 1

-- 栽培記録詳細: 生育写真一覧
SELECT * FROM growth_photos WHERE cultivation_id = :id ORDER BY taken_at ASC

-- 履歴一覧: 収穫済みの記録を新しい順に
SELECT c.*, d.name as device_name FROM cultivations c
JOIN devices d ON c.device_id = d.id
WHERE c.is_active = 0
ORDER BY c.harvest_date DESC

-- QRからの栽培記録特定
SELECT * FROM cultivations WHERE id = :cultivationId
```
