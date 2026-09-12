#!/usr/bin/env bash
# Move the BPMN editor feature between this repository and an offline (air-gapped) checkout of
# MedPortal.
#
#   scripts/sync-bpmn.sh export                      # here, online: build a portable bundle
#   scripts/sync-bpmn.sh apply --bundle B --target T # there, offline: install it
#
# Why a bundle and not a `git fetch`: the offline project has no route to github.com, so the
# transport has to be a single file that can be carried across. The bundle is self-describing —
# it holds the payload, a sha256 manifest, the dependency list, and a copy of this script — so
# `apply` needs nothing from the network and nothing from this repository.
#
# What `apply` will and will not touch, and why the line is drawn there:
#
#   PAYLOAD  Files that belong to the feature and to nothing else (the whole `bpmn-editor/`
#            module, its icon assets, its tests). The offline copy of these has no information
#            the bundle lacks, so they are replaced wholesale — after a backup. The module
#            directory is removed before the copy, so a file deleted upstream does not survive
#            as a stale import.
#
#   WIRING   Files the feature only *reaches into* — the router, the shell layout, the Flow
#            entity and screens, the Liquibase changelog. The offline project owns these and may
#            carry local changes, so they are never overwritten. `apply` greps each one for the
#            marker that proves the wiring is present and reports what is missing, with the
#            upstream copy staged next to the report to diff against.
#
# Everything is idempotent: a second `apply` of the same bundle re-copies identical bytes,
# re-reports the same wiring state, and adds no duplicate dependency.
set -euo pipefail

SCRIPT_NAME="$(basename "$0")"

# ---------------------------------------------------------------------------
# Inventory. Both lists are repository-relative paths; WIRING entries carry the fixed string
# that proves the wiring is in place, after a `|`.
# ---------------------------------------------------------------------------

PAYLOAD_PATHS=(
  "src/main/webapp/app/bpmn-editor"
  "src/main/webapp/content/bpmn-icons"
  "src/main/webapp/content/images/bpmn-icon.png"
  "src/main/webapp/content/images/bpmn-icon.svg"
  "e2e/playwright/bpmn-editor.e2e.spec.ts"
  "e2e/playwright/bpmn-custom-icons.e2e.spec.ts"
  "e2e/playwright/flow-bpmn-editor.e2e.spec.ts"
  "src/test/java/com/behsa/medportal/web/rest/FlowResourceBpmnParserIT.java"
  "src/test/java/com/behsa/medportal/web/rest/CustomIconFlowFixture.java"
)

# Directories under PAYLOAD_PATHS that are replaced rather than merged. A merge would leave
# files that upstream deleted, and a dangling import fails the build in a way that reads as a
# bug in the new code.
PAYLOAD_REPLACE_DIRS=(
  "src/main/webapp/app/bpmn-editor"
)

WIRING_PATHS=(
  'src/main/webapp/app/app-routing.module.ts|bpmn-editor/bpmn-editor.module'
  'src/main/webapp/app/layouts/main/main.component.ts|/bpmn-editor'
  'src/main/webapp/app/layouts/main/main.component.scss|.fullscreen-mode'
  'src/main/webapp/app/entities/flow/service/flow.service.ts|xmlTemp'
  'src/main/webapp/app/entities/flow/list/flow.component.ts|/bpmn-editor'
  'src/main/webapp/app/entities/flow/list/flow.component.html|bpmn-icon.png'
  'src/main/webapp/app/entities/flow/new/flow-new.component.ts|bpmnXml'
  'src/main/webapp/app/entities/flow/new/flow-new.component.html|bpmnXml'
  'src/main/java/com/behsa/medportal/domain/FlowEntity.java|columnDefinition = "clob"'
  'src/main/java/com/behsa/medportal/web/rest/FlowResource.java|bpmnParserActive'
  'src/main/resources/config/liquibase/changelog/20260711_002_domain_schema.xml|name="flow" type="${clobType}"'
  'e2e/playwright/support/medportal-fixtures.ts|mockApi'
)

