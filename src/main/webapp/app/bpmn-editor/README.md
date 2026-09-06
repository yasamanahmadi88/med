# BPMN Editor Module

This module provides a BPMN (Business Process Model and Notation) diagram editor integrated into the Angular application.

## Features

- **BPMN Diagram Editor**: Full-featured BPMN 2.0 diagram editing capabilities
- **Custom Elements**: Support for custom BPMN elements and properties
- **Properties Panel**: Edit element properties with a dynamic properties panel
- **Toolbar**: Quick access to common operations (save, export, import, undo, redo)
- **Settings**: Configurable editor settings (language, background, UI elements)
- **Internationalization (i18n)**: Multi-language support
- **Mini Map**: Visual navigation for large diagrams

## Installation

### 1. Install Dependencies

The required BPMN.js libraries and dependencies are already listed in the project's `package.json`:

```bash
npm install
```

### 2. Import the Module

In your Angular module, import the `BpmnEditorModule`:

```typescript
import { BpmnEditorModule } from './bpmn-editor/bpmn-editor.module';

@NgModule({
  imports: [
    BpmnEditorModule,
    // other imports...
  ],
})
export class YourModule {}
```

### 3. Add Route

Add the BPMN editor route to your routing configuration:

```typescript
{
  path: 'bpmn-editor',
  loadChildren: () => import('./bpmn-editor/bpmn-editor.module').then(m => m.BpmnEditorModule)
}
```

## Project Structure

```
bpmn-editor/
├── components/           # Angular components
│   ├── designer/        # BPMN diagram canvas component
│   ├── toolbar/         # Toolbar component
│   ├── palette/         # Elements palette
│   ├── panel/           # Properties panel
│   ├── settings/        # Settings panel
│   └── context-menu/    # Context menu
├── services/            # Angular services
│   └── bpmn-editor.service.ts  # Main editor service
├── store/               # State management (migrated from Pinia)
├── i18n/               # Internationalization files
├── config/             # Configuration files
├── utils/              # Utility functions
├── types/              # TypeScript type definitions
├── styles/             # Global styles
└── bpmn-editor.module.ts  # Module definition
```

## Usage

### Basic Usage

Use the BPMN editor component in your template:

```html
<app-bpmn-editor></app-bpmn-editor>
```

### Programmatic Access

Access the editor programmatically through the service:

```typescript
import { BpmnEditorService } from './services/bpmn-editor.service';

constructor(private editorService: BpmnEditorService) {}

// Get current editor settings
const settings = this.editorService.getEditorSettings();

// Update settings
this.editorService.updateConfiguration({ language: 'en' });

// Get process XML
const xml = this.editorService.getProcessXml();

// Access BPMN modeler
const modeler = this.editorService.getBpmnModeler();
```

### Save and Cancel

The editor components never persist anything themselves. `ToolbarComponent` reports Save and Cancel to
whatever is provided for the `BPMN_EDITOR_HOST` token (`services/bpmn-editor-host.ts`), so where a
diagram is stored is the routed page's decision and the editor stays reusable.

`components/flow/flow-bpmn-editor.component.ts` is the host behind the `/bpmn-editor` route and the
only file in this feature that knows about flows. It seeds the diagram before the editor is created
— `DesignerComponent` reads `[xml]` once, while it builds the modeler — and on save writes it back
to the flow named by `?flowId=`, or to `FlowService.xmlTemp` when the flow has not been created yet.

## Configuration

Edit `src/main/webapp/app/bpmn-editor/config/index.ts` to customize:

- Process name and ID
- Editor appearance (background, palette mode, panel mode)
- UI elements visibility (toolbar, mini map, context menu)
- Default language
- Process engine (Camunda, Activiti, etc.)

## Internationalization (i18n)

Translations are stored in `i18n/` directory. Currently supported languages:

- 中文 (Chinese) - zh_CN
- English - en

Add new translations by:

1. Creating a new language file in `i18n/`
2. Adding language support in settings

## Structure

- Standalone Angular components for the canvas, palette, toolbar, properties panel and settings
- Angular services with RxJS observables for editor state
- Scoped component styles

## Dependencies

Main BPMN.js libraries used:

- `bpmn-js`: Core BPMN modeler
- `bpmn-js-properties-panel`: Properties panel for editing element properties
- `diagram-js`: Diagram drawing and manipulation library
- `camunda-bpmn-moddle`: Camunda BPMN extensions support

## Port status

The Vue editor this module replaces had 18 files under `src/utils`, plus a context menu spread
over four more. Resolved through their call sites rather than their names, most carry no
behaviour this editor still needs.

### `utils/`

**Ported**

| Vue file                  | Here                                  | Note                                                                                                                                                                                                                                                         |
| ------------------------- | ------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `EmptyXML.ts`, `index.ts` | `utils/empty-diagram.ts`              | `modeler.createDiagram()` discards the configured `processId`/`processName`. The engine argument `EmptyXML` took is dropped — it was never referenced, so no diagram ever carried it. The process name is now XML-escaped; the original interpolated it raw. |
| `BpmnDesignerUtils.ts`    | `context-menu/ContextMenuProvider.ts` | Only `isAppendAction`, its one function with a consumer.                                                                                                                                                                                                     |

