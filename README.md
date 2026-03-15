# Sodatter-BT

家庭菜園の播種→収穫を管理するAndroidアプリ。

## 概要

播種・栽培・収穫の記録管理に加え、BT/BLE機器・電子ペーパータグと連携する家庭菜園管理アプリ。

- **Decent Scale** (BLE): 収穫時の重量計測
- **スター精密 SM-S210i** (Bluetooth): 栽培ラベル印刷
- **Gicisky 電子ペーパータグ** (HTTP/ESP32): 装置ごとの栽培情報表示

## セットアップ

### 前提条件

- Android Studio Ladybug 以降
- JDK 17
- Android SDK (minSdk 31 / targetSdk 35)

### ビルド手順

```bash
cd android
./gradlew assembleDebug
```

## ディレクトリ構成

```
sodatter-bt/
├── .claude/          # Claude Code設定（hooks, skills）
├── specs/            # アプリ仕様書
│   ├── SPEC.md       # 画面・機能仕様
│   ├── DEVICES.md    # デバイス通信仕様
│   └── DATABASE.md   # DBスキーマ仕様
├── readdy_export/    # UIリファレンス（React + Tailwind）
├── android/          # Androidプロジェクト本体
├── CLAUDE.md         # Claude Code用プロジェクト指示
└── README.md
```

## 連携デバイス

| デバイス | 通信方式 | 用途 |
|---|---|---|
| Decent Scale | BLE (GATT Notify) | 収穫重量計測 |
| スター精密 SM-S210i | Bluetooth (StarXpand SDK) | 栽培ラベル印刷 |
| Gicisky 電子ペーパータグ | HTTP multipart (ESP32経由) | 装置ラベル表示 |

## ライセンス

Private