# Runtime dependencies the module imports. Versions are read from this repository's package.json
# at export time rather than hard-coded here, so the bundle cannot drift from the source tree.
BPMN_DEPS=(
  "bpmn-js"
  "bpmn-js-properties-panel"
  "bpmn-moddle"
  "camunda-bpmn-moddle"
  "diagram-js"
  "diagram-js-minimap"
)

usage() {
  # NOTE: this heredoc is unquoted so $SCRIPT_NAME expands. That also makes backticks and $( )
  # run as commands, so neither belongs in the text below — use plain quotes. The guard just
  # above the subcommand dispatch at the bottom of this file enforces it on every run.
  cat <<USAGE
$SCRIPT_NAME — carry the BPMN editor feature to an offline MedPortal checkout.

  $SCRIPT_NAME export [--out DIR]
      Run inside this repository, on a machine that has the source tree. Writes
      med-bpmn-bundle-<sha>-<date>.tar.gz (default: target/).

  $SCRIPT_NAME apply (--bundle FILE.tar.gz | --from CHECKOUT) --target DIR
                     [--dry-run] [--fix-wiring]
      Install into DIR, the root of the target MedPortal checkout (the directory holding
      package.json and angular.json).

      --bundle       a bundle built by 'export', for a machine with no route to the source.
      --from         a checkout of this repository already on this machine — same checks,
                     no tar round-trip.
      --dry-run      report every action without writing anything.
      --fix-wiring   also close the wiring gaps this script knows how to close safely,
                     backing up each file it rewrites. Off by default: those files belong
                     to the target. Anything it cannot fix deterministically it reports
                     instead, as usual.

Exit status: 0 when the payload is in place and every wiring marker is present; 3 when the
payload was installed but wiring is missing (the report names each one); 1 on error.
USAGE
}

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | cut -d' ' -f1
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$1" | cut -d' ' -f1
  else
    fail "neither sha256sum nor shasum is available; cannot verify the bundle"
  fi
}

# ---------------------------------------------------------------------------
# export
# ---------------------------------------------------------------------------

# Stage everything a bundle carries, from a checkout of this repository. `export` tars the
# result; `apply --from` reads it in place, so both paths see byte-identical input and a single
# manifest check covers them.
_stage_from_repo() {
  local root="$1" stage="$2"

  [[ -d $root/src/main/webapp/app/bpmn-editor ]] ||
    fail "$root is not a checkout of this repository: src/main/webapp/app/bpmn-editor is missing"

  rm -rf "$stage"
  mkdir -p "$stage/payload" "$stage/wiring-reference"

  echo "Staging payload..."
  local path
  for path in "${PAYLOAD_PATHS[@]}"; do
    [[ -e $root/$path ]] || fail "payload path is missing from the source: $path"
    mkdir -p "$stage/payload/$(dirname "$path")"
    cp -R "$root/$path" "$stage/payload/$(dirname "$path")/"
  done

  echo "Staging wiring reference..."
  local entry marker
  for entry in "${WIRING_PATHS[@]}"; do
    path="${entry%%|*}"
    marker="${entry#*|}"
    [[ -e $root/$path ]] || fail "wiring path is missing from the source: $path"
    grep -qF -- "$marker" "$root/$path" ||
      fail "wiring marker '$marker' is no longer in $path — update the inventory in $SCRIPT_NAME"
    mkdir -p "$stage/wiring-reference/$(dirname "$path")"
    cp "$root/$path" "$stage/wiring-reference/$path"
  done

  # Dependency versions, read from package.json so they cannot drift from the tree being shipped.
  echo "Reading dependency versions..."
  : > "$stage/BPMN_DEPENDENCIES"
  local dep version
  for dep in "${BPMN_DEPS[@]}"; do
    version="$(sed -n "s/^[[:space:]]*\"$dep\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\".*/\1/p" "$root/package.json" | head -1)"
    [[ -n $version ]] || fail "dependency $dep is not in the source package.json"
    printf '%s\t%s\n' "$dep" "$version" >> "$stage/BPMN_DEPENDENCIES"
  done

  {
    echo "source-repo: $(git -C "$root" config --get remote.origin.url 2>/dev/null || echo unknown)"
    echo "source-branch: $(git -C "$root" rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown)"
    echo "source-commit: $(git -C "$root" rev-parse HEAD 2>/dev/null || echo unknown)"
    echo "exported-at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  } > "$stage/PROVENANCE"

  cp "$0" "$stage/sync-bpmn.sh"
  chmod +x "$stage/sync-bpmn.sh"

  echo "Writing manifest..."
  ( cd "$stage" && find payload wiring-reference BPMN_DEPENDENCIES PROVENANCE -type f | LC_ALL=C sort ) > "$stage/.files"
  : > "$stage/MANIFEST"
  local file
  while IFS= read -r file; do
    printf '%s  %s\n' "$(sha256_of "$stage/$file")" "$file" >> "$stage/MANIFEST"
  done < "$stage/.files"
  rm -f "$stage/.files"
}

