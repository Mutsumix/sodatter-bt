---
name: ui-reference
description: Jetpack Compose のUI実装、画面デザイン、レイアウト、カラー、スタイリングに関する作業時に使用。Readdyコードからの変換ルールとデジタル庁デザインシステムの方針を提供する。
---

# UIリファレンス

## デザインソース

`readdy_export/` にReact + Tailwind CSSのコードがある。
Compose UIを実装する際はこのコードを読んで構造・色・余白を再現すること。

## デザイン方針（デジタル庁デザインシステム準拠）

- 背景は白基調。色のベタ塗りは禁止
- Primary (#5B8BD4) と Secondary (#6DAE72) は枠線・アイコン・テキストのみに使用
- 角丸: R4 (≤40dp), R8 (41-119dp), R12 (≥120dp)
- 余白: 8dp グリッド (8, 16, 24, 32, 40, 48dp)
- テキスト: コントラスト比 4.5:1 以上
- ボタン: アウトライン型（白背景 + 色付き枠線 + 色付きテキスト）

## Tailwind → Compose 変換ルール

| Tailwind | Compose |
|----------|---------|
| p-4 | Modifier.padding(16.dp) |
| px-6 | Modifier.padding(horizontal = 24.dp) |
| py-3 | Modifier.padding(vertical = 12.dp) |
| rounded-lg | RoundedCornerShape(8.dp) |
| rounded-xl | RoundedCornerShape(12.dp) |
| text-sm | fontSize = 14.sp |
| text-base | fontSize = 16.sp |
| text-lg | fontSize = 18.sp |
| text-xl | fontSize = 20.sp |
| font-medium | fontWeight = FontWeight.Medium |
| font-semibold | fontWeight = FontWeight.SemiBold |
| gap-2 | Arrangement.spacedBy(8.dp) |
| gap-3 | Arrangement.spacedBy(12.dp) |
| gap-4 | Arrangement.spacedBy(16.dp) |
| border | BorderStroke(1.dp, color) |
| border-2 | BorderStroke(2.dp, color) |

## 画面一覧

8画面の詳細は @specs/SPEC.md の「画面構成と遷移」を参照。
