# Carrying the BPMN editor to the offline project

## Which implementation is ahead, and how that was settled

The BPMN feature in this repository is not an alternative to the Vue editor the offline project
came with — it is a file-by-file port of it, and the port has already been audited to completion
against the original. The record is in
[`src/main/webapp/app/bpmn-editor/README.md`](../../src/main/webapp/app/bpmn-editor/README.md),
under "Port status", written as the porting work happened (PRs #16-#29). Every Vue file was
resolved through its call sites, not its name, and each one is accounted for as ported, replaced
by a library the Angular build already carries, or deliberately dropped with the reason stated.

So the question "which is better" is not open. The Angular module here is the one to keep, for
reasons that are recorded rather than asserted:

- **It is the whole of the original, minus what the original could not run.** The audit closed out
  `utils/` (18 files), the four context-menu files, the six leftover `additional-modules`, the four
  moddle extensions, `components/common/`, the 12 stylesheets, `bpmnEnums.ts`, `selectOptions.ts`
  and `bpmn-icons/`. Files that were not ported are named individually with the measurement behind
  the decision — most had no importer in the Vue project either.
- **It fixes bugs the original shipped.** Among them: the delete rule returned a boolean, so one
  protected start event in a drag-selection blocked the whole delete; every module shape was
  100x80 with its icon drawn squashed; the properties panel's selects showed the stored value
  instead of the label; right-click stayed dead across the portal after one visit; the settings
  panel wrote DOM events into the settings; `EmptyXML.ts` interpolated the process name into XML
  unescaped. Each is described in the README with what was measured.
- **It refuses two things the original offered.** `ContextPad/Rewrite` and `PopupMenu/Rewrite`
  register empty providers under the stock names, so selecting those modes from the settings panel
  empties the context pad and the replace menu. They are scaffolding with the examples commented
  out, and they are not here.
- **It is covered.** The module carries its own unit tests, three Playwright suites
  (`bpmn-editor`, `bpmn-custom-icons`, `flow-bpmn-editor`), and a backend integration test for the
  parser path. The Vue editor had none of that.
- **Custom icons are a build, not a port.** The Vue files for that feature had no reachable
  importer and would have thrown if wired. Here the icon library lives in the diagram's own
  `bpmn:Definitions/bpmn:extensionElements`, so it travels with the flow — no new table, no
  endpoint, no `localStorage` — and every upload is validated as hostile input (see "Custom icons"
  in the module README for what is rejected and the 32 KB / 192 KB caps).

Known gaps, stated so the choice is informed rather than sold: `AutoPlace` is not ported, so an
appended element with nothing to measure against sits 50px to the right instead of 100px;
`Translate` and the bpmnlint bundle are out (the first was broken in the original and its bundle is
not English, the second needs two dependencies this project does not have). All three are listed
in the README with what porting them would cost.

## The transport

`scripts/sync-bpmn.sh` moves the feature across. It exists because the offline project has no
route to github.com, so the transport has to be one file that can be carried by hand, and because
"copy the folder over" is not enough: the module also needs six npm dependencies and eleven wiring
points in files the offline project owns.

### 1. Here, on a machine with this repository

```sh
./scripts/sync-bpmn.sh export
```

Writes `target/med-bpmn-bundle-<sha>-<date>.tar.gz` — payload, upstream copies of every wiring
file, the dependency versions read out of `package.json`, a sha256 manifest, provenance, and a
copy of the script itself. Nothing in `apply` needs this repository afterwards.

### 2. There, on the offline machine

One command, from whichever directory the bundle landed in:

```sh
mkdir -p bpmn-bundle && tar -xzf med-bpmn-bundle-<sha>-<date>.tar.gz -C bpmn-bundle \
  && bpmn-bundle/sync-bpmn.sh apply --bundle med-bpmn-bundle-<sha>-<date>.tar.gz \
       --target /path/to/offline/med
```

Add `--dry-run` to see every action first without writing anything.

## What `apply` does, and what it refuses to do

It verifies the bundle against its manifest before touching the target, and refuses to run at all
unless the target holds `package.json`, `angular.json` declaring the `med-portal` project, and
`src/main/webapp/app` — the guard that stops the bundle being unpacked over something that is not
this application.

**Payload — replaced, after a backup.** These belong to the feature and to nothing else, so the
offline copy holds no information the bundle lacks:

| Path                                                                      | Note                                                  |
| ------------------------------------------------------------------------- | ----------------------------------------------------- |
| `src/main/webapp/app/bpmn-editor/`                                        | the module; removed before the copy, not merged into  |
| `src/main/webapp/content/bpmn-icons/`                                     | 107 element icons                                     |
| `src/main/webapp/content/images/bpmn-icon.{png,svg}`                      | the Flow screens' editor button                       |
| `e2e/playwright/{bpmn-editor,bpmn-custom-icons,flow-bpmn-editor}.e2e.spec.ts` | the three browser suites                          |
| `src/test/java/.../FlowResourceBpmnParserIT.java`, `CustomIconFlowFixture.java` | the parser path and its fixture                |

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

| File                                                          | Marker                            | What it is                                          |
| ------------------------------------------------------------- | --------------------------------- | --------------------------------------------------- |
| `app/app-routing.module.ts`                                   | `bpmn-editor/bpmn-editor.module`  | the lazy `/bpmn-editor` route behind the auth guard |
| `app/layouts/main/main.component.ts`                          | `/bpmn-editor`                    | `fullScreen` — bpmn-js needs a resolved canvas height |
| `app/layouts/main/main.component.scss`                        | `.fullscreen-mode`                | the height-constrained column that route gets       |
| `app/entities/flow/service/flow.service.ts`                   | `xmlTemp`                         | where a not-yet-created flow parks its draft diagram |
| `app/entities/flow/list/flow.component.{ts,html}`             | `/bpmn-editor`, `bpmn-icon.png`   | opening the editor on a persisted flow (`?flowId=`) |
| `app/entities/flow/new/flow-new.component.{ts,html}`          | `bpmnXml`                         | the new-flow form's side of the draft               |
| `domain/FlowEntity.java`                                      | `columnDefinition = "clob"`       | without it Hibernate generates `varchar(255)` and a real flow cannot be stored |
| `web/rest/FlowResource.java`                                  | `bpmnParserActive`                | the external parser path, and the CLOB filter rules |
| `liquibase/changelog/20260711_002_domain_schema.xml`          | `flow` as `${clobType}`           | the production column the entity must match         |
| `e2e/playwright/support/medportal-fixtures.ts`                | `mockApi`                         | the fixture the three BPMN suites need              |

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