cmd_export() {
  local out_dir="target"
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --out) out_dir="${2:-}"; [[ -n $out_dir ]] || fail "--out needs a directory"; shift 2 ;;
      -h|--help) usage; exit 0 ;;
      *) fail "unknown option for export: $1" ;;
    esac
  done

  local root stage sha date bundle
  root="$(cd "$(dirname "$0")/.." && pwd)"
  sha="$(git -C "$root" rev-parse --short HEAD 2>/dev/null || echo nogit)"
  date="$(date +%Y%m%d)"
  stage="$out_dir/bpmn-bundle-stage"
  bundle="$out_dir/med-bpmn-bundle-$sha-$date.tar.gz"

  mkdir -p "$out_dir"
  _stage_from_repo "$root" "$stage"

  tar -czf "$bundle" -C "$stage" .
  rm -rf "$stage"

  local count
  count="$(tar -tzf "$bundle" | grep -c '^\./payload/.*[^/]$' || true)"
  echo
  echo "Bundle: $bundle"
  echo "  payload files: $count"
  echo "  sha256: $(sha256_of "$bundle")"
  echo
  echo "Carry it to the offline machine, then run there (one command, from the bundle's directory):"
  echo
  echo "  mkdir -p bpmn-bundle && tar -xzf $(basename "$bundle") -C bpmn-bundle \\"
  echo "    && bpmn-bundle/sync-bpmn.sh apply --bundle $(basename "$bundle") --target /path/to/offline/med"
}

# ---------------------------------------------------------------------------
# apply
# ---------------------------------------------------------------------------

