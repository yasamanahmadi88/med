#!/usr/bin/env bash
# Fail if dangerous Unicode bidirectional / invisible control characters appear in source files.
# Persian letters and ZWNJ/ZWJ inside fa i18n files are allowed.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# ---------------------------------------------------------------------------
# Patterns are written as explicit UTF-8 byte sequences (\xNN), never as \uHHHH.
#
# This is not a style choice. Bash expands `$'\uHHHH'` using the *current locale's* charset, and
# in the C/POSIX locale it cannot encode a non-ASCII code point, so it silently leaves the literal
# six-character text `\u200C` in the variable. grep then dutifully searches every file for the
# string "\u200C", finds it nowhere, and the scan reports success on a tree full of the very
# characters it exists to reject — including the bidi controls, not just the zero-width ones.
#
# That is exactly what happened: with no LANG/LC_ALL set this script printed
# "Unicode security scan passed (1095 files)" on a tree CI rejected. `$'\xNN'` is a raw byte and
# is expanded identically in every locale, so the patterns below cannot degrade that way. The
# self-test at the bottom of this block enforces it regardless of the mechanism.
#
#   U+202A e2 80 aa   U+202B e2 80 ab   U+202C e2 80 ac   U+202D e2 80 ad   U+202E e2 80 ae
#   U+2066 e2 81 a6   U+2067 e2 81 a7   U+2068 e2 81 a8   U+2069 e2 81 a9
#   U+200E e2 80 8e   U+200F e2 80 8f   U+FEFF ef bb bf
#   U+200B e2 80 8b   U+200C e2 80 8c   U+200D e2 80 8d
# ---------------------------------------------------------------------------

# Always forbidden (Trojan Source / bidi spoofing)
DANGEROUS=$'\xe2\x80\xaa|\xe2\x80\xab|\xe2\x80\xac|\xe2\x80\xad|\xe2\x80\xae|\xe2\x81\xa6|\xe2\x81\xa7|\xe2\x81\xa8|\xe2\x81\xa9|\xe2\x80\x8e|\xe2\x80\x8f|\xef\xbb\xbf'

# Zero-width join controls: forbidden in code, allowed in Persian i18n
ZW=$'\xe2\x80\x8b|\xe2\x80\x8c|\xe2\x80\x8d'

# ---------------------------------------------------------------------------
# Self-test: prove the patterns still match the characters they name, in whatever locale and with
# whatever grep this run actually has. A scan that cannot detect a planted character must fail
# loudly rather than report a clean tree.
# ---------------------------------------------------------------------------
self_test() {
  local dir rc=0
  dir="$(mktemp -d)"
  # shellcheck disable=SC2064
  trap "rm -rf '$dir'" RETURN

  printf 'before \xe2\x80\xae after\n' >"$dir/bidi"       # U+202E RIGHT-TO-LEFT OVERRIDE
  printf 'salam \xe2\x80\x8c jan\n' >"$dir/zw"            # U+200C ZERO WIDTH NON-JOINER
  printf 'plain ascii only\n' >"$dir/clean"

  # The variables must hold raw bytes. If bash left the escape text behind, say so precisely
  # rather than letting the grep below fail for an unexplained reason.
  case "$DANGEROUS$ZW" in
    *'\u'*) echo "self-test: patterns contain literal '\\u' escape text, not characters" >&2; rc=1 ;;
  esac

  grep -P -q "$DANGEROUS" -- "$dir/bidi" || { echo "self-test: DANGEROUS failed to match a planted U+202E" >&2; rc=1; }
  grep -P -q "$ZW" -- "$dir/zw" || { echo "self-test: ZW failed to match a planted U+200C" >&2; rc=1; }
  # Negative cases, so a pattern that matched everything could not pass the two checks above.
  if grep -P -q "$DANGEROUS" -- "$dir/clean"; then
    echo "self-test: DANGEROUS matched a clean ASCII file" >&2
    rc=1
  fi
  if grep -P -q "$ZW" -- "$dir/clean"; then
    echo "self-test: ZW matched a clean ASCII file" >&2
    rc=1
  fi

  return "$rc"
}

if ! self_test; then
  echo
  echo "Unicode security scan ABORTED: its own detection is broken, so a pass would mean nothing."
  echo "Patterns must be raw UTF-8 bytes (\$'\\xNN'), never \$'\\uHHHH', which bash leaves as literal"
  echo "text in the C/POSIX locale."
  exit 1
fi

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
  echo "In code and prose, write the joiner as a \\u200C escape instead of the character."
  exit 1
fi

echo "Unicode security scan passed (${#FILES[@]} files)."
