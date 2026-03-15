---
name: database-design
description: Room データベース、Entity、DAO、テーブル定義、クエリ、データ永続化の実装時に使用。
---

# データベース設計

詳細なテーブル定義・リレーション・主要クエリは @specs/DATABASE.md を参照。

## 概要

- devices (4台固定、アプリ初回起動時にシード)
- cultivations (栽培記録、装置ごとのサイクル)
- growth_photos (生育写真、0枚以上)
- device_settings (BT機器・ESP32のKey-Value設定)

## 重要な制約

- 1装置にアクティブな栽培記録は最大1件 (is_active = true は device_id ごとに1件)
- 日付は epoch millis (Long) で保存
- 写真はURIで保存（Bitmapを直接DBに入れない）