**Not ported — no consumer in the Vue project either**

`BpmnHasProcessRef.ts`, `files.ts`, `storage.ts`, `uuid.ts`, `tools.ts`. Nothing imports them,
by path or by exported symbol.

**Not ported — already provided by the stock properties panel**

`BpmnAsyncElement.ts`, `BpmnEventDefinitionUtil.ts`, `BpmnExtensionElementsUtil.ts`,
`BpmnImplementationType.ts`, `BpmnValidator.ts`. Each exists only to serve a `bo-utils` file
behind one panel group — job execution, conditions, execution listeners, extension properties,
and id validation. `designer.component.ts` registers `BpmnPropertiesProviderModule` and
`CamundaPlatformPropertiesProviderModule`, which ship those same groups (`JobExecutionProps`,
`ConditionProps`, `ExecutionListenerProps`, `ExtensionPropertiesProps`, `IdProps`,
`AsynchronousContinuationsProps`). Porting them would fork a maintained implementation.

**Not ported — belongs with a feature still to come**

| Vue file                                                  | Waiting on                                                                                                                                  |
| --------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `customIconRegistry.ts` (and its `-fixed` near-duplicate) | Custom icon upload — registry, palette provider, upload UI and service together.                                                            |
| `Logger.ts`                                               | Its two consumers (the Vue panel shell and `CustomRules`) are unported. The orphan `types/editor/utils.d.ts` declaring it has been removed. |

### The context menu

`context-menu/` and `components/context-menu/` between them replace `EnhancementContextmenu.ts`,
`components/ContextMenu/`, and the two utils those used.

Right-click splits in two, and only one half needed anything of ours:

| Right-clicked            | Vue                                                                                     | Here                                                                                             |
| ------------------------ | --------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| An element               | its own flat list, from a 130-line reimplementation of `ReplaceMenuProvider.getEntries` | the stock `bpmn-replace` popup — the same choices, plus search, grouping and keyboard navigation |
| Canvas, pool, subprocess | the same flat list, in "create" mode                                                    | our own menu; `bpmn-replace` is empty for these                                                  |

`BpmnReplaceOptions.ts` is not ported because it _is_ that reimplementation. The create menu's
entries are still bpmn-js's own `ReplaceOptions` data (`START_EVENT + TASK + GATEWAY +
BOUNDARY_EVENT`, the four groups Vue chose) — importing their data, not forking their logic.

**This closes out `EventEmitter.ts`.** Its last unported event was `show-contextmenu`, carried
between the modeler-side handler and the menu component. bpmn-js already has an event bus both
sides can reach, so `ContextMenuProvider` fires `contextMenu.append.open` on it and
`ContextMenuComponent` — which takes the modeler from `BpmnEditorService` — listens. No bus of
our own, and nothing left in `EventEmitter.ts` to port. Its other two events were already
answered: `element-update` by the properties panel, `modeler-init` by Angular DI.

### The six `additional-modules` left over

Audited the same way. Four of the six carry no behaviour at all, and two of those would take
behaviour _away_ if they were ported.

| Vue module               | Registered when                               | What it actually does                                                                                                                                                                                                |
| ------------------------ | --------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Rules`                  | `otherModule`                                 | One rule: start and end events cannot be deleted. **Ported.**                                                                                                                                                        |
| `ContextPad/Enhancement` | `contextPadMode: 'enhancement'` (the default) | Extends `ContextPadProvider`, overrides `getContextPadEntries` to return `{}`, registers under a _new_ name — so the stock provider still runs and this adds nothing. Every example entry is commented out. A no-op. |
| `ContextPad/Rewrite`     | `contextPadMode: 'rewrite'`                   | The same empty provider, but registered as `contextPadProvider` — it _replaces_ the stock one. Selecting this mode empties the context pad: no delete, no connect, no append.                                        |
| `PopupMenu/Enhancement`  | never                                         | `class EnhancementPopupMenuProvider {}`. Not referenced by the designer at all.                                                                                                                                      |
| `PopupMenu/Rewrite`      | never                                         | `class RewritePopupMenuProvider {}` registered as `replaceMenuProvider`, which would replace bpmn-js's real replace menu with a no-op. Also never referenced.                                                        |
| `AutoPlace`              | `otherModule`                                 | bpmn-js's own `getFlowNodePosition`, copied, with two constants changed. See below.                                                                                                                                  |
| `Translate`              | always                                        | Broken — see below.                                                                                                                                                                                                  |
| `Lint`                   | `useLint` (off by default)                    | bpmnlint rule bundle. Needs two dependencies this project does not have.                                                                                                                                             |

`ContextPad` and `PopupMenu` are scaffolding the Vue author left behind: "here is where you would
customise this", with the examples commented out. Porting the enhancement variants would add four
files that do nothing; porting the rewrite variants would ship a way to break the editor from the
settings panel. Neither is here.

**The delete rule is ported with one deliberate change.** The Vue rule returned a boolean, so a
single start event caught in a drag-selection blocked the whole delete. This one returns the
elements that _may_ go, which is the convention diagram-js expects, so the rest of the selection
still deletes.

