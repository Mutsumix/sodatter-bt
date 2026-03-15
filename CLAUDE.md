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

## ミスの記録

（開発中に発生したミスをここに追記していく）