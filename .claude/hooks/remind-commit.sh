#!/usr/bin/env bash
set -euo pipefail

# ClaudeCodeが応答を完了した時点で、未コミットの変更があれば警告する
# android/ 配下の変更のみチェック（specs等の変更は無視）

# gitリポジトリでなければスキップ
if ! git rev-parse --is-inside-work-tree &>/dev/null; then
  exit 0
fi

# android/ 配下に未コミットの変更があるかチェック
CHANGED=$(git status --porcelain android/ 2>/dev/null || true)

if [ -n "$CHANGED" ]; then
  COUNT=$(echo "$CHANGED" | wc -l | tr -d ' ')
  echo "{\"decision\": \"block\", \"reason\": \"android/ 配下に未コミットの変更が ${COUNT} 件あります。実装の区切りごとにコミットしてください。形式: type(scope): 日本語の説明\"}"
else
  echo "{}"
fi

exit 0