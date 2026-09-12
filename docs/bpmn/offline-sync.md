# Carrying the BPMN editor to the offline project

## Which implementation is ahead, and how that was measured

The offline checkout is **not** the Vue editor. It is this same Angular application on a branch
named `feature/bpmn-vue-angular-parity`, with no `.vue` file anywhere in it — a second attempt at
the same Vue-to-Angular port, made in parallel with the one on `main`.

That makes the comparison a measurement rather than a judgement. Every file in the offline
`bpmn-editor/` was hashed and looked up in this repository's full object history:

|                                               | offline                                                              | here |
| --------------------------------------------- | -------------------------------------------------------------------- | ---- |
| files under `bpmn-editor/`                    | 158                                                                  | 181  |
| of the 63 files present in both but differing | 34 are **byte-identical to an older version in this repo's history** | —    |
| the other 29                                  | independent edits made on the parity branch                          | —    |

The 34 exact matches place the branch point precisely: the offline tree is closest to
**PR #16** (`552ad81`, "Port the BPMN module property forms from the Vue editor"), with 33
differences, and grows steadily more distant from every commit after it. So the offline branch
left `main` at #16 and has since missed PRs #17-#30.

### What the offline checkout is missing

Whole directories and features, not refinements:

- `context-menu/` — the right-click architecture (#18). Element right-click defers to bpmn-js's
  own `bpmn-replace` popup (search, grouping, keyboard navigation); the append menu fires on the
  modeler's event bus. The offline branch wrote its own flat menu instead.
- `custom-icons/` — the whole custom-icon feature (#25): upload an SVG, name it, place it from the
  palette, stored in the diagram's own `bpmn:Definitions/bpmn:extensionElements` so it travels
  with the flow. With `moddle-extensions/customIcons.json`. The offline branch has a leftover
  `styles/custom-icons.scss` and none of the feature.
- `additional-modules/Rules` — start and end events cannot be deleted (#19).
- `module-properties/validation.ts` and `validators.ts` — the four field validators, with Save
  gated on the whole diagram rather than the selected element (#21).
- The toolbar's dialogs and keyboard layer (#20): `xml-preview-dialog`, `custom-icons-dialog`,
  `shortcut-keys-dialog`, `shortcuts.ts`, `toolbar.component.scss`.
- `types/declares/diagram-js-minimap.d.ts`, `components/flow/flow-bpmn-editor.component.scss`.

And the fixes from #22-#30, none of which are in it: the five bugs #24 found (replace menu, grid
background, element sizes, select labels, settings panel), the panel header laid out around an
icon it never draws (#23), the 47px of editor that was unreachable (#27), RTL, the Persian bundle
(#28, #29), and the `FlowEntity` CLOB (#26).

Two of those are worth naming, because the offline `styles/index.scss` still contains them:

- `body, html, #app { height: 100vh; overflow: hidden }` as a global rule under
  `ViewEncapsulation.None`. That is exactly what put the bottom 47px of the editor both off-screen
  and unscrollable — #27 measured it and moved the sizing to the shell.
- `@import "element-templates.css"` (all 23 selectors match nothing, no element-templates provider
  is registered) and `background-image: url("/04.jpg")`, a file this project does not ship.

Its eight extra stylesheets — `bpmn-override.scss`, `camunda-penal.scss`, `setting.scss`,
`toolbar.scss`, `panel.scss`, `style.css`, `font-awesome.min.css` (102 kB), `custom-icons.scss` —
are the same files this repository measured in Chromium and removed as dead, selector by selector.
See "The stylesheets" in the module README for each measurement. `types/editor/utils.d.ts` is the
orphan `Logger` declaration, also removed deliberately.

### What the offline checkout had that this one did not — now ported

The parity branch did two things of real value that were genuinely absent here. Both are now in
this repository, so the sync runs in one direction with nothing lost:

- **`additional-modules/ColorPicker/`** — a **Set Color** button on the element context pad
  opening a six-swatch popup that sets fill and stroke via `modeling.setColor`, registered under
  `otherModule`. Verified against the pinned libraries rather than assumed: diagram-js 11.13.1
  does call `getMultiElementContextPadEntries` for an array target, and `modeling.setColor` does
  take a list, so a multi-selection paints and undoes in one command. 14 tests.
- **`i18n/translate.ts`** — the `translate` service the Vue `Translate` module was supposed to be.
  The Language setting has offered 中文 since the port began and reached nothing; it now reaches
  every label bpmn-js draws. `en_US` stays on the identity path on purpose, because that bundle is
  largely untranslated Chinese — a fact now held as a test rather than as a note. 11 tests.

Both are recorded in the module README's port-status ledger, with what was checked and what is
being claimed. The third offline-only file, `components/context-menu/context-menu-options.ts`, is
superseded by the `bpmn-replace` architecture from #18 and is not ported.

### Verdict

Replace the offline `bpmn-editor/` with this one. It is a strict superset now: everything the
parity branch built, plus fourteen PRs of porting, auditing and bug-fixing that branch never saw.

Known gaps, so the choice is informed: `AutoPlace` is still not ported, so an appended element
with nothing to measure against sits 50px to the right instead of 100px; the bpmnlint bundle is
still out, needing two dependencies this project does not have. Both are described in the module
README with what porting them would cost.

### Measured against the actual offline checkout

`apply --dry-run` was run against the offline tree itself, not a simulation. The result:

- **Payload**: the whole module replaced, plus two files it does not have at all
  (`e2e/playwright/bpmn-custom-icons.e2e.spec.ts`, `CustomIconFlowFixture.java`).
- **Dependencies**: all six already present at the same versions. Nothing to add.
- **Wiring**: eleven of twelve markers already present. The one gap is
  `layouts/main/main.component.scss`, which carries the rule under the old `full-screen-mode`
  name — that is the 47px fix, and `--fix-wiring` closes it in the same command.

The offline branch also had `height: 100dvh` on that rule, which this repository did not. That is
a real catch and it is now ported: `.browserslistrc` targets iOS Safari back to 18.0, and there
`100vh` is the viewport with the browser chrome _ignored_, so the editor is sized taller than the
space it has. The `100vh` above it stays as the fallback.

A real `apply` then made the offline `bpmn-editor/` byte-identical to this one, and its eight dead
stylesheets were gone rather than merged in.

### Scope: this bundle is the BPMN feature, not the whole branch

The offline checkout also lags outside `bpmn-editor/` — 34 files under `src/main/`, most of them
the Persian bundle from PRs #28 and #29, plus `login`, `home`, `translate.directive.ts` and one
`@NotNull` on `FlowEntity`. None of that is BPMN, so none of it is in the bundle. If you want that
too, it is a separate sync and worth deciding separately.

Its `liquibase` changelog and `FlowEntity` already carry the `flow` CLOB, so the BPMN-critical half
of the backend is in place.

## The transport

`scripts/sync-bpmn.sh` moves the feature across. It exists because "copy the folder over" is not
enough: the module also needs six npm dependencies and twelve wiring points in files the target
owns.

There are two ways in, and they are checked identically — both stage the same payload and both
verify it against a sha256 manifest before anything is written.

### If the target machine can reach the repository: one command

Run it from the target project's root:

```sh
git clone --depth 1 -b claude/bpmn-comparison-integration-ipacwc \
      https://github.com/yasamanahmadi88/med.git /tmp/med-bpmn-src \
  && /tmp/med-bpmn-src/scripts/sync-bpmn.sh apply \
      --from /tmp/med-bpmn-src --target "$PWD" --fix-wiring
```

`--from` reads the clone directly, with no tar round-trip. Add `--dry-run` to see every action
first. Re-running it is a no-op, so it is safe to repeat.

### If it cannot: build a bundle and carry it

On a machine with the source tree:

```sh
./scripts/sync-bpmn.sh export
```

Writes `target/med-bpmn-bundle-<sha>-<date>.tar.gz` — payload, upstream copies of every wiring
file, the dependency versions read out of `package.json`, a sha256 manifest, provenance, and a
copy of the script itself. Nothing in `apply` then needs the network:

```sh
mkdir -p bpmn-bundle && tar -xzf med-bpmn-bundle-<sha>-<date>.tar.gz -C bpmn-bundle \
  && bpmn-bundle/sync-bpmn.sh apply --bundle med-bpmn-bundle-<sha>-<date>.tar.gz \
      --target /path/to/med --fix-wiring
```

## What `apply` does, and what it refuses to do

It verifies the bundle against its manifest before touching the target, and refuses to run at all
unless the target holds `package.json`, `angular.json` declaring the `med-portal` project, and
`src/main/webapp/app` — the guard that stops the bundle being unpacked over something that is not
this application.

**Payload — replaced, after a backup.** These belong to the feature and to nothing else, so the
offline copy holds no information the bundle lacks:

| Path                                                                            | Note                                                 |
| ------------------------------------------------------------------------------- | ---------------------------------------------------- |
| `src/main/webapp/app/bpmn-editor/`                                              | the module; removed before the copy, not merged into |
| `src/main/webapp/content/bpmn-icons/`                                           | 107 element icons                                    |
| `src/main/webapp/content/images/bpmn-icon.{png,svg}`                            | the Flow screens' editor button                      |
| `e2e/playwright/{bpmn-editor,bpmn-custom-icons,flow-bpmn-editor}.e2e.spec.ts`   | the three browser suites                             |
| `src/test/java/.../FlowResourceBpmnParserIT.java`, `CustomIconFlowFixture.java` | the parser path and its fixture                      |

The module directory is removed before the copy so that a file deleted upstream cannot survive as
a stale import — a merge would leave it behind and the build would fail somewhere unrelated.

**Dependencies — added, never rewritten.** The six packages (`bpmn-js`,
`bpmn-js-properties-panel`, `bpmn-moddle`, `camunda-bpmn-moddle`, `diagram-js`,
`diagram-js-minimap`) are added to `package.json` through a real JSON parser — `node`, or `python3`
if node is absent, and printed for hand-editing if neither is there. A package already present at
a **different** version is reported and left alone: silently moving a version the offline project
chose is a change to its dependency tree that nobody asked for.

These must be installable offline — an internal registry, a warm npm cache, or a `node_modules`
copied across. `apply` cannot arrange that and does not pretend to.

**Wiring — checked and reported, never edited.** The module reaches into files the offline project
owns and may have changed locally, so `apply` greps each for the marker that proves the wiring is
there and reports what is missing. The upstream copy of every one of them is staged under
`bpmn-sync-<timestamp>/wiring-reference/` at the same path, and the report gives the exact `diff`
command per file.

| File                                                 | Marker                           | What it is                                                                     |
| ---------------------------------------------------- | -------------------------------- | ------------------------------------------------------------------------------ |
| `app/app-routing.module.ts`                          | `bpmn-editor/bpmn-editor.module` | the lazy `/bpmn-editor` route behind the auth guard                            |
| `app/layouts/main/main.component.ts`                 | `/bpmn-editor`                   | `fullScreen` — bpmn-js needs a resolved canvas height                          |
| `app/layouts/main/main.component.scss`               | `.fullscreen-mode`               | the height-constrained column that route gets                                  |
| `app/entities/flow/service/flow.service.ts`          | `xmlTemp`                        | where a not-yet-created flow parks its draft diagram                           |
| `app/entities/flow/list/flow.component.{ts,html}`    | `/bpmn-editor`, `bpmn-icon.png`  | opening the editor on a persisted flow (`?flowId=`)                            |
| `app/entities/flow/new/flow-new.component.{ts,html}` | `bpmnXml`                        | the new-flow form's side of the draft                                          |
| `domain/FlowEntity.java`                             | `columnDefinition = "clob"`      | without it Hibernate generates `varchar(255)` and a real flow cannot be stored |
| `web/rest/FlowResource.java`                         | `bpmnParserActive`               | the external parser path, and the CLOB filter rules                            |
| `liquibase/changelog/20260711_002_domain_schema.xml` | `flow` as `${clobType}`          | the production column the entity must match                                    |
| `e2e/playwright/support/medportal-fixtures.ts`       | `mockApi`                        | the fixture the three BPMN suites need                                         |

**`--fix-wiring` closes the one gap it can do deterministically.** The offline checkout marks the
editor route with `full-screen-mode`; this repository renamed it to `fullscreen-mode` along with
the measured rule block behind it (see "The 47px, fixed" in the module README). The name lives in
**two** files — `main.component.html` and `main.component.scss` — so the fix moves both together;
renaming one alone leaves the shell with no matching rule, which is worse than not touching it.
The flag backs up both files before rewriting either, and if the block it expects is not there it
skips and reports rather than guessing. Everything else is still reported, never edited.

`FlowEntity.java` is the one worth not skipping. The custom-icon library puts up to 192 KB of
base64 inside `FlowEntity.flow`; left at Hibernate's default length that column is generated as
`varchar(255)`, and a realistic flow cannot be saved at all on a schema Hibernate built.

Exit status: `0` when the payload is in and every marker is present, `3` when the payload is in but
wiring still needs a hand, `1` on error.

## Afterwards

```sh
npm install
npm run lint
npm run test -- --run app/bpmn-editor
./mvnw -Dtest=FlowResourceBpmnParserIT test
npx playwright test e2e/playwright/bpmn-editor.e2e.spec.ts   # needs a browser on the machine
```

Everything `apply` replaced is under `bpmn-sync-<timestamp>/backup/` in the target, at its original
path, so the previous state is one `cp -R` away.
