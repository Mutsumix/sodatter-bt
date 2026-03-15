# Sodatter-BT アプリ仕様書

## アプリ概要

家庭菜園の播種から収穫までを一括管理するAndroidアプリ。
Bluetooth機器（電子はかり・モバイルプリンター）や電子ペーパータグと連携し、栽培記録の管理・計量・ラベル印刷・タグ表示更新を行う。

- アプリ名: Sodatter-BT
- パッケージ名: com.mutsumix.sodatterbt

## 技術スタック

| 項目 | 選定 |
|------|------|
| 言語 | Kotlin |
| UI | Jetpack Compose (BOM最新安定版) |
| アーキテクチャ | MVVM (Screen / ViewModel / UiState) |
| ナビゲーション | Navigation Compose (型安全ルート、Kotlin Serialization使用) |
| DB | Room |
| 非同期 | Kotlin Coroutines + Flow |
| DI | Hilt |
| HTTP | OkHttp |
| BLE | Android Bluetooth LE API |
| プリンター | StarXpand SDK (com.starmicronics:stario10) |
| 画像 | Coil |
| ビルド | Gradle Version Catalog (libs.versions.toml) |
| Compose Compiler | Kotlin 2.0+ の Compose Compiler Gradle Plugin |
| minSdk | 31 (Android 12) |
| targetSdk | 35 |

### 技術スタック補足

- minSdk 31により `neverForLocation` が使えるため、BLEスキャンに位置情報パーミッションが不要
- Navigation Composeは文字列ルートではなく、Kotlin Serializationによる型安全ルートを使用
- Compose CompilerはKotlin 2.0+のGradle Plugin方式を使用（composeOptionsブロックは不要）

## ビジネスルール

- 栽培装置は4台固定。動的な追加・削除は不要
- 1装置 = 1品種（複数株OK）。1つの装置で複数品種の同時栽培は想定しない
- 収穫は一括収穫のみ。継続収穫（バジル等の複数回収穫）は想定しない
- 播種 → 生育記録（写真は任意）→ 一括収穫 → 終了、のサイクル
- 収穫後は同じ装置で次の作物を開始できる
- クラウド同期は将来対応（本バージョンではローカル保存のみ）

## 画面構成と遷移

### 画面一覧 (9画面)

| # | 画面 | 概要 |
|---|------|------|
| 1 | ホーム | 装置4台のグリッド表示。空きスロットから播種登録へ |
| 2 | 播種登録 | 品種・メーカー・播種日・写真を入力 |
| 3 | 栽培記録詳細 | 作物の情報、生育写真ギャラリー、収穫ボタン |
| 4 | QRスキャン | タグのQRを読み取り、栽培記録を特定する |
| 5 | 写真記録 | カメラを起動し、生育写真を撮影・保存する |
| 6 | 収穫記録 | Decent Scaleの重量表示、収穫完了 |
| 7 | ラベル印刷プレビュー | QRコード付きラベルの確認と印刷 |
| 8 | 履歴一覧 | 過去の収穫記録リスト（月別グループ） |
| 9 | 設定 | 装置のタグ割当、BT機器接続、ESP32設定 |

### 画面遷移

```
ホーム
├─ 空き装置タップ or "+"ボタン → 播種登録 → ホーム
├─ 栽培中装置タップ → 栽培記録詳細
│   ├─ カメラボタン → 写真撮影 → 栽培記録詳細
│   └─ 収穫ボタン → 収穫記録
│       └─ ラベル印刷ボタン → ラベル印刷プレビュー → ホーム
├─ BottomNav「履歴」→ 履歴一覧
└─ BottomNav「設定」→ 設定

QRスキャン（外部起動 / アプリ内起動）
└─ QR認識 → 栽培記録特定 → カメラ起動 → 写真保存 → 栽培記録詳細
```

### QRコードの仕組み

- 電子ペーパータグにQRコードを表示する
- QRにはアプリ内で栽培記録を特定できるIDを埋め込む（カスタムスキーム: `sodatterbt://cultivation/{id}`）
- AndroidManifest.xmlのIntent Filterに `scheme="sodatterbt"` を設定することで外部QRリーダーからも起動可能
- スマホでQRを読み取ると、該当の栽培記録画面を経由してカメラが起動
- 撮影した写真はその栽培記録の生育写真として保存される
- 収穫後にモバイルプリンターで印刷するラベルにもQRコードを含める
- ラベルのQRを読み取ると、生育写真のギャラリーが閲覧できる

## 連携デバイス

| デバイス | 通信方式 | 用途 |
|---------|---------|------|
| Decent Scale | BLE (GATT) | 収穫時の重量自動取得 |
| スター精密 SM-S210i | Bluetooth Classic (StarXpand SDK) | QRコード付きラベル印刷 |
| Gicisky 2.9インチ電子ペーパー | HTTP → ESP32 (OpenEPaperLink) → BLE | 栽培装置のタグ表示 |

詳細は DEVICES.md を参照。

## デザイン方針

- デジタル庁デザインシステム (https://design.digital.go.jp/dads/) に準拠
- 背景は白基調。色はベタ塗りせず、枠線・アイコン・テキストのアクセントとして使用
- Primary: 淡い青 (#5B8BD4) — Bluetooth
- Secondary: 淡い緑 (#6DAE72) — 植物/成長
- 角丸: R4(≤40px), R8(41-119px), R12(≥120px)
- 余白: 8px グリッド
- テキスト: Noto Sans JP、コントラスト比 4.5:1以上
- UIリファレンスとして readdy_export/ のReact+Tailwindコードを参照すること

## 開発方針

### フェーズ

1. **基盤構築** — プロジェクト作成、依存関係、ナビゲーション骨組み
2. **UI実装** — readdy_exportを参考にCompose画面を実装（通信はモック）
3. **DB実装** — Room エンティティ・DAO・マイグレーション
4. **デバイス通信実装** — Decent Scale → SM-S210i → Gicisky の順
5. **結合・調整** — エラーハンドリング、UI微調整

### worktreeによる複数パターン生成（UI実装フェーズ）

UI実装フェーズでは、git worktreeを使って複数パターンを並行生成する。
同じ仕様・同じUIリファレンスから、異なるアプローチのCompose実装を比較するため。

```bash
git worktree add ../sodatter-ui-a feature/ui-pattern-a
git worktree add ../sodatter-ui-b feature/ui-pattern-b
git worktree add ../sodatter-ui-c feature/ui-pattern-c
```

各パターンで意図的にバリエーションを出す観点:
- Composable関数の分割粒度
- UiStateの構造設計
- readdy_exportコードの解釈・変換アプローチ

最終的に最も良いパターンをmainにマージする。