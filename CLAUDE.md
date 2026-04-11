# Sodatter-BT

家庭菜園の播種→収穫を管理するAndroidアプリ。BT機器・電子ペーパーと連携。

## プロジェクト構造

- `specs/` — アプリ仕様書（SPEC.md, DEVICES.md, DATABASE.md）
- `readdy_export/` — UIリファレンス（React + Tailwind、デジタル庁デザインシステム準拠）
- `android/` — Androidプロジェクト本体

## 重要ルール

- Androidのコード編集は `android/` 配下でのみ行うこと
- `android/` の外にKotlinファイル、gradle設定、リソースファイルを作らないこと
- UIの実装は `readdy_export/` のReactコードを参照してCompose変換すること
- 仕様の確認は `specs/` 配下のファイルを読むこと

## 技術スタック（厳守）

- Kotlin 2.0+ / Jetpack Compose (BOM最新安定版)
- Compose Compiler Gradle Plugin（composeOptionsブロックは使わない）
- Navigation Compose 型安全ルート（Kotlin Serialization）
- Gradle Version Catalog (libs.versions.toml)
- MVVM (Screen / ViewModel / UiState)
- Room / Hilt / Coroutines + Flow
- minSdk 31 / targetSdk 35

## Git

- コミットメッセージはConventional Commits形式、説明は日本語（Hookで強制される）
- 形式: `type(scope): 日本語の説明`
- 例: `feat(home): 装置グリッドレイアウトを実装`
- 以下の単位でこまめにコミットすること:
  - 1画面のUI実装が完了したとき
  - 1つのデバイス通信機能が動いたとき
  - DBのEntity/DAO追加が完了したとき
  - バグ修正1件ごと
- 複数画面や複数機能をまとめて1コミットにしないこと

## 実装ルール

- 1つの機能を実装したら、必ず実機テスト可能な状態まで持っていく
- テストに実機やデバイスが必要な場合、コードを書く前にユーザーにデバイスの準備状況を確認する
- 「ビルドが通った」はテスト完了ではない。実際に動作確認するまで次の機能に進まない
- テストできない環境の場合は、その旨をユーザーに伝えて判断を仰ぐ
- 外部デバイスとの通信（HTTP API、BLE等）は、コードを書く前にcurl等で実際にコマンドを実行して動作確認すること。推測でコードを書いて余計な手戻りを発生させない

## ミスの記録

- テストをスキップして複数機能を一気に実装し、問題を積み重ねた
- BLEランタイム権限チェック漏れ（BLUETOOTH_SCAN/CONNECT）でクラッシュ
- LabelPrintViewModelがgetHarvestedCultivationsのみ参照し、未収穫時にcultivation=nullでサイレントリターン
- StarXpand SDKのCJKクラス名が大文字小文字違い（CJKCharacterType → CjkCharacterType）
- SM-S210iの日本語印刷はstyleSecondPriorityCharacterEncoding(CharacterEncodingType.Japanese)が正解
- 設定画面のバージョン表示をハードコード（"1.0.0"）していたため、バージョン更新時に追従しなかった。BuildConfig.VERSION_NAMEを使うべき