#### `AutoPlace` — not ported, and the difference is visible

Vue's `CustomAutoPlace` is `getFlowNodePosition` from `bpmn-js/lib/features/auto-place/
BpmnAutoPlaceUtil` copied out, with `minDistance` raised from 80 to 100 and passed to
`getConnectedDistance` as `defaultDistance` (bpmn-js uses 50 there). The effect: an appended
element with nothing to measure against sits 100px to the right instead of 50px.

That is a real difference, and reproducing it means forking ~40 lines of library code to change
two numbers — the same trade this port has refused elsewhere. Left out for now; say the word and
it is a small, self-contained follow-up.

#### `Translate` — broken in the original, and the bundle is not English

```ts
const lang = sessionStorage.getItem('en_Us'); // a language name used as a storage key
const translations = languages['en_Us' || lang]; // always the literal 'en_Us'
```

`languages` has `zh_CN` and `en_US`. `'en_Us'` — lowercase `s` — is neither, so `translations` is
always `undefined` and every label falls through untranslated. The Vue editor never translated
anything; bpmn-js's own English labels are what users saw.

Fixing the lookup alone would make things worse here. `i18n/en_US` is largely **untranslated
Chinese** carried over from `zh_CN`: all 25 entries in `elements/tasks.ts`, 30 in
`elements/events.ts`, 176 of ~182 in `elements/other.ts`. Wiring translation up with
`defaultLang = 'en_US'` would turn the palette, context pad and popup menu Chinese.

So this needs a decision about the product's language before it needs code, and it is not in this
change.

### The toolbar

Nine Vue files under `components/Toolbar`. Resolved the same way — by what each one renders, not
by its name. **Two of them render nothing at all**, because `setup()` builds the handlers and
never returns a render function.

| Vue file            | Renders? | What it does                                                                                                            |
| ------------------- | -------- | ----------------------------------------------------------------------------------------------------------------------- |
| `Previews.tsx`      | yes      | "Preview as XML" dialog, and a Cancel button. **Ported** (the JSON preview it also defines is never rendered).          |
| `Scales.tsx`        | yes      | Zoom out / fit-to-viewport with a live percentage / zoom in. **Ported.**                                                |
| `Commands.tsx`      | yes      | Undo / redo / restart. Restart **ported**; undo and redo already existed.                                               |
| `ExternalTools.tsx` | yes      | Minimap toggle, shortcut-key dialog, token-simulation toggle, lint toggle, event-listener dialog. First two **ported**. |
| `Exports.tsx`       | yes      | One Save button. Superseded — see below.                                                                                |
| `Imports.tsx`       | **no**   | `setup()` returns nothing. Angular already has a working Import button.                                                 |
| `Aligns.tsx`        | **no**   | `setup()` returns nothing. And bpmn-js ships alignment anyway — see below.                                              |
| `index.tsx`         | yes      | Layout only.                                                                                                            |
| `index.vue`         | —        | The custom-icon upload; belongs with the icons work, not here.                                                          |

#### Ported

- **Preview as XML** — the serialised document in a dialog, with a Copy button. `Ctrl`/`⌘` + `C`
  copies the whole document, but only when nothing is selected: the Vue original took the
  keystroke even from a real selection and re-wrote that selection to the clipboard itself, which
  is what the browser already does.
- **Zoom** — 10% steps about the canvas origin, plus fit-to-viewport, with the live scale on the
  middle button. Two changes: the label rounds (`83%`) where Vue truncated to 10% steps (`80%`),
  which matters because fit-to-viewport lands on an arbitrary scale; and the steps are clamped to
  `0.2`–`4`, the bounds diagram-js caps its own scroll zoom to. Vue's arithmetic walked to `0` and
  then negative, and a canvas zoomed to zero cannot be zoomed back out.
- **Restart** — clears the command stack, then seeds a fresh diagram through the same
  `createNewDiagram` the designer uses, so it carries the configured `processId`/`processName`.
- **Minimap** — `diagram-js-minimap` was already a dependency and its stylesheet was already
  imported, but the module was never registered: `settings.miniMap` reached nothing, so the
  setting did nothing in either position. It is registered now, and the toolbar toggles it.
  `designer.scss` hides the minimap's own toggle widget, so this button is the only way in.
- **Keyboard shortcuts** — see below.

#### The shortcut list is derived, not copied

Vue's dialog listed ten shortcuts and gated three more behind `templateChooser`. Checked against
the modules `bpmn-js/lib/Modeler` actually registers, that list was both incomplete and wrong:
`R`, `A` and `N` come from `BpmnKeyboardBindings` and `CreateAppendKeyboardBindings`, which are
default modules, so they are always bound; and delete, copy, paste, find, the connect tool and
every arrow-key binding were missing.

`components/toolbar/shortcuts.ts` is written from those modules and confirmed by pressing the keys
in the Playwright suite. A shortcut list that lies is worse than no list.

**Correcting a note from the previous change.** That change reported the Delete key as "not bound
in this configuration at all". It is bound — `KeyboardBindings` registers `removeSelection` for
`Delete` and `Backspace`. What actually happens is that placing an element opens its label editor
and puts the caret inside it, so the keystroke belongs to the text until `Escape` closes it. Two
Playwright tests pin this: Delete after `Escape` removes a task, and the same sequence on the
start event removes nothing — which is a stronger proof of the delete rule than the context-pad
test, because the keyboard goes straight to the editor action.

#### Not ported

- **`Exports.tsx`** — its Save posts the diagram to the embedding page with
  `window.parent.postMessage(xml, '*')`. A wildcard target origin hands the document to whatever
  frame happens to be the parent. Angular's Save goes through `BpmnEditorHost`, which the route
  that opened the editor provides. That is kept.

  What Vue's Save also did is **disable itself while any URL, port, IP or time field fails
  validation**. That gate is now in place — see "The four validators" below.

- **`Aligns.tsx`** — never rendered, and bpmn-js 11's `Modeler` registers `AlignElementsModule`
  by default, so aligning a multi-element selection already works from the context pad. Porting
  six toolbar buttons would duplicate a shipped feature.

- **`Imports.tsx`** — never rendered. Angular's Import button already reads a `.bpmn`/`.xml` file
  and imports it.

- **Token simulation** (`bpmn-js-token-simulation`) and **lint** (`bpmnlint`,
  `bpmn-js-bpmnlint`) — neither dependency is installed. Adding them is your call, the same
  question the lint module raised previously.

- **The event-listener dialog** — lists the keys of `eventBus._listeners`, a private field, with a
  search box. It is a debugging aid for someone working on the editor, not something to put in
  front of a user drawing a process. Say the word if you want it behind a developer flag.

#### Icons

The toolbar's buttons were marked up as `<i class="fas fa-save">`. This project ships FontAwesome
as SVG components (`@fortawesome/angular-fontawesome`) and loads no webfont stylesheet, so those
elements rendered nothing — every button was its text label and an empty box. They are `<fa-icon>`
now, with the definitions passed straight in rather than added to the app-wide icon registry, and
a Playwright test counts the rendered SVGs so the two cases cannot be confused again.

### The four validators

Four `store/*Validation.ts` files, 669 lines, and every one of them is the same generic error bag
with one validator bolted on. Only **four fields** in the whole panel ever used them:

| Field                     | Rule             | Vue message                                   |
| ------------------------- | ---------------- | --------------------------------------------- |
| `fileTransmitter.ip`      | IPv4 dotted quad | `Invalid IPv4 format (e.g., 192.168.1.1)`     |
| `fileTransmitter.port`    | integer 0–65535  | `Port must be an integer between 0 and 65535` |
| `httpTransmitter.authUrl` | URL pattern      | `Invalid URL format` / spaces / scheme        |
| `merger.expireTimeOfDay`  | `HH:mm:ss`       | `Time must be in HH:mm:ss format`             |

`validators.ts` holds those four rules and their messages. A schema field opts in by naming one
(`validate: 'ipv4'`); nothing validates by default. An empty value is always accepted — none of
these properties is required, and every Vue component cleared its error before testing the
pattern.

The message appears under the field, because `@bpmn-io/properties-panel` entries already take a
`validate` callback and render what it returns. That replaces four hand-written components' worth
of error markup with one line in the provider.

#### The gate reads the whole diagram, not the selected element

The Vue stores were keyed by element, but every field component called `clearError` in
`onUnmounted` — so selecting a different element **erased the record of the invalid one**, and
Save went back to enabled with the bad value still in the diagram. An invalid value only blocked
the save while you were looking straight at it.

`validation.ts` walks the element registry instead, on every `commandStack.changed` and after
every import. What gates Save is the state of the whole diagram, including elements the user has
never selected and values that arrived through a file.

And the banner says which element, which field and why. The Vue button greyed itself out with no
explanation anywhere on the page — and worse, `App.tsx` hid the **entire toolbar** when a URL
error existed (`canShowToolbar = showToolbar && !urlValidationStore.hasErrors`), taking Cancel
away with it, so a mistyped URL left the user with no way out of the editor. That is not ported.

#### Two rules changed, deliberately

- **`http://localhost:8080` was rejected.** The Vue URL pattern made the scheme optional but
  always demanded a dot in the host, so a service name inside a compose network or a cluster —
  which is what these fields usually hold — failed, and with the Save gate the flow could not be
  saved at all. The pattern now accepts either a scheme with any host or a dotted host on its
  own. Widening only: nothing Vue accepted is rejected now.
- **`1e2`, `0x10`, `12.0` and `-0` were valid ports.** Vue ran `Number(value)` and asked whether
  the result was an integer in range. None of those is a port number; the check is digits-only
  now. This one is a narrowing, and it is the point of the field.

The URL messages are Vue's, with one reordering: a value holding a space is reported as such even
when it also lacks a scheme. Vue tested the scheme first, so `http://a b.com` — where the space is
the real problem — was reported as missing the `http://` it plainly has.

#### Fields that could be validated and are not

Only the four Vue validated are gated, because the gate blocks saving: turning it on for a field
nobody validated before can block a flow that has been saving fine for years. The candidates, if
you want them:

| Field                      | Module            | Note                                                                                         |
| -------------------------- | ----------------- | -------------------------------------------------------------------------------------------- |
| `ip`, `port`               | `FileReceiver`    | The same two properties, same labels, on the sibling module. The strongest candidate by far. |
| `partyUrl`                 | `HttpTransmitter` | An http URL, like the `authUrl` next to it.                                                  |
| `authTokenVerificationUrl` | `HttpReceiver`    | An http URL.                                                                                 |
| `url`                      | `DbTransmitter`   | A **JDBC** URL — the http pattern would be wrong here; it needs its own rule.                |

Adding one is a single word in the schema. Say which, and whether the risk of blocking an
existing flow is worth it for that field.

### `config/bpmnEnums.ts`, `config/selectOptions.ts` and `bpmn-icons/` — 228 lines, nothing to port

Audited the same way as everything else: by resolving each file's consumers, then checking
whether the library already ships what it does. All four turn out to be library code copied out,
or dead.

| Vue file                    | Lines | What it is                                                        |
| --------------------------- | ----- | ----------------------------------------------------------------- |
| `config/bpmnEnums.ts`       | 8     | `LISTENER_ALLOWED_TYPES`, copied out of bpmn-js-properties-panel. |
| `config/selectOptions.ts`   | 7     | **Dead** — nothing imports it.                                    |
| `bpmn-icons/getIconType.ts` | 90    | `getConcreteType`, copied out of bpmn-js-properties-panel.        |
| `bpmn-icons/index.ts`       | 123   | The panel's `iconsByType` map, as font classes.                   |

#### `bpmnEnums.ts` is the library's own constant

```ts
// Vue: src/config/bpmnEnums.ts
export const LISTENER_ALLOWED_TYPES = [
  'bpmn:Activity',
  'bpmn:Event',
  'bpmn:Gateway',
  'bpmn:SequenceFlow',
  'bpmn:Process',
  'bpmn:Participant',
];
```

`bpmn-js-properties-panel/dist/index.esm.js:15289` declares a constant of the **same name** with
the **same six entries in the same order**, and gates its own Execution listeners group on it:

```js
function ExecutionListenerProps({ element, injector }) {
  if (!isAny(element, LISTENER_ALLOWED_TYPES)) return;
  if (is(element, 'bpmn:Participant') && !element.businessObject.processRef) return;
```

That second line is also in the Vue copy, as `isExecutable()` in `bo-utils/executionListenersUtil.ts`
— the enum's only consumer. The whole thing is one library function reimplemented around one
library constant, and `CamundaPlatformPropertiesProviderModule` already renders the group.

#### `getIconType.ts` is `getConcreteType`, comment numbering included

Vue's 90 lines are `getConcreteType` from `bpmn-js-properties-panel/dist/index.esm.js:1219`,
line for line, down to the `// (1) event definition types` / `// (2) sub process types` /
`// (3) conditional + default flows` comments and the four helpers below them.

The panel calls it in `PanelHeaderProvider` to pick the header icon out of `iconsByType`
(`index.esm.js:1107`, the same ~90 keys as `bpmn-icons/index.ts`) and to build the header's type
label. The Vue panel's header did exactly that and no more, so the map and the function are both
already on screen in this editor.

#### `selectOptions.ts` is dead, and its labels are Chinese

```ts
export const scriptTypeOptions = ref<Record<string, string>[]>([
  { label: '外链脚本( External Resource )', value: 'external' },
  ...
])
```

Nothing imports it. The two components that use a `scriptTypeOptions` — `ElementExecutionListeners.vue`
and `ElementConditional.vue` — each declare their own local copy. The panel's equivalent offers
`resource` / `script`, which are the values that actually write `camunda:resource` or an inline
`camunda:script`; `external` / `inline` / `none` were never wired to anything.

#### What this change does instead: prove the coverage

"The library already does it" is worth nothing if a future change quietly drops the module, so
three Playwright tests now hold the claims:

- the header names the selected element by its **concrete** type — `Exclusive Gateway`, not
  `Gateway`, which is the whole reason `getConcreteType` exists;
- the header icon **differs** between a start event and a task, so a single fallback icon fails
  the test where a presence check would pass;
- the Execution listeners group appears on a start event, a gateway and the process.

The third was falsified before being kept: with `CamundaPlatformPropertiesProviderModule` removed
from `designer.component.ts` it fails on the first assertion, and passes again once restored.

### The four `moddle-extensions` still in the Vue folder — none of them ported

`moddle-extensions/` holds 23 files in the Vue editor and 19 here. The four missing ones were
resolved the same way as everything else: by who imports them, and then by whether an npm package
already ships them. All four stay out, and one of them would have done real damage.

| Vue file                | Imported by                                                  | Verdict                                                                |
| ----------------------- | ------------------------------------------------------------ | ---------------------------------------------------------------------- |
| `bpmn.json`             | **nobody**                                                   | `bpmn-moddle`'s own BPMN 2.0 schema. Registering it replaces it.       |
| `camunda.json`          | `Designer/modulesAndModdle.ts:10`                            | `camunda-bpmn-moddle`'s own file, plus one type nothing names.         |
| `zeebe.json`            | **nobody**                                                   | `zeebe-bpmn-moddle`'s schema, for an engine this editor never targets. |
| `customIconModule.json` | `Designer/CustomIconIntegration.ts:3` — which nobody imports | Dead twice over, and it contradicts its own palette.                   |

#### `bpmn.json` is BPMN 2.0 itself, and registering it is silent damage

The file is `bpmn-moddle/resources/bpmn/json/bpmn.json` — 2957 normalised lines of it — with two
extra `isAttr: true` flags, on `LinkEventDefinition#target` and `MessageEventDefinition#operationRef`.
It carries `"prefix": "bpmn"` and `"uri": "http://www.omg.org/spec/BPMN/20100524/MODEL"`: the
package `bpmn-moddle` registers for itself.

The failure mode is what makes this worth spelling out. `bpmn-moddle` merges the extensions over
its own packages **by key** (`bpmn-moddle/dist/index.js:3730-3742`,
`assign({}, packages, additionalPackages)`), so:

- keyed `bpmn`, the descriptor **replaces** the BPMN 2.0 schema — no error, no warning, and every
  diagram is then parsed against a stale copy;
- keyed anything else, moddle throws `package with prefix <bpmn> already defined`
  (`moddle/dist/index.js:958`).

The first is the one to guard, because nothing else would notice. `index.spec.ts` does now.

#### `camunda.json` is the library file plus one type nothing names

Normalised and diffed against `camunda-bpmn-moddle@7.0.2/resources/camunda.json`, the whole
difference is 64 lines, all additions, all in one place:

- a type `cdrParserProperties` — `camunda:ExecutionListener`'s shape under another name — which
  appears nowhere in the Vue project but its own declaration (`moddle-extensions/camunda.json:1039`);
- the string `camunda:cdrProperties` added to `Field`'s `meta.allowedIn`
  (`moddle-extensions/camunda.json:695`), naming a type that does not exist — the type above is
  declared as `cdrParserProperties`. `allowedIn` is inert here in any case: the only occurrence of
  the word across `bpmn-js`, `bpmn-js-properties-panel`, `moddle`, `bpmn-moddle`,
  `camunda-bpmn-moddle` and `diagram-js` is inside the resource file that declares it.

`additional-modules/index.ts:1` already imports the npm descriptor. The CDR parser's real schema is
the separate `cdrParserProperties.json`, which is ported, and which is what
`Designer/modulesAndModdle.ts:100,171` registered for that panel.

#### `zeebe.json` targets an engine this editor does not

`zeebe-bpmn-moddle`'s schema — `ZeebeServiceTask`, `TaskDefinition`, `IoMapping`, `TaskHeaders` and
twelve more, under `http://camunda.org/schema/zeebe/1.0`. Nothing imports it in the Vue project,
`zeebe-bpmn-moddle` is not a dependency of this project, no Zeebe properties provider is
registered, and no palette entry or property form emits a Zeebe element. It would register a
namespace and change nothing.

#### `customIconModule.json` is dead in the Vue project too

Its one importer is `Designer/CustomIconIntegration.ts:3`, and `CustomIconIntegration` is imported
by nothing — a whole-project grep returns only the file's own self-references. Reached, it would
still not work:

- `utils/customIconRegistry-fixed.ts:48` mints element types as `Custom:${icon.name}`, while the
  file declares prefix `custom` with the single type `CustomElement`. The palette provider would
  ask `elementFactory.createShape` for a namespace nobody registered.
- `CustomIconIntegration.ts:1` reads the `-fixed` registry while
  `Palette/EnhancementPalette/customIconPaletteProvider.ts:10` reads the other one, so the two
  halves would not see the same icons.
- `CustomIconIntegration.ts:29-53` only `new`s the provider into a field. It is never registered
  with didi, so `getPaletteEntries` is never called.
- the file spells its own `enumerations` key `emumerations`.

Custom icons are blocked on a product decision, and this file is not the part that was missing.

#### What this change does instead: hold the claims in moddle

The `moddleExtensionsFor` tests could only read the keys of the returned object, which never
touches moddle — and moddle is where every way of getting this wrong actually shows up. Four tests
now build the real thing (`new BpmnModdle(extensions)`, which is what bpmn-js does with this object
at `bpmn-js/lib/BaseViewer.js:61,646`), and each was falsified before being kept:

| Test                                              | Falsified by                                                                              |
| ------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| `bpmn:Definitions` resolves for every engine      | returning camunda and cdrParser together → `property <diagramRelationId> already defined` |
| every type the palettes place resolves            | dropping `KafkaReceiver` → `expected undefined to be truthy`                              |
| a diagram parses and re-serialises                | dropping the camunda descriptor → `expected 'true' to be true`                            |
| no package `bpmn-moddle` already owns is declared | adding the BPMN 2.0 package → `expected [ 'bpmn', … ] to not include 'bpmn'`              |

The third is the one worth reading twice. `camunda:asyncBefore="true"` comes back as the **string**
`'true'` when the camunda descriptor is missing, because moddle keeps the attributes of an unknown
namespace in `$attrs` verbatim. A test that only checked the value survived would pass either way;
asserting the boolean is what makes it a test.

#### Two corrections to what `additional-modules/index.ts` said

Checking the one-engine rule against moddle rather than against the key list turned up two claims
in that comment that are not true:

- **"the modeler fails to construct at all."** It does not. moddle builds type descriptors lazily,
  so the clash surfaces on the first `getType('bpmn:Definitions')` — that is, the first
  `createDiagram` or `importXML`, with a modeler that constructed perfectly well. A test that only
  constructs a modeler would miss it entirely.
- **"registering more than one"** is not the trigger. Only camunda and cdrParser clash, because
  only those two extend `bpmn:Definitions` (`camunda-bpmn-moddle/resources/camunda.json:10-23`,
  `moddle-extensions/cdrParserProperties.json:10-21`). All five other pairs build a working moddle.
  The rule is still right — one diagram carries one engine's schema — but it is enforced here, not
  by moddle.

#### Two hand-written declarations corrected along the way

Testing against moddle rather than against the key list meant calling the library the way bpmn-js
calls it, and `types/declares/` described three of those signatures wrongly. Checked against
`bpmn-moddle/dist/index.js` (v9.0.4) and fixed rather than cast around:

- `BpmnModdle`'s constructor was typed `Package[]` only. moddle also takes a record keyed by
  prefix, which is the form bpmn-js passes (`BaseViewer.js:649`).
- `fromXML` was typed as resolving to a result _or_ an error. It resolves with the result and
  rejects on failure, so the union only forced callers to narrow a case that never arrives.
- `toXML` took a `string`. It takes the element; the library's own JSDoc says `@param {String}`
  and is wrong about itself.
- `moddle`'s `Package` was missing `uri`, which every package has and which the new test reads.

Four `as unknown as` casts came out with them.

### `components/common/` and `styles/context-pad.scss` — 154 lines, nothing to port

Four "common" components and one stylesheet, resolved the same way as everything else: by who
imports each file, and then whether _that_ consumer is itself reachable from `App.tsx`. Two are
dead in the Vue project, two are the hand-rolled version of something the stock properties panel
now renders, and the stylesheet styles a class no code produces, using an image that is not there.

| Vue file                   | Lines | Verdict                                                                                                     |
| -------------------------- | ----- | ----------------------------------------------------------------------------------------------------------- |
| `common/BpmnIcon.vue`      | 23    | **Dead** — no import, no global registration, no template names it.                                         |
| `common/CollapseTitle.vue` | 32    | Registered globally, but its only users are seven files nothing imports. Also the panel's own group header. |
| `common/EditItem.vue`      | 54    | The same seven files. Also the panel's own labelled entry row.                                              |
| `common/LucideIcon.vue`    | 38    | Live in Vue — and every call site that was ported already draws a `<fa-icon>` here.                         |
| `styles/context-pad.scss`  | 7     | Styles a class only commented-out code produces, from an image that does not exist.                         |

#### `BpmnIcon.vue` is dead — and its stylesheet is why this panel header wrapped

`main.ts:79-96` registers `LucideIcon`, `EditItem` and `CollapseTitle` globally. `BpmnIcon` is not
among them, nothing imports it by path, and no template in the Vue source names `<BpmnIcon>` or
`<bpmn-icon>`. The Vue panel header it was written for renders `<p>{bpmnElementName}</p>` and
nothing else (`components/Panel/index.tsx:450-451`), so the component never reached a screen.

Its **stylesheet** did reach this module, though. `styles/panel.scss` was carried over whole, and
its `.panel-header` rule is the icon's layout: a grid with a 40px first column for the `<svg>`,
spanning two rows for the element name and type. `PanelComponent` renders one word and no icon
(`components/panel/panel.component.html:2`), so that column had nothing in it and the title was
squeezed into it — measured in the browser: the header 117px tall, `grid-template-columns: 40px
245px`, and "Properties" broken across two line boxes 39.7px and 31.0px wide with the 245px column
beside it empty. `BpmnEditorComponent` uses `ViewEncapsulation.None`, which is how a global rule
written for a Vue component reached an Angular one.

The rule is removed. Its one non-layout declaration, the `#f5f5f7` tint, moves to
`panel.component.scss` where the header that actually exists is styled, so the only visible change
is the title fitting on its line.

#### `CollapseTitle.vue` and `EditItem.vue` — seven consumers, none of them imported

`<collapse-title>` and `<edit-item>` appear only in `components/Panel/components/Element*.vue` —
`ElementAsyncContinuations`, `ElementConditional`, `ElementDocumentations`,
`ElementExecutionListeners`, `ElementExtensionProperties`, `ElementGenerations`,
`ElementJobExecution`. **Nothing in the Vue project imports any of those seven**, by path or by
symbol. `Panel/index.tsx` renders `renderComponents` (`index.tsx:454-456`), and every branch that
fills it (`index.tsx:172-380`) pushes only the module-specific `*Properties/*.vue` editors. So even
though `penalMode` defaults to `custom` (`config/index.ts:10`) and `App.tsx:77` therefore mounts
the custom panel, none of the seven ever mounts, and the two components they hold are unreachable.

They are also duplicates. `@bpmn-io/properties-panel/dist/index.esm.js:923-947` is `Group`: the
header, the title, the arrow and the open/closed state — the whole collapsible section, of which
`CollapseTitle` was the title row inside a naive-ui `n-collapse-item`. And each entry renders
`<label class="bio-properties-panel-label" for="…">` bound to its control, which is `EditItem`'s
`<div class="edit-item_label">` row with an association it never had: clicking the panel's label
focuses the field.

One difference, stated rather than hidden: `EditItem` put the label to the left of the control at a
fixed pixel width (`labelWidth`, default 80); the panel stacks the label above it. Same
information, different shape, and no code of ours either way.

#### `LucideIcon.vue` — live in Vue, already answered by `<fa-icon>`

The one of the four that is genuinely reachable. Its call sites and what became of each:

| Vue call site                   | Icon                          | Here                                                       |
| ------------------------------- | ----------------------------- | ---------------------------------------------------------- |
| `Commands.tsx:40,50,60`         | `Undo2`, `Redo2`, `Eraser`    | `icons.undo`, `icons.redo`, `icons.restart`                |
| `Scales.tsx:49,71`              | `ZoomOut`, `ZoomIn`           | `icons.zoomOut`, `icons.zoomIn`                            |
| `ExternalTools.tsx:146,170`     | `Map`, `Keyboard`             | `icons.minimap`, `icons.shortcuts`                         |
| `ExternalTools.tsx:125,135,158` | `Bot`, `Podcast`, `FileCheck` | token simulation, lint, event-listener dialog — not ported |
| `Aligns.tsx:7`                  | —                             | imported but never rendered; `setup()` returns nothing     |
| `Setting/index.tsx:81`          | `Settings`                    | inside a `{/* … */}` JSX comment                           |

Every call site that was ported already draws its icon, and porting `LucideIcon` would mean adding
a `lucide-angular` dependency — `package.json` has no lucide package — to redraw them. The toolbar
icon test already counts one `svg.svg-inline--fa` per button, so a regression there is caught.

#### `context-pad.scss` — a class nothing produces, pointing at an image that is not there

Both halves fail independently.

`.enhancement-op` appears exactly twice in the Vue source outside the stylesheet, and **both are
commented out**: `additional-modules/ContextPad/RewriteContextPad/rewriteContextPadProvider.ts:65`
and `:77`. `getContextPadEntries` returns the `actions` object it declared empty at `:60`. Nothing
in this module produces the class either.

And the image. The rule asks for `./logo.ico`, which resolves against `src/styles/`. The only
`.ico` in the Vue repository is `public/logo.ico`; `src/styles/logo.ico` does not exist. Had the
class ever been rendered, its background would still have been nothing.

The provider it decorates is not here in any case: `RewriteContextPadProvider` is registered only
under `contextPadMode: 'rewrite'`, and neither context-pad variant is ported — see "The six
`additional-modules` left over" above. This module's `styles/index.scss` already omits the import.

#### What this change does instead: hold the two coverage claims

The dead files need no code. The two "the library already does it" claims do, because a future
change could quietly drop the module — the same reason the `bpmn-icons` audit left tests behind.
Three Playwright tests:

- **the panel header is a single line box.** This is the assertion that catches the dead
  `.panel-header` grid; the header reads "Properties" either way, so a text assertion walks
  straight past a title broken in half.
- **a group opens and closes from its header**, with its fields appearing and disappearing —
  groups open closed, so the first click has to be the one that reveals them.
- **the ID row's `<label for>` focuses its input when clicked**, which is the association
  `EditItem`'s `<div>` never had.

Each was falsified before being kept. Restoring the `.panel-header` rule turns the first from one
line box into two. Dropping `BpmnPropertiesProviderModule` from `designer.component.ts` fails the
other two. And the focus assertion was checked against a control: clicking the group header title —
a `<div>`, not a label — leaves the input `inactive`, so it is the `<label for>` doing the work.

**Left alone, and worth a word.** `styles/panel.scss` still carries naive-ui leftovers —
`.n-collapse`, `.n-collapse-item*`, `.inline-large-button`, `.need-filled.n-form`. Unlike
`.panel-header` these are inert: no element with an `n-collapse` class renders anywhere in this
editor (counted in the browser: zero). They are the styling for the collapse the properties panel
now provides. Say the word and they go with a follow-up that audits the rest of `Panel/`.

## Future Enhancements

- [ ] Token simulation
- [ ] Color picker
- [ ] Custom icons upload
- [ ] BPMN linting
- [ ] Element templates
- [ ] Advanced validation

## Troubleshooting

### Canvas not rendering

Ensure the canvas container div has width and height set. The component expects `height: 100vh` and `width: 100%`.

### BPMN.js modules not loading

If you encounter module loading issues, ensure all BPMN.js dependencies are installed:

```bash
npm install bpmn-js bpmn-js-properties-panel diagram-js
```

### Styling issues

If styles are not applied correctly, ensure SCSS is properly configured in your Angular build. Check `angular.json` for SCSS support.

## Support

For issues specific to BPMN.js libraries, refer to:

- [BPMN.js Documentation](https://bpmn.io/toolkit/bpmn-js/)
- [Diagram.js Documentation](https://diagram-js.org/)

## License

This module is part of the MedPortal application. Refer to the root LICENSE file for licensing information.
