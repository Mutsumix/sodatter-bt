#!/usr/bin/env bash
set -euo pipefail

# git commitコマンドを検出し、コミットメッセージのフォーマットを強制する
# Conventional Commits (typeは英語、説明は日本語)

INPUT=$(cat)
CMD=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# git commitコマンドかどうか判定
if echo "$CMD" | grep -Eq '^\s*git\s+commit'; then

  # コミットメッセージを抽出 (-m "..." の中身)
  MSG=$(echo "$CMD" | grep -oP '(?<=-m\s["\x27])[^"\x27]+' || true)

  if [ -z "$MSG" ]; then
    # -m なしのコミット（エディタ起動型）は許可
    exit 0
  fi

  # Conventional Commits形式かチェック
  if ! echo "$MSG" | grep -Eq '^(feat|fix|docs|style|refactor|test|chore|build|ci|perf|revert)(\(.+\))?: .+'; then
    echo "コミットメッセージはConventional Commits形式にしてください。" >&2
    echo "形式: type(scope): 日本語の説明" >&2
    echo "type: feat|fix|docs|style|refactor|test|chore|build|ci|perf|revert" >&2
    echo "例: feat(home): 装置グリッドレイアウトを実装" >&2
    echo "例: fix(scale): 重量パースの符号判定を修正" >&2
    exit 2
  fi

  # 説明部分（: の後）に日本語が含まれていなければブロック
  DESC=$(echo "$MSG" | sed -E 's/^[^:]+:\s*//')
  if ! echo "$DESC" | grep -Pq '[\p{Hiragana}\p{Katakana}\p{Han}]'; then
    echo "コミットメッセージの説明部分は日本語で書いてください。" >&2
    echo "例: feat(home): 装置グリッドレイアウトを実装" >&2
    exit 2
  fi
fi

exit 0