cmd_apply() {
  local bundle="" from="" target="" dry_run=0 fix_wiring=0
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --bundle) bundle="${2:-}"; [[ -n $bundle ]] || fail "--bundle needs a file"; shift 2 ;;
      --from) from="${2:-}"; [[ -n $from ]] || fail "--from needs a directory"; shift 2 ;;
      --target) target="${2:-}"; [[ -n $target ]] || fail "--target needs a directory"; shift 2 ;;
      --dry-run) dry_run=1; shift ;;
      --fix-wiring) fix_wiring=1; shift ;;
      -h|--help) usage; exit 0 ;;
      *) fail "unknown option for apply: $1" ;;
    esac
  done

  [[ -n $bundle || -n $from ]] || fail "one of --bundle or --from is required"
  [[ -z $bundle || -z $from ]] || fail "--bundle and --from are mutually exclusive"
  [[ -n $target ]] || fail "--target is required"
  [[ -d $target ]] || fail "target is not a directory: $target"
  [[ -z $bundle || -f $bundle ]] || fail "bundle not found: $bundle"
  [[ -z $from || -d $from ]] || fail "source checkout not found: $from"

  target="$(cd "$target" && pwd)"
  [[ -z $bundle ]] || bundle="$(cd "$(dirname "$bundle")" && pwd)/$(basename "$bundle")"
  [[ -z $from ]] || from="$(cd "$from" && pwd)"

  # Applying a checkout over itself would delete the module and copy it back from a staging
  # directory built out of the same files. Refuse rather than rely on that working.
  [[ $from != "$target" ]] || fail "--from and --target are the same directory"

  # Preflight. This is the guard that keeps the bundle from being unpacked over something that
  # is not MedPortal — an Angular module tree dropped into an unrelated project produces a
  # hundred broken imports and no useful error.
  echo "== Preflight =="
  [[ -f $target/package.json ]] || fail "$target has no package.json — that is not a MedPortal checkout"
  [[ -f $target/angular.json ]] || fail "$target has no angular.json — that is not an Angular checkout"
  grep -qF '"med-portal"' "$target/angular.json" ||
    fail "$target/angular.json does not declare the med-portal project; refusing to write into it"
  [[ -d $target/src/main/webapp/app ]] ||
    fail "$target/src/main/webapp/app is missing — the JHipster layout this bundle assumes is not there"
  echo "  target looks like a MedPortal checkout: OK"

  local work source_label
  work="$(mktemp -d)"
  # shellcheck disable=SC2064
  trap "rm -rf '$work'" EXIT

  if [[ -n $bundle ]]; then
    source_label="$(basename "$bundle")"
    tar -xzf "$bundle" -C "$work"
    [[ -f $work/MANIFEST ]] ||
      fail "bundle has no MANIFEST — it was not produced by $SCRIPT_NAME export"
  else
    # No tar round-trip when the source is already on this machine. The manifest is still
    # written and still verified below, so this path is checked exactly like the bundle one.
    source_label="$from"
    echo "== Staging from $from =="
    _stage_from_repo "$from" "$work"
    echo
  fi

  echo "== Verifying payload =="
  local expected file actual bad=0
  while read -r expected file; do
    [[ -f $work/$file ]] || { echo "  MISSING: $file"; bad=1; continue; }
    actual="$(sha256_of "$work/$file")"
    [[ $actual == "$expected" ]] || { echo "  CHECKSUM MISMATCH: $file"; bad=1; }
  done < "$work/MANIFEST"
  [[ $bad -eq 0 ]] || fail "payload failed verification; do not apply it"
  echo "  $(wc -l < "$work/MANIFEST") files verified against the manifest: OK"
  echo
  sed 's/^/  /' "$work/PROVENANCE"
  echo

  local stamp report_dir
  stamp="$(date +%Y%m%d-%H%M%S)"
  report_dir="$target/bpmn-sync-$stamp"
  local backup_dir="$report_dir/backup"
  local report="$report_dir/REPORT.md"

  if [[ $dry_run -eq 1 ]]; then
    echo "== DRY RUN — nothing will be written =="
  else
    mkdir -p "$backup_dir"
    cp "$work/PROVENANCE" "$report_dir/PROVENANCE"
    cp -R "$work/wiring-reference" "$report_dir/wiring-reference"
  fi

  # --- payload ---------------------------------------------------------------
  echo "== Payload =="
  local path rel
  for path in "${PAYLOAD_PATHS[@]}"; do
    if [[ -e $target/$path ]]; then
      echo "  replace  $path"
      if [[ $dry_run -eq 0 ]]; then
        mkdir -p "$backup_dir/$(dirname "$path")"
        cp -R "$target/$path" "$backup_dir/$(dirname "$path")/"
      fi
    else
      echo "  add      $path"
    fi

    if [[ $dry_run -eq 0 ]]; then
      # Replace, not merge, for the directories that are wholly ours.
      for rel in "${PAYLOAD_REPLACE_DIRS[@]}"; do
        [[ $path == "$rel" ]] && rm -rf "$target/$path"
      done
      mkdir -p "$target/$(dirname "$path")"
      cp -R "$work/payload/$path" "$target/$(dirname "$path")/"
    fi
  done

  # --- dependencies ----------------------------------------------------------
  echo
  echo "== Dependencies =="
  local dep want have dep_actions=()
  while IFS=$'\t' read -r dep want; do
    have="$(sed -n "s/^[[:space:]]*\"$dep\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\".*/\1/p" "$target/package.json" | head -1)"
    if [[ -z $have ]]; then
      echo "  add      $dep $want"
      dep_actions+=("$dep|$want|add")
    elif [[ $have == "$want" ]]; then
      echo "  present  $dep $have"
    else
      # Never rewrite a version the offline project chose. A silent bump is a change to a
      # dependency tree nobody asked to change, and the module works against what it declares.
      echo "  DIFFERS  $dep: offline has $have, bundle expects $want (left as-is — decide deliberately)"
      dep_actions+=("$dep|$want|differs:$have")
    fi
  done < "$work/BPMN_DEPENDENCIES"

  local adds=0
  for dep in "${dep_actions[@]}"; do
    [[ ${dep##*|} == add ]] && adds=$((adds + 1))
  done

  if [[ $adds -gt 0 && $dry_run -eq 0 ]]; then
    cp "$target/package.json" "$backup_dir/package.json"
    if _patch_package_json "$target/package.json" "$work/BPMN_DEPENDENCIES"; then
      echo "  package.json updated ($adds added)"
    else
      echo "  package.json NOT updated — no node or python3 on this machine."
      echo "  Add these to \"dependencies\" by hand:"
      for dep in "${dep_actions[@]}"; do
        [[ ${dep##*|} == add ]] && printf '    "%s": "%s",\n' "${dep%%|*}" "$(cut -d'|' -f2 <<<"$dep")"
      done
    fi
  fi

  # --- wiring ----------------------------------------------------------------
  echo
  echo "== Wiring =="
  local entry marker missing=()
  for entry in "${WIRING_PATHS[@]}"; do
    path="${entry%%|*}"
    marker="${entry#*|}"
    if [[ ! -f $target/$path ]]; then
      echo "  ABSENT   $path (file does not exist offline)"
      missing+=("$path|$marker|file missing")
    elif grep -qF -- "$marker" "$target/$path"; then
      echo "  ok       $path"
    else
      echo "  TODO     $path — does not contain: $marker"
      missing+=("$path|$marker|marker missing")
    fi
  done

  # --- optional wiring fix ---------------------------------------------------
  # Off by default: these files belong to the target, and a rewrite it did not ask for is how a
  # local change gets lost. `--fix-wiring` opts in, and only the one gap below is ever touched.
  if [[ $fix_wiring -eq 1 && ${#missing[@]} -gt 0 ]]; then
    echo
    echo "== Fixing wiring (--fix-wiring) =="
    local fixed=()
    local entry path companion
    for entry in "${missing[@]}"; do
      path="${entry%%|*}"

      # Back up before rewriting, and back up the template alongside the stylesheet: the two
      # move together, so restoring one without the other would leave the shell inconsistent.
      if [[ $dry_run -eq 0 ]]; then
        for companion in "$path" "$(_fix_wiring_companion "$path")"; do
          [[ -n $companion && -f $target/$companion ]] || continue
          mkdir -p "$backup_dir/$(dirname "$companion")"
          cp "$target/$companion" "$backup_dir/$companion"
        done
      fi

      if _fix_wiring_path "$target" "$work/wiring-reference" "$path" "$dry_run"; then
        fixed+=("$path")
      fi
    done

    # Re-check, so what is reported is the state on disk rather than what the fixer intended.
    if [[ ${#fixed[@]} -gt 0 && $dry_run -eq 0 ]]; then
      local still=()
      for entry in "${missing[@]}"; do
        path="${entry%%|*}"
        marker="${entry#*|}"; marker="${marker%|*}"
        if [[ -f $target/$path ]] && grep -qF -- "$marker" "$target/$path"; then
          echo "  now ok   $path"
        else
          echo "  STILL    $path"
          still+=("$entry")
        fi
      done
      missing=("${still[@]+"${still[@]}"}")
    fi
  fi

  # --- report ----------------------------------------------------------------
  if [[ $dry_run -eq 0 ]]; then
    _write_report "$report" "$stamp" "$bundle" "$work" "${missing[@]+"${missing[@]}"}"
    echo
    echo "Backup:  $backup_dir"
    echo "Report:  $report"
  fi

  echo
  # Dry run has written nothing, so it must not point at a backup or a report that does not exist.
  local verb="Payload installed"
  [[ $dry_run -eq 1 ]] && verb="Payload would be installed"

  if [[ ${#missing[@]} -gt 0 ]]; then
    if [[ $dry_run -eq 1 ]]; then
      echo "$verb. ${#missing[@]} wiring item(s) would still need a hand; re-run without --dry-run"
      echo "to write the backup, the report and the upstream copies to diff against."
    else
      echo "$verb. ${#missing[@]} wiring item(s) still need a hand — see the report."
      echo "Upstream copies to diff against are in $report_dir/wiring-reference/."
    fi
    exit 3
  fi

  echo "$verb, and every wiring marker is present."
  if [[ $dry_run -eq 0 ]]; then
    echo "Next: npm install && npm run lint && npm run test -- --run app/bpmn-editor"
  fi
}

# The second file a wiring fix has to touch, if any. Empty when the fix is self-contained.
_fix_wiring_companion() {
  case "$1" in
    src/main/webapp/app/layouts/main/main.component.scss)
      echo "src/main/webapp/app/layouts/main/main.component.html" ;;
    *) echo "" ;;
  esac
}

# Apply the one wiring gap this script knows how to close, and refuse anything else.
#
# The gap: the shell marks the editor route with a class, and the branch this feature comes from
# renamed it from `full-screen-mode` to `fullscreen-mode` along with the measured rule block
# behind it (see "The 47px, fixed" in the module README). A checkout that predates that carries
# the old name in BOTH `main.component.html` and `main.component.scss`, so the two have to move
# together — renaming one alone leaves the shell with no matching rule at all, which is worse
# than not touching it.
#
# Returns 0 when it changed something, 1 when it did not. Idempotent: a target already carrying
# the new name reports nothing to do.
_fix_wiring_path() {
  local target="$1" ref="$2" path="$3" dry="$4"

  case "$path" in
    src/main/webapp/app/layouts/main/main.component.scss)
      local html="src/main/webapp/app/layouts/main/main.component.html"

      [[ -f $target/$path && -f $target/$html ]] || {
        echo "  skip     $path — the layout files are not both there; fix by hand"
        return 1
      }
      grep -qF 'full-screen-mode' "$target/$path" || {
        echo "  skip     $path — no 'full-screen-mode' block to rename; fix by hand from the reference copy"
        return 1
      }

      if [[ $dry -eq 1 ]]; then
        echo "  would fix $path and $html (rename the class, take the upstream block)"
        return 0
      fi

      _rewrite_fullscreen_block "$target/$path" "$ref/$path" "$target/$html" || {
        echo "  skip     $path — could not rewrite it safely; fix by hand from the reference copy"
        return 1
      }
      echo "  fixed    $path and $html"
      return 0
      ;;
    *)
      echo "  skip     $path — no automatic fix for this one; see the report"
      return 1
      ;;
  esac
}

# Swap the target's `.app-root.full-screen-mode { ... }` block for the reference's
# `.fullscreen-mode { ... }` block, brace-matched rather than line-guessed, and rename the class
# binding in the template. Both files are written only if both edits resolve.
_rewrite_fullscreen_block() {
  local scss="$1" ref_scss="$2" html="$3"
  local runner=""

  if command -v python3 >/dev/null 2>&1; then
    runner=python3
  elif command -v node >/dev/null 2>&1; then
    runner=node
  else
    return 1
  fi

  if [[ $runner == python3 ]]; then
    python3 - "$scss" "$ref_scss" "$html" <<'PY_FIX'
import sys

scss_path, ref_path, html_path = sys.argv[1:4]


def block(text, opener):
    """The text of `opener { ... }`, brace-matched, plus the comment lines above it."""
    start = text.find(opener)
    if start == -1:
        return None, None, None
    # Walk back over the comment block that documents the rule.
    head = start
    lines = text[:start].split("\n")
    # text[:start] ends on the newline before the rule, so the split leaves a trailing "" that
    # would stop the walk-back at once and silently drop the comments documenting the rule.
    if lines and lines[-1] == "":
        lines.pop()
    keep = []
    while lines and lines[-1].lstrip().startswith("//"):
        keep.insert(0, lines.pop())
    if keep:
        head = len("\n".join(lines))
        head += 1 if lines else 0
    depth = 0
    i = text.index("{", start)
    for j in range(i, len(text)):
        if text[j] == "{":
            depth += 1
        elif text[j] == "}":
            depth -= 1
            if depth == 0:
                return head, j + 1, text[head:j + 1]
    return None, None, None

with open(ref_path, encoding="utf-8") as fh:
    ref = fh.read()
_, _, replacement = block(ref, ".fullscreen-mode {")
if replacement is None:
    sys.exit(1)

with open(scss_path, encoding="utf-8") as fh:
    scss = fh.read()
start, end, _ = block(scss, ".app-root.full-screen-mode {")
if start is None:
    sys.exit(1)

with open(html_path, encoding="utf-8") as fh:
    html = fh.read()
binding = '[class.full-screen-mode]="fullScreen"'
if binding not in html:
    sys.exit(1)

with open(scss_path, "w", encoding="utf-8") as fh:
    fh.write(scss[:start] + replacement + scss[end:])
with open(html_path, "w", encoding="utf-8") as fh:
    fh.write(html.replace(binding, '[class.fullscreen-mode]="fullScreen"', 1))
PY_FIX
    return $?
  fi

  node - "$scss" "$ref_scss" "$html" <<'NODE_FIX'
const fs = require('fs');
const [scssPath, refPath, htmlPath] = process.argv.slice(2);

function block(text, opener) {
  const start = text.indexOf(opener);
  if (start === -1) return null;
  const lines = text.slice(0, start).split('\n');
  // Drop the trailing '' the split leaves before the rule, or the walk-back stops at once and
  // the comments documenting the rule are silently lost.
  if (lines.length && lines[lines.length - 1] === '') lines.pop();
  const keep = [];
  while (lines.length && lines[lines.length - 1].trimStart().startsWith('//')) keep.unshift(lines.pop());
  const head = keep.length ? lines.join('\n').length + (lines.length ? 1 : 0) : start;
  let depth = 0;
  for (let j = text.indexOf('{', start); j < text.length; j++) {
    if (text[j] === '{') depth++;
    else if (text[j] === '}' && --depth === 0) return { head, end: j + 1, text: text.slice(head, j + 1) };
  }
  return null;
}

const replacement = block(fs.readFileSync(refPath, 'utf8'), '.fullscreen-mode {');
if (!replacement) process.exit(1);
const scss = fs.readFileSync(scssPath, 'utf8');
const found = block(scss, '.app-root.full-screen-mode {');
if (!found) process.exit(1);
const html = fs.readFileSync(htmlPath, 'utf8');
const binding = '[class.full-screen-mode]="fullScreen"';
if (!html.includes(binding)) process.exit(1);

fs.writeFileSync(scssPath, scss.slice(0, found.head) + replacement.text + scss.slice(found.end));
fs.writeFileSync(htmlPath, html.replace(binding, '[class.fullscreen-mode]="fullScreen"'));
NODE_FIX
  return $?
}

# Add the missing BPMN dependencies to package.json, in place, through a real JSON parser.
# A sed-based insertion into package.json is how a lockfile and a trailing comma get broken.
_patch_package_json() {
  local pkg="$1" deps_file="$2"

  if command -v node >/dev/null 2>&1; then
    node - "$pkg" "$deps_file" <<'NODE_EOF'
const fs = require('fs');
const [pkgPath, depsPath] = process.argv.slice(2);
const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
pkg.dependencies = pkg.dependencies || {};
for (const line of fs.readFileSync(depsPath, 'utf8').split('\n')) {
  if (!line.trim()) continue;
  const [name, version] = line.split('\t');
  if (!(name in pkg.dependencies)) pkg.dependencies[name] = version;
}
pkg.dependencies = Object.fromEntries(Object.entries(pkg.dependencies).sort(([a], [b]) => (a < b ? -1 : 1)));
fs.writeFileSync(pkgPath, JSON.stringify(pkg, null, 2) + '\n');
NODE_EOF
    return 0
  fi

  if command -v python3 >/dev/null 2>&1; then
    python3 - "$pkg" "$deps_file" <<'PY_EOF'
import json, sys
pkg_path, deps_path = sys.argv[1], sys.argv[2]
with open(pkg_path, encoding='utf-8') as fh:
    pkg = json.load(fh)
deps = pkg.setdefault('dependencies', {})
with open(deps_path, encoding='utf-8') as fh:
    for line in fh:
        if not line.strip():
            continue
        name, version = line.rstrip('\n').split('\t')
        deps.setdefault(name, version)
pkg['dependencies'] = dict(sorted(deps.items()))
with open(pkg_path, 'w', encoding='utf-8') as fh:
    json.dump(pkg, fh, indent=2, ensure_ascii=False)
    fh.write('\n')
PY_EOF
    return 0
  fi

  return 1
}

_write_report() {
  local report="$1" stamp="$2" bundle="$3" work="$4"
  shift 4

  {
    echo "# BPMN sync report — $stamp"
    echo
    echo "Bundle: \`$(basename "$bundle")\`"
    echo
    echo '```'
    cat "$work/PROVENANCE"
    echo '```'
    echo
    echo "## Payload installed"
    echo
    local path
    for path in "${PAYLOAD_PATHS[@]}"; do
      echo "- \`$path\`"
    done
    echo
    echo "The previous contents of anything replaced are under \`backup/\` next to this file."
    echo
    echo "## Wiring"
    echo
    if [[ $# -eq 0 ]]; then
      echo "Every wiring marker was already present. Nothing to do."
    else
      echo "The BPMN module reaches into the files below, and this script does not edit them —"
      echo "the offline project owns them and may carry local changes. Each entry names the"
      echo "marker that was not found; the upstream copy is under \`wiring-reference/\` at the"
      echo "same path, so \`diff\` shows exactly what to carry over."
      echo
      local entry p m why
      for entry in "$@"; do
        p="${entry%%|*}"
        why="${entry##*|}"
        m="${entry#*|}"; m="${m%|*}"
        echo "### \`$p\` — $why"
        echo
        echo "Missing marker: \`$m\`"
        echo
        echo '```sh'
        echo "diff -u \"$p\" \"$(basename "$(dirname "$report")")/wiring-reference/$p\""
        echo '```'
        echo
      done
    fi
    echo "## Then"
    echo
    echo '```sh'
    echo "npm install                       # the BPMN packages must be reachable offline"
    echo "npm run lint"
    echo "npm run test -- --run app/bpmn-editor"
    echo "./mvnw -Dtest=FlowResourceBpmnParserIT test"
    echo '```'
  } > "$report"
}

# ---------------------------------------------------------------------------

# The usage heredoc is unquoted (it interpolates $SCRIPT_NAME), so a backtick or $( ) in that
# text would execute. That is how `--help` once printed the whole environment. Checked here
# against this file's own source, so it cannot regress silently.
if grep -n '`\|\$(' <<<"$(sed -n '/^usage() {/,/^}/p' "$0" | grep -v '^\s*#')" >/dev/null; then
  fail "usage() contains command substitution; use plain quotes in the help text"
fi

case "${1:-}" in
  export) shift; cmd_export "$@" ;;
  apply) shift; cmd_apply "$@" ;;
  -h|--help|help|"") usage ;;
  *) usage >&2; fail "unknown subcommand: $1" ;;
esac
