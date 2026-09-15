#!/usr/bin/env bash
# Fail if dangerous Unicode bidirectional / invisible control characters appear in source files.
# Persian letters and ZWNJ/ZWJ inside fa i18n files are allowed.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# Always forbidden (Trojan Source / bidi spoofing)
DANGEROUS=$'\u202A|\u202B|\u202C|\u202D|\u202E|\u2066|\u2067|\u2068|\u2069|\u200E|\u200F|\uFEFF'

# Zero-width join controls: forbidden in code, allowed in Persian i18n
ZW=$'\u200B|\u200C|\u200D'

mapfile -t FILES < <(
  git ls-files \
    '*.java' '*.ts' '*.tsx' '*.js' '*.jsx' '*.mjs' '*.cjs' \
    '*.json' '*.yml' '*.yaml' '*.xml' '*.sh' '*.bash' \
    '*.md' '*.html' '*.css' '*.scss' '*.properties' \
    'Dockerfile*' 'pom.xml' '.github/workflows/*' \
    2>/dev/null || true
)

if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "No files to scan"
  exit 0
fi

FOUND=0
for file in "${FILES[@]}"; do
  [[ -f "$file" ]] || continue
  case "$file" in
    package-lock.json|*/package-lock.json) continue ;;
  esac

  if grep -P -n -l "$DANGEROUS" -- "$file" >/dev/null 2>&1; then
    echo "Dangerous bidi/BOM Unicode found in: $file"
    grep -P -n -o "$DANGEROUS" -- "$file" | head -20 || true
    FOUND=1
  fi

  # Allow ZWNJ/ZWJ only in Persian translation resources
  case "$file" in
    *i18n/fa/*|*messages_fa.properties|*fa.json) ;;
    *)
      if grep -P -n -l "$ZW" -- "$file" >/dev/null 2>&1; then
        echo "Unexpected zero-width Unicode found in: $file"
        grep -P -n -o "$ZW" -- "$file" | head -20 || true
        FOUND=1
      fi
      ;;
  esac
done

if [[ "$FOUND" -ne 0 ]]; then
  echo
  echo "Unicode security scan FAILED."
  echo "Allowed: Persian letters; ZWNJ/ZWJ only inside fa i18n files."
  echo "Forbidden: U+202A-E, U+2066-9, U+200E-F, BOM; ZW* outside fa i18n."
  exit 1
fi

echo "Unicode security scan passed (${#FILES[@]} files)."
