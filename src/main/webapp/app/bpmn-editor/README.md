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
| `customIconRegistry.ts` (and its `-fixed` near-duplicate) | Nothing: custom icons are built, and they store the library in the diagram rather than in `localStorage`. See "Custom icons" below.         |
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

Custom icons were blocked on a product decision, and this file is not the part that was missing.
The decision has since been made — see "Custom icons" below — and this descriptor is still not part
of it.

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

### The stylesheets — 12 files, 5 left, and two of them were breaking things

`styles/` came across whole, the way `panel.scss` did, so it was audited the same way: every rule
resolved against the DOM this editor actually renders. Not by reading — by counting. A Playwright
probe walked `document.styleSheets`, ran `document.querySelectorAll(rule.selectorText).length` for
all 3,981 rules on the page, and repeated it with the settings panel open, the XML and shortcut
dialogs open, the create menu open and the replace popup open. Anything that looked live was then
measured a second time with its declarations blanked in place (`rule.style.cssText = ''`), which is
the only way to see what a rule contributes: an `!important` override reports the wrong answer,
because `revert` also discards the SVG presentation attributes bpmn-js paints with.

#### Two rules applied and were wrong

**`.djs-popup-group { display: flex !important }`** (`palette.scss`) turned the replace menu — the
wrench on the context pad, which this port deliberately kept as bpmn-js's own popup — into a single
row. diagram-js draws that menu as a `<ul>` in a 300px popup and scrolls it vertically
(`.djs-popup-results { max-height: 280px; overflow: auto }`). Measured on a task in a 1280px
viewport: the ten "Change element" entries laid out **1341px wide**, `resultsScrollWidth` 1341 against
a `clientWidth` of 283, and **eight of the ten past the popup's right edge** — three sitting under
`.djs-popup-backdrop`, where a click dismisses the menu instead of choosing a type, and four off the
side of the screen at x ≥ 1269. With the rule blanked: 10 rows, 1 column, nothing spilling. Removed.

**The grid background never drew.** `index.scss` painted it on `.designer` — Vue's canvas element.
This editor renders `<jhi-designer>`, so the selector matched **0** elements and `bg: 'grid-image'`,
which is the shipped default (`config/index.ts:13`) and puts `designer-with-bg` on the container,
produced `background-image: none`. Same shape as the `.panel-header` bug: a rule laid out around
markup that is not there. Retargeted at `jhi-designer`, measured as the 40px tile with
`background-repeat: repeat` and `background-size: auto` — the `background` shorthand is `!important`,
so it resets the `contain` that `bpmn-editor.component.scss` sets on the same element.

Its sibling `&.designer-with-image` is **removed rather than retargeted**: it asks for `/04.jpg`,
which this project does not ship (the only jpg under `content/` is `images/background-blurry.jpg`).
Retargeting it would turn the settings panel's "Image" option into a 404 on every load, so that
option stays the no-op it already was. `bg: 'grid'` has never had a rule at all.

#### Five files removed, each measured dead

| File                            | Evidence                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `font-awesome.min.css` (102 kB) | Referenced by nothing — `git grep` over the whole tree at the merge base returns only the file itself. It never reaches `document.styleSheets`, and `document.fonts` lists exactly one face: `bpmn`. This project ships FontAwesome as SVG components.                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `style.css`                     | Also referenced by nothing. Its live-looking rules were `.bjs-breadcrumbs { display: none }` (bpmn-js already hides it outside a subprocess) and two contradictory media queries setting `.djs-container { overflow: auto }` and `overflow: hidden`.                                                                                                                                                                                                                                                                                                                                                                                                                     |
| `setting.scss`                  | All 15 selectors matched 0. It styles `.setting`, `.setting-container`, `.setting-header/-content/-footer`, `.toggle-button`; `SettingsComponent` renders `.settings-container`, `.settings-panel`, `.settings-header/-content`, `.settings-toggle` — different classes with their own scoped stylesheet. The rest (`.n-form-item.theme-list`, `.n-color-picker`, `.n-input-number`, `.tips-message`) is naive-ui.                                                                                                                                                                                                                                                       |
| `toolbar.scss`                  | `.toolbar` matches the Angular toolbar, so it was measured rather than read: with the rule blanked, the container box and all 13 button rectangles are **byte-identical** (`identical: true`). Every declaration is either set to the same value by `toolbar.component.scss` (`display: flex`, `align-items: center`, `padding: 8px 16px` = `0.5rem 1rem`) or resolves to the used value it already had (`width: 100%`, `height: min-content`, `box-sizing`). Its other selectors — `.button-list_column`, `.preview-model`, `.shortcut-keys-model`, `.event-listeners-box`, `.n-dialog.n-modal`, `div[class^='n-button']` — matched 0, including with each dialog open. |
| `bpmn-override.scss`            | `.bts-toggle-mode` (bpmn-js-token-simulation, not a dependency) and `.cmd-change-menu`: 0 each.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `camunda-penal.scss`            | `.camunda-penal` renders only under `penalMode: 'default'`. Measured in that mode: 350px with the rule and 350px with it blanked — `bpmn-editor.component.scss`'s `.main-content .camunda-penal` out-specifies it. Nothing else in the file.                                                                                                                                                                                                                                                                                                                                                                                                                             |

Plus one `@import`: **`element-templates.css`**. All 23 of its selectors matched 0, and no
element-templates provider is registered — `designer.component.ts` adds `BpmnPropertiesProviderModule`
and `CamundaPlatformPropertiesProviderModule` only.

#### `panel.scss` was partly live, so its live half moved

The naive-ui leftovers reported earlier — `.n-collapse*`, `.inline-large-button`,
`.need-filled.n-form` — all matched 0, and `width: 480px` never applied: `panel.component.scss`'s
scoped `.panel` wins on specificity, measured at 350px with the global rule both enabled and
blanked. But four declarations _were_ doing something. Blanking the rule moved `padding` from
`0 8px` to `0`, `box-shadow` from `0 0 8px #ccc` to `none`, `max-height` from `100%` to `none` and
`overflow-y` from `auto` to `visible`. Those four (with `box-sizing`) are now in
`panel.component.scss` at their original values — the same handling the `#f5f5f7` tint got when the
`.panel-header` grid came out — and the panel measures identically after the move.

#### Trimmed from the files that stayed

- `designer.scss`: `.designer { flex: 1 }` — 0 matches; `bpmn-editor.component.scss` already gives
  `jhi-designer` that. Its other two rules are live and stay: blanking `.djs-minimap div.toggle`
  turns the minimap's own widget from `display: none` into a 138px block, and blanking
  `.layer-selectionOutline` turns the layer's computed `fill` from `none` to black.
- `palette.scss`: `.djs-visual g image` and seven icon classes, 0 matches each —
  `.transformer-module` and `.eventaDbReceiver-module` (their palette entries are commented out at
  `enhancementPaletteProvider.ts:179` and `:260`), `.httpReceiverEventa-module` (named nowhere), and
  `.csvTransformerCorner` / `.mergerCorner` / `.fragmenterCorner` / `.KafkaTransmitterCorner`, where
  "Corner" is only ever a function name or an entry key, never a `className`. The duplicate
  `.csvTransformer-module` block also went: it was declared twice, and the second (1.5em/1.8em) is
  the one that won — measured at 45px before and after.
- `index.scss`: `#app`, Vue's mount point — 0 matches.

#### Left alone, deliberately

- **`custom-icons.scss` is gone.** It was never imported by `index.scss`, so none of it ever
  loaded and all of its selectors matched 0. It was kept while the custom-icon feature was
  undecided; that feature is built now (see "Custom icons" below), it stores its icons in the
  diagram and its dialog carries its own scoped stylesheet, so nothing was ever going to claim
  this file.
- **`.djs-visual rect`** (`palette.scss`) stays, and it is worth knowing what it does. Three of its
  four declarations are inert: `RewriteRenderer` writes `stroke-width: 2px; fill: #fff;
fill-opacity: …` as an _inline style_, which beats them — measured identical with them and
  without. The fourth, `stroke: rgb(0,0,0) !important`, repaints every rect-drawn element black
  over the renderer's `defaultTaskColor: '#9cafcf'` (`RewriteRenderer.ts:77`). That is also what
  the Vue editor looked like, so removing it would change the diagram's appearance rather than fix
  it — and under `rendererMode: 'default'` bpmn-js paints with presentation attributes, which a
  rule beats, so the other three would stop being inert. Say the word if the blue-grey borders the
  renderer was written for are what you want.
- **`.palette`** (`palette.scss`) renders only under `paletteMode: 'custom'`. Measured in that mode:
  `width: 360px` is out-specified by `palette.component.scss` (200px either way), leaving
  `padding: 16px` as its only effect. Kept — removing it is a visual change, not a dead-rule fix.
- **`body, html, .designer-container`** in `index.scss`. `#app` came out of the selector list; the
  rest stayed at the time, and it had a problem that change did not fix. See below.

#### One bug found and not fixed _at the time_: 47px of the editor is unreachable

**Fixed since — see "The 47px, fixed" below. The diagnosis in this section still stands; only its
last sentence, that it wants its own change, has been overtaken.**

Measured on `/bpmn-editor` at a 720px viewport: the navbar is 46.4px tall, `.designer-container`
starts at y=47 and is `height: 100vh`, so it ends at y=767 — 47px below the bottom of the screen.
`body { overflow: hidden }` from this stylesheet means the page cannot scroll to it
(`window.scrollTo(0, 5000)` leaves `scrollY` at 0), so the bottom 47px of the canvas and of the
properties panel are simply gone. It is the same root cause as everything else here — Vue's editor
_was_ the document — but the fix is not a stylesheet edit:

- `height: 100vh` is set by `bpmn-editor.component.scss` as well, and that copy wins;
- blanking the `body, html` half alone does **not** fix it (measured: still clipped by 47, document
  768px tall, and the page still would not scroll);
- making the container fill the room below the navbar needs the shell chain
  (`body > jhi-main > .app-root > main > .card`) to become a height-constrained column, which is
  `layouts/main/main.component.scss` and affects every route.

The third point turned out to be the one that was wrong, and it is why the change looked bigger
than it is: the shell already branches on this route.

#### What this does to the budget

`bpmn-editor.component.scss` was `2.00 kB` budget, `68.53 kB` actual — 66.53 kB over. It is now
**58.73 kB**, 56.73 kB over — 9.80 kB out. Not more, because the bundle is almost entirely the six
library stylesheets `index.scss` imports (properties-panel 28 kB, diagram-js 20 kB, the bpmn font
17.5 kB, minimap 1.4 kB); our own SCSS was never the bulk of it. The 102 kB of `font-awesome.min.css`
does not appear in that number at all — it was never imported, so it was never in the bundle.

### A second pass over `App.tsx`: `Panel/`, `Designer/`, `store/` and `EnhancementContextmenu`

The areas the earlier changes covered least, resolved the same way — from `App.tsx` outward, by
call site. `App.tsx` mounts six components: `Toolbar`, `Palette`, `Designer`, `Panel`, `Setting`
and `ContextMenu`. Four were already audited. What follows is what the other two, the two stores
and `initModeler`'s one remaining import turned up.

Three of the findings are fixed here. The rest are reported with the patch they would take,
because each is a decision rather than a transcription.

#### Fixed: every module shape was 100x80, and its icon drawn squashed

`components/Designer/modulesAndModdle.ts:154-157` passed `options.elementFactory` whenever
`otherModule` was on, which is the default:

```ts
modules.push(ElementFactory);
options['elementFactory'] = {
  'bpmn:Task': { width: 120, height: 120 },
  'bpmn:SequenceFlow': { width: 100, height: 80 },
};
```

`CustomElementFactory` was ported. That option was not, and it is the only thing the class reads
(`additional-modules/ElementFactory/CustomElementFactory.ts:20-28`): with `config.elementFactory`
undefined every lookup fell through to `super.getDefaultSize`, so the ported factory was a no-op
and bpmn-js's 100x80 applied.

Every integration module declares `superClass: ["bpmn:Task"]`, so this is the size a
KafkaReceiver, an HttpTransmitter and the rest are placed at. It is not only geometry:
`RewriteRenderer` stretches the module's corner icon to the shape's box
(`RewriteRenderer.ts:1961-1981`) and those icons are square — `kafkaReceiverModule_corner.svg` is
`viewBox="0 0 551 553"` — so at 100x80 every module icon was drawn squashed.

`DEFAULT_ELEMENT_SIZES` is now passed to the modeler. The Vue config's `bpmn:SequenceFlow` entry
is deliberately not: `getDefaultSize` is consulted for shapes only, and
`ElementFactory.createConnection` never asks for one, so it changed nothing there either.

#### Fixed: the panel's selects showed the stored value, not the Vue label

`module-properties/schema.ts` already models a choice as a bare value _or_ a `{ value, label }`
pair, "because the Vue templates often showed text differing from the stored value". Two schemas
used the pair form. Fourteen did not, and two of them justified it with a claim about the library
that is not true — `cdr-parser.ts` said "the panel has no separate option label", while
`@bpmn-io/properties-panel/dist/index.esm.js:3931-3935` renders
`<option value={option.value}>{option.label}</option>`.

So the same `0`/`1` flag read **No / Yes** on an HttpTransmitter and **0 / 1** on a Merger, in one
panel. Transcribed from the Vue `<option>` markup, the drift was:

| Field                                     | Vue showed                                     | This showed            |
| ----------------------------------------- | ---------------------------------------------- | ---------------------- |
| `agreementMode` (13 modules)              | `FETCH ONLY`                                   | `FETCH_ONLY`           |
| `Merger.isIncremental234`                 | `No` / `Yes`                                   | `0` / `1`              |
| `Merger.mergerForceNextDay`               | `Is Not` / `Is`                                | `0` / `1`              |
| `CsvTransformer.haveHeader`               | `NO` / `YES`                                   | `0` / `1`              |
| `Merger` first/last-save/last-send/expire | `SAVE AND SEND`, `NOT SEND`, …                 | the underscored values |
| `CdrParser.batchCdrType` (8 of 9)         | `HUAWEI PGW DATA CDR`, `TAP 312`, …            | the underscored values |
| `Transformer` transformType + firstAction | `MCCI CHANGECARD`, `EVENT FROM DB RECEIVER`, … | the underscored values |
| `FileReceiver.fileScanPolicy`             | `SUB FOLDERS`                                  | `SUBFOLDERS`           |
| `FileReceiver.postProcessingAction`       | `RENAME AND MOVE`                              | `RENAME_AND_MOVE`      |
| `anyProcess.ackMode`                      | `NO ACK`, `REC ACK`, …                         | the underscored values |

**No stored value changes.** Only the text the user picks from. `optionValue` and `optionLabel`
in `schema.ts` are the one place that distinction is made, and the specs now assert both halves
separately, so a future schema cannot drop a label without a test noticing.

#### Fixed: right-click stayed dead across the portal after one visit

`bpmn-editor.component.ts` registered the Vue original's
`document.addEventListener('contextmenu', ev => ev.preventDefault())` (`App.tsx:52`) and never
removed it. In Vue that cost nothing — the editor _was_ the application. Here it is a lazily
routed page: leaving the listener behind killed right-click on every other screen in the portal
until a full reload, and each visit stacked another copy. It comes off in `ngOnDestroy` now.

#### Fixed along the way: the settings panel wrote DOM events into the settings

Not a port gap — the Vue `Setting/index.tsx` renders an empty `<div>`, its whole drawer commented
out (`index.tsx:78-215`), so none of this existed there to port. But the Angular panel that
replaced it passed `$event` where its handler expected a value, and stored the `Event` object as
the setting. Nothing threw:

- `toolbar` became a truthy object, so the checkbox could never hide the toolbar;
- `bg` stopped equalling `'grid-image'`, so touching any control dropped the grid background;
- `language` reached `BpmnEditorService.updateConfiguration`, which writes it to `sessionStorage` —
  leaving the string `[object Event]` for the next visit to load as a language.

The handler reads `target.value` / `target.checked` now. Its gear was also `<i class="fas fa-cog">`,
which draws nothing in this project for the reason the toolbar icons did not, so it is an
`<fa-icon>`; and the English option's value is `en_US`, which is the bundle key `i18n/index.ts`
actually exports.

#### Reported, not built

- **The minimap opens closed.** Vue registered it with `minimap: { open: true }`
  (`modulesAndModdle.ts:127-132`), so it was open on load; here the toolbar button is the only way
  to open it. The patch is that one option, but it also means rewriting the Playwright test that
  currently pins "starts closed", so it wants a decision rather than a commit.
- **`bpmn-js-color-picker` and `bpmn-js-token-simulation` were on by default in Vue**, both under
  `otherModule` (`modulesAndModdle.ts:141-147`) — not behind the toolbar toggles. Neither package
  is a dependency here, so this is the same "adding a dependency is your call" question the lint
  module raised, with the correction that the colour picker was not an optional extra in the Vue
  editor: right-clicking an element there offered it.
- **`bg: 'grid'` registers nothing.** Vue pushed `diagram-js/lib/features/grid-snapping/visuals`
  for that setting (`modulesAndModdle.ts:134-137`); here the value only removes a CSS class. Also
  the settings panel's `miniMap` checkbox reaches nothing after the modeler is built — Vue rebuilt
  the whole modeler on any settings change (`Designer/index.tsx:24-41`), and this does not.
- **`window.addEventListener('message', ev => modeler.importXML(ev.data))`**
  (`modulesAndModdle.ts:186-188`) is the receiving half of the `postMessage` save this port
  already declined. It imports any message any frame posts, with no origin check. Not ported, and
  it should not be without one.
- **The Vue panel disappeared entirely for a Process.** `Panel/index.tsx:445` returns `null`
  unless `currentElementType !== 'Process'`, so clicking bare canvas emptied the panel. Here the
  stock panel shows the process's own groups. Kept, as an improvement rather than a difference to
  reverse — flagging it because it is visible on the first click of every session.
- **`styles/font-awesome.min.css` and `styles/style.css` are imported by nothing** —
  `styles/index.scss` lists neither, and no component references them. 6,834 lines of dead
  stylesheet carried over from the Vue tree.

#### Checked and found equivalent

`store/editor.ts` and `store/modeler.ts` are answered by `BpmnEditorService`, getter for getter,
including the `sessionStorage` language write. `additional-functions/EnhancementContextmenu.ts` is
`context-menu/ContextMenuProvider.ts`: same `element.contextmenu` hook at the same priority 2000,
same `isAppendAction` split, same +10px cursor offset, same close-on-canvas-click listener. The
enhancement palette matches entry for entry, including the two entries Vue left commented out.
`Palette/index.tsx` (the custom palette) is a Chinese-language stub that only mounts under
`paletteMode: 'custom'`, which is not the default and not what this editor uses.

## Custom icons

A user uploads an SVG, names it, and places it on the canvas like any other palette entry. The
icons live in the diagram's own XML, so they travel with the flow: whoever opens that flow next
sees the same icons, with no second store to keep in step and nothing to migrate.

This is a build rather than a port. The Vue files for this feature never ran — `CustomIconIntegration.ts`,
`customIconService.ts`, `CustomIconManager.vue`, `customIconPaletteProvider.ts` and `Toolbar/index.vue`
have no reachable importer, and reached, the palette would have asked `createShape` for a
`Custom:<name>` namespace nothing registers. What was taken from them is the intent. See "The four
`moddle-extensions` still in the Vue folder" above for how that was established.

### Where an icon is stored

`bpmn:Definitions` → `bpmn:extensionElements` → `customIcon:iconLibrary`, one library per document.
A placed shape is a `customIcon:customTask` carrying the icon's id. Real exported output:

```xml
<bpmn:definitions xmlns:bpmn="…" xmlns:bpmndi="…" xmlns:dc="…"
                  xmlns:customIcon="http://medportal.behsa.com/schema/bpmn/custom-icons"
                  id="Definitions_Process_1788748197978" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:extensionElements>
    <customIcon:iconLibrary>
      <customIcon:icon iconId="Icon_1" name="Payment" contents="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9…" />
    </customIcon:iconLibrary>
  </bpmn:extensionElements>
  <bpmn:process id="Process_1788748197978" name="processName" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" />
    <customIcon:customTask id="Activity_0ip6hnb" name="Payment" iconId="Icon_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1788748197978">
      <bpmndi:BPMNShape id="Activity_0ip6hnb_di" bpmnElement="Activity_0ip6hnb">
        <dc:Bounds x="400" y="260" width="120" height="120" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
```

(The `contents` attribute and the namespace declarations are elided; everything else is verbatim,
including the 120x120 bounds `DEFAULT_ELEMENT_SIZES` gives a `bpmn:Task`.)

Three decisions worth stating, because each has a failure mode:

- **No property is added to `bpmn:Definitions`.** It extends `bpmn:BaseElement`
  (`bpmn-moddle/resources/bpmn/json/bpmn.json`), so `extensionElements` is already there. Extending
  it is what makes `camunda` and `cdrParser` mutually exclusive — both add `diagramRelationId` and
  moddle refuses the second — and `additional-modules/index.spec.ts` holds that line for all four
  engines. `customIcons.json` declares no `extends` at all, and a test asserts it.
- **One moddle type for every icon**, `customIcon:CustomTask`, with the icon id as an attribute.
  Minting a type per icon is exactly what made the Vue palette throw.
- **The extension is always registered**, alongside the integration modules rather than with the
  engine schemas: a diagram that carries a library has to be readable whichever engine is selected.

`bpmn:Definitions` is also the only place the editor reads from. There is no module-scope registry
and no `localStorage` — the Vue version had two singletons over one `localStorage` key, in
disagreement with each other, and icons that no other user could ever see.

### Size caps

Two limits, both enforced in `CustomIconLibrary.add`, which is the only way into the library:

| Cap         | Value  | Measured on                            | Why                                                                                                                                                                                                              |
| ----------- | ------ | -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Per icon    | 32 KB  | the SVG source, in UTF-8 bytes         | The number a user can compare with the file on disk. About eight times a typical icon, with room for an editor export that kept its metadata.                                                                    |
| Per diagram | 192 KB | the stored `data:` URIs, in characters | This is what lands in `FlowEntity.flow` and is carried by every save, every load and every `/api/flows` row. ≈35 typical icons, or six at the per-icon cap, against a diagram that is otherwise a few kilobytes. |

The units differ deliberately: the per-icon message is about a file the user chose, the per-diagram
one is about what the flow will cost. base64 inflates by a third, so 192 KB stored is ≈144 KB of SVG.

### What is rejected, and what is knowingly allowed

The SVG is user-uploaded content shown to other users, so it is treated as hostile. `svg-icon.ts`
is the whole boundary; `accept=".svg"` is a filter on the file dialog and is not one of the checks.

Rejected: a source over the per-icon cap; anything `DOMParser` reports a parser error for; a root
element that is not an SVG `<svg>`; `<script>`, `<foreignObject>`, `<iframe>`, `<embed>`,
`<object>`, `<audio>`, `<video>`, `<handler>`; any attribute whose name starts with `on`, in any
case; any `href`/`xlink:href` that is not a `data:` URI; any attribute value carrying `javascript:`.

Allowed on purpose, with the reason: `<style>` elements and CSS `url(#…)` references, which are
ordinary in exported icons; SMIL animation elements, which cannot script and whose one dangerous
target — `href` — is already restricted; and `data:` URIs inside the icon, which are self-contained.

Where the icon ends up is the other half of the control. It reaches the DOM only as a `data:` URI
in an `<img src>` (the palette entry, built by diagram-js at `Palette.js:275-280`, and the dialog's
preview) and in an SVG `<image href>` on the canvas — the same way `RewriteRenderer` draws the
integration modules' corner icons. Both are image contexts: a browser runs no script in an SVG
loaded that way and resolves no external reference from it. Nothing is ever assigned as markup, and
nothing goes through `bypassSecurityTrust*`.

Two limits to be explicit about:

- The bytes are **not rewritten**. An icon that passes is stored exactly as it arrived, so the
  safety of what is stored rests on where it is used, not on the file having been sanitised.
- The parse-time rules also run on **read**, not only on upload: a `.bpmn` file is user input, and
  `readIconLibrary` drops any icon whose `contents` is not the exact encoding this module produces
  or whose decoded SVG would not have passed. That costs one parse per import, cached thereafter.

### An icon a diagram does not have

`customIcon:customTask` keeps its `iconId` whether or not the library still holds it. A shape whose
icon is missing — removed here, or a hand-edited file — draws a dashed outline carrying
`custom-icon-missing` plus the element's own name, and the diagram opens normally. Removing an icon
that is in use therefore leaves the task in place rather than deleting it; the command reports every
`customIcon:CustomTask` as changed, which is what makes those shapes redraw as placeholders.

Every change to the library goes through one command (`custom-icons.updateLibrary`), so uploads and
removals are undoable and mark the diagram dirty like any other edit.

### Not carried over from the Vue files

- **Per-icon "parameters"** (`CustomIconUpload.vue:41-73`). They were stored on the element and read
  by nothing. Module properties are what configure a shape here.
- **Generated CSS classes** (`customIconRegistry-fixed.ts:78-113`), which wrote a `<style>` element
  per icon. diagram-js already renders a palette entry's `imageUrl` as an `<img>`.
- **`styles/custom-icons.scss`** stays unimported and untouched. It styles naive-ui modal internals;
  the Angular dialog has its own scoped stylesheet. It is now dead with no feature waiting on it —
  say the word and it goes.
- **`moddle-extensions/customIconModule.json`** stays out. It declares prefix `custom` with a single
  `CustomElement` type, which is neither the shape the palette placed nor anything this design uses.

## The 47px, fixed

The bug recorded above under "One bug found and not fixed" is fixed. Measured on `/bpmn-editor` at
1280x720, before and after:

|                                            | before  | after  |
| ------------------------------------------ | ------- | ------ |
| navbar bottom                              | 46.375  | 46.375 |
| `#designer-container` top                  | 47.375  | 46.375 |
| `#designer-container` bottom               | 767.375 | 720    |
| canvas (`.bpmn-canvas`) bottom             | 767.375 | 720    |
| properties panel (`jhi-panel`) bottom      | 767.375 | 720    |
| `document.documentElement.scrollHeight`    | 720     | 720    |
| `window.scrollY` after `scrollTo(0, 5000)` | 0       | 0      |

The same 47.375px overhang measured at 500x640 (bottom 687.375 against a 640px viewport) and at
360x640. After the change the editor ends on the viewport bottom at 1280x720, 1440x900, 500x640 and
360x640, and the document still has nothing to scroll — it no longer needs to.

### Why the shell change is editor-only

The earlier note said the fix "affects every route". It does not have to. `main.component.ts:62`
already sets `fullScreen = path.includes('/bpmn-editor')`, and `main.component.html` already
branches on it (`card` versus `card jh-card`, and no footer). The fix rides that branch:
`.app-root` gains `fullscreen-mode` from `[class.fullscreen-mode]="fullScreen"`, and every new rule
in `main.component.scss` is nested under that class. Nothing outside `/bpmn-editor` matches it.

### The chain, and the two host elements in the middle

`height: 100%` needs a definite height at every step, and the route has two Angular host elements
between the shell's `.card` and the editor's container — both `display: inline; height: auto` by
default, both invisible in the template:

```
body > jhi-main > .app-root.fullscreen-mode > main > .card
     > jhi-flow-bpmn-editor > jhi-bpmn-editor > #designer-container
```

Missing either one is what makes the percentage silently fall back to the content height. With
`fullscreen-mode` in place but the host elements left alone, `.card` measured the right 673.625px
and the container inside it measured 500px — a 173.6px gap instead of a 47px overhang. So:

- `layouts/main/main.component.scss` — `.fullscreen-mode` is a `100vh` flex column; `> main` and
  `> main > .card` are `flex: 1 1 auto; min-height: 0`. The `.card` border is zeroed on this branch
  only, which is the 1px that held the editor below the navbar.
- `components/flow/flow-bpmn-editor.component.scss` (new) — a `:host` rule making the routed
  component the flex item that carries that room. Emulated encapsulation, so it is this component
  and nothing else.
- `components/bpmn-editor.component.scss` — the same for `jhi-bpmn-editor`, and
  `.designer-container` is `height: 100%` rather than `100vh`.
- `styles/index.scss` — `body, html` left the selector list. `ViewEncapsulation.None` made those
  global rules, and the `height: 100vh; overflow: hidden` on them is what made the missing 47px
  unscrollable as well as off-screen. The shell now sizes the route, so the document needs no help.

### What was measured on the other routes

Every number below is identical before and after the change, at 1280x720 with the same mocks:

- **product list, 60 rows** — `scrollHeight` 3265 against a 720 client height, `scrollY` 2545 after
  an instant `scrollTo`, last row bottom 607.09, footer top 720.42. It scrolls to the end.
- **login** (`isLoginPage` branch) — `.app-root` 745.656px tall, `scrollHeight` 746, `scrollY` 26;
  and at 360x480, `scrollHeight` 697 with `scrollY` 217 and the submit button reachable.
- **home**, **`/admin/user-management`** — both shorter than the viewport (`scrollHeight` 720),
  `bodyOverflow` visible, footer present and on screen.
- **404** — `scrollHeight` 740, `scrollY` 20 after scrolling.

The `bpmn-editor.component.scss` budget warning moved from 58.73 kB to **58.81 kB** — the comments
this change adds. The warning is pre-existing and unrelated to the size; see "What this does to the
budget" above for why the number is library CSS rather than ours.

Note that `scroll-behavior: smooth` is set on the document, so a plain `window.scrollTo` has not
landed by the time the next line reads `scrollY`; every reading above used `behavior: 'instant'`.
That is the difference between measuring the scroll and always reading 0.

### Pinned by

- `e2e/playwright/bpmn-editor.e2e.spec.ts` — "the editor ends at the bottom of a WxH viewport", at
  1280x720 and 500x640. It compares the canvas and panel bottoms to `window.innerHeight` and the
  container top to the navbar bottom, so it fails on the overhang (767.375) _and_ on the gap
  (546.375) that a half-applied fix produces. Both failures were reproduced deliberately.
- `e2e/playwright/medportal.e2e.spec.ts` — "a list longer than the viewport still scrolls to its
  last row". Making the shell rule global (`.app-root` instead of `.fullscreen-mode`) fails it:
  `scrollHeight` collapses from 3265 to 720.

### RTL, measured rather than reasoned about

This application ships Persian (`config/language.constants.ts`) and `MainComponent` writes `dir` on
`<html>` from it (`main.component.ts:84-88`), so RTL is a direction users actually run the editor
in. The height chain is a column flex, which is direction-neutral by construction — but that was an
argument, not a measurement. Measured at 1280x720, LTR against RTL:

|                                     | LTR         | RTL         |
| ----------------------------------- | ----------- | ----------- |
| navbar bottom                       | 46.375      | 46.375      |
| `#designer-container` top           | 46.375      | 46.375      |
| `#designer-container` bottom        | 720         | 720         |
| canvas bottom / panel bottom        | 720 / 720   | 720 / 720   |
| `scrollHeight` / `clientHeight`     | 720 / 720   | 720 / 720   |
| canvas left / properties panel left | 0 / 930     | 350 / 0     |
| `scrollWidth` / `clientWidth`       | 1280 / 1280 | 1280 / 1280 |

Every vertical number is identical, and the layout mirrors horizontally without overflowing.

**One thing did not mirror.** The 1px rules that separate the panes were physical — `border-left`
on the properties panel, `border-right` on the palette. Measured in RTL, `jhi-panel` moved to the
left of the canvas but kept `border-left: 1px` / `border-right: 0px`, so the divider between panel
and canvas disappeared and a stray line sat on the outer edge of the window instead. The six
direction-sensitive borders in this editor are now logical properties — `border-inline-start` /
`border-inline-end`, and `padding-inline-start` for the toolbar's group separator — which put the
line on the edge that faces the canvas in either direction. Re-measured: `border-right: 1px` and
`border-left: 0px` on the panel in RTL, unchanged in LTR.

Pinned by "the editor keeps its geometry in RTL, and its dividers follow the flip" in
`bpmn-editor.e2e.spec.ts`. Falsified by restoring `border-left` on `jhi-panel`: it fails on
`expect(rtl.panelBorderRight).toBe('1px')` with `0px`.

### Not addressed here

`layouts/footer/footer.component.html` is a zero-byte file, so `<jhi-footer>` renders an empty box.
The element is in the document and in the right place; there is simply nothing in it. Empty since
the `7f0f6df` baseline, so nothing here broke it. Unrelated to this change and left alone.

**Three English-side content questions**, surfaced while translating and deliberately not touched —
`en` is the source language, and changing it is a product decision rather than a translation one:

- `i18n/en/reportLogs.json` → `medPortalApp.reportLogs.properties` reads `"P roperties"`, with a
  stray space. The Persian is `خواص`.
- `i18n/en/flow.json` → `medPortalApp.flow.home.createLabelFor` reads `"Create a new Flow For "`
  with a trailing space. That space is load-bearing: `flow-new.component.html` puts the product
  name in a second inline `<span>` immediately after it, so it is the only separator between the
  label and the name. The Persian had lost it and now carries it too.
- `metrics.jvm.http.title` said `"HTTP requests (time in millisecond)"` in English and
  `"HTTP requests (events per second)"` in Persian — a genuine disagreement about what the panel
  measures, not a translation slip. **Settled: the English is right.** The panel is rendered by
  `admin/metrics/blocks/metrics-request/metrics-request.component.html`, whose columns are Code,
  Count, Mean and Max, and those numbers come from `JHipsterMetricsEndpoint.httpRequestsMetrics`
  in `tech.jhipster:jhipster-framework:9.1.0`, which builds them as
  `Timer.totalTime(TimeUnit.MILLISECONDS)` and `Timer.max(TimeUnit.MILLISECONDS)` (read from the
  bytecode). So the values really are milliseconds of latency per HTTP status code, and "events
  per second" was simply wrong. The Persian now reads
  `درخواست‌های HTTP (زمان به میلی‌ثانیه)`; no English-side change is needed.

### Persian, now actually loaded

Previously reported here as unaddressed, and since fixed. `webpack.custom.js` merged only
`./src/main/webapp/i18n/en/*.json` into a bundle; the `fa` entry had never been added at the
`jhipster-needle-i18n-language-webpack` line directly below it, even though
`src/main/webapp/i18n/fa/` holds 28 translation files and `fa` is in `LANGUAGES`. So `i18n/fa.json`
404'd, the ngx-translate loader rejected, `onLangChange` never fired, and picking Persian changed
neither the text nor the direction.

`fa` is now registered at that needle (the needle itself stays last in the array, where a JHipster
regeneration writes). `MergeJsonWebpackPlugin` produces `i18n/fa.json` from all 28 files — 511 leaf
keys, deep-merged, verified per source file rather than by the file merely existing.

Coverage ended at **511 of 511 keys translated, up from 509 of 511**. 198 values were English text
byte-identical to their English original, and a further 12 were English text that merely differed
from it — "Create a new Custom Audit Event" against `en`'s "Create a new Audit Log", `"Usage"`,
`"Config"`, `"Properties"` — which an equality check cannot see. Both sets are translated.
`entity.validation.patternLogin` and `health.status.OUT_OF_SERVICE` were missing outright and were
added. 16 values remain byte-identical to English on purpose: the product name (`global.title`,
`home.title`), `GitHub`, the eight `p50`/`p75`/`p95`/`p99` percentile column headers, the three
`jhipster-needle-menu-add-*` markers whose own text reads "(do not translate!)", and the two
`"null": ""` blanks that are the empty option of an enum dropdown.

One Persian value could not be interpolated at all. `userManagement.delete.question` read
`{{ login  }}` with two spaces, and ngx-translate's `templateMatcher` is `/{{\s?([^{}\s]*)\s?}}/g`
— `\s?` is zero or _one_ space, so the occurrence did not match, no substitution ran, and the
delete-user dialog showed the literal braces instead of the username. Pre-existing at `4e78c5b`,
but unreachable until this change made Persian load, so it is fixed here.

Structural parity and placeholder integrity are pinned by `shared/language/i18n-parity.spec.ts`:
per file and per key it fails on a dropped or renamed `{{ placeholder }}`, on mangled inline HTML,
and — using ngx-translate's own matcher rather than an idealised one — on any `{{…}}` occurrence in
either language that the runtime would not interpolate. Comparing placeholder _names_ is not
enough: `{{ login  }}` and `{{ login }}` are both named `login`.

### نیم‌فاصله — the half-space, normalised

Persian binds some morphemes to their host word with a ZERO WIDTH NON-JOINER (U+200C, نیم‌فاصله)
rather than a space. The bundle used both forms, and the split was not the same on both sides of it.

**The verb prefix was an inconsistency, not a style.** `می` / `نمی` was already ZWNJ in the clear
majority — 16 occurrences against 8 — and `global.json` held _both spellings of the same word_:
`global.messages.validate.newpassword.maxlength` read `می‌تواند` while
`global.messages.validate.newpassword.emptyPassword`, twelve lines away, read `می تواند`. Nothing
chose between them; one of the two was simply wrong.

**The plural suffix was a real change of the dominant form**, and is the half that needed asking
for: `ها` / `های` stood at 23 spaces against 1 ZWNJ. It is now uniform.

The rule applied is narrower than "join Persian words that look joinable": **ZWNJ only where the
bound element cannot stand alone as a word.** An ordinary boundary between two free words keeps its
space, which is why `گزارش لاگ‌ها`, `همه درخواست‌ها` and `تخلیه نخ‌ها` are joined on the suffix only.
38 substitutions across 11 files, all of them U+0020 → U+200C and nothing else — every file is the
same length in characters afterwards. (That claim covers the typography pass alone. The doubled
plural fixed further down is a content change and does move characters; the two are separable in
the diff, and the totals below are the typography pass on its own.)

| Morpheme     | Why it binds                                | Before (space → ZWNJ) | After |
| ------------ | ------------------------------------------- | --------------------- | ----- |
| `ها` / `های` | plural suffix                               | 23 → 1                | 24    |
| `می` / `نمی` | imperfective / negated verb prefix          | 8 → 16                | 24    |
| `میلی`       | SI combining form (milli-), not a free word | 3 → 0                 | 3     |
| `بی`         | privative prefix (بی‌اعتبار)                | 3 → 0                 | 3     |
| `ای`         | indefinite enclitic after a silent ه        | 1 → 0                 | 1     |

The last three were not in the original brief and were found by widening the search from the two
known families to bound morphemes generally. `تر` / `ترین`, `هایی` and `ام` / `اید` / `اند` were
searched for and do not occur. Across `i18n/fa` the ZWNJ count goes 24 → 62 for this pass, and to
63 once the plural fix below removes one from `entity.action.show` and adds one to each of the two
`resourceAuthorities` labels.

**Four candidates were rejected, and the reasons matter more than the count.** `غیر امن` and
`غیر فعال` (×2) keep their space: the Academy of Persian Language prescribes `غیر` written
separately. `فیلترهای زیر` is "the filters _below_" — `زیر` is a free word here, not the prefix.
`ثبت نام` and `ثبت شده` are two free words each, so only the enclitic in `ثبت شده‌ای` was joined.
And `آدرس ایمیلی که` is the trap a looser rule falls into: `...میلی` there is the tail of `ایمیلی`
("an email"), not the milli- prefix, so every prefix rule carries a standalone-token guard.

`مجوز‌ها` is worth one note: `ز` does not join forwards, so the ZWNJ there is visually identical to
writing `مجوزها` outright. It is still the correct encoding of a bound suffix, and it keeps the
corpus uniform and machine-checkable.

No shipped value binds a suffix directly to a `{{…}}` placeholder — `entity.action.show`
deliberately does not, see below — but Persian gives every reason to write one, so where the
boundary lies is recorded rather than left to be rediscovered. `templateMatcher` is
`/{{\s?([^{}\s]*)\s?}}/g`, and U+200C is **not** in JavaScript's `\s` class (`/\s/.test('‌')` is
`false`), so a ZWNJ hard against the closing braces cannot be consumed by `\s?` and the captured key
stays exactly `otherEntity`. The asymmetry is worth knowing: `[^{}\s]` _does_ match U+200C, so the
same character one position to the left, **inside** the braces, becomes part of the key, the lookup
misses, and the raw `{{…}}` reaches the screen. The spec pins both halves and adds the live check
that no `en` or `fa` value contains a ZWNJ inside a placeholder.

Guarded per file and per key by "binds Persian suffixes and prefixes with a ZWNJ rather than a
space" in `i18n-parity.spec.ts`, which reports the file, the key, the offending substring and the
spelling it expected, plus a companion check that the joiner is a literal U+200C and never a
`‌` escape — the character comparison alone would pass a file that spelled the escape out and
rendered it as text. Falsified both ways: reverting `config.json` to `تنظیم ها` fails with
`found: "م ها", expected: "م‌ها"` on `medPortalApp.config.home.title`, and reverting `flow.json` to
`می باشد` fails with `found: " می ب", expected: " می‌ب"`.

Bytes are not pixels, so "Persian half-spaces survive the bundle and reach the DOM as U+200C" in
`medportal.e2e.spec.ts` renders it: it switches to Persian on `/module` and asserts the navbar's
`ماژول‌ها` and the row button's `نمایش تنظیم‌ها` contain the character after the whole path —
`MergeJsonWebpackPlugin` merge, HTTP fetch, decode, interpolation, DOM write. U+200C is zero-width,
so a step that dropped it would leave the glyphs where they are and a screenshot would look right.

### The doubled plural on `entity.action.show`

Pre-existing at `5038d41`, and fixed here rather than left alone. English keeps plurality **in the
label** — `Configs`, `Flows`, `Resource Authorities` — behind a bare `Show {{otherEntity}}`.
Persian had duplicated it into the template, which carried a trailing `ها` of its own. All four
call sites were wrong, in two different ways:

| call site                                         | `otherEntity` (fa) | rendered at `5038d41` |                |
| ------------------------------------------------- | ------------------ | --------------------- | -------------- |
| `module/list/module.component.html:109`           | `تنظیم ها`         | `نمایش تنظیم ها ها`   | doubled        |
| `product/list/product.component.html:95`          | `فلوها`            | `نمایش فلوها ها`      | doubled        |
| `resource/list/resource.component.html:91`        | `مجوز منبع`        | `نمایش مجوز منبع ها`  | singular label |
| `med-authority/…/med-authority.component.html:86` | `مجوز منبع`        | `نمایش مجوز منبع ها`  | singular label |

The two faults were load-bearing on each other, which is why they had to move together: dropping
the template's suffix alone would have left `resourceAuthorities` reading `نمایش مجوز منبع`,
singular against English's plural, because that suffix was the only thing supplying its plurality.
So `entity.action.show` becomes `نمایش {{otherEntity}}`, and both `resourceAuthorities` labels
(`resource.json` and `medAuthority.json` — the same string in two files) become `مجوز‌های منبع`.
`مجوز منبع` is an ezafe construction, "authority _of_ resource", so the plural attaches to the head
noun `مجوز` and the ezafe that follows it is spelled `ی`: `مجوز‌های منبع`, not `مجوز منبع‌ها`.
`module.configs` and `product.flows` were already plural and are untouched. All four now render the
plural exactly once: `نمایش تنظیم‌ها`, `نمایش فلوها`, `نمایش مجوز‌های منبع` ×2.

**Why fix a pre-existing bug in a typography commit.** Normally this would stay out of scope. But
after the ZWNJ change the doubling renders as `نمایش تنظیم‌ها‌ها`, visibly joined, and anyone
reading the diff or the running app would reasonably conclude this commit produced it. It did not —
at `5038d41` it read `نمایش تنظیم ها ها` — but shipping it here would make it look like ours.

Pinned by "entity.action.show — plurality lives in the label, once" in `i18n-parity.spec.ts`, which
**reads the call sites out of the component templates** rather than listing them, so a fifth one is
covered the day it is added; it asserts the extracted count equals the number of
`jhiTranslate="entity.action.show"` usages, so a template reformat that broke the extraction fails
loudly instead of reducing the suite to nothing. Per call site it asserts the exact rendered string,
that no `ها` is doubled, and that the label still carries the plural itself. Falsified both ways:
restoring the template's `ها` fails 8 assertions across all four sites, and de-pluralising
`resource.resourceAuthorities` back to `مجوز منبع` fails with `plural: false`. Both are needed —
restoring the suffix makes `resource` read `نمایش مجوز‌های منبع‌ها`, which is not an adjacent
`ها‌ها`, so the doubling check alone would miss it and only the exact-render assertion catches it.

Consequently the RTL test above **no longer serves the bundle itself**. It previously installed a
`page.route('**/i18n/fa.json*')` handler that read `i18n/fa/` off disk, because the real bundle did
not exist; that mock also shallow-merged the 28 files with an object spread, so the 12 files
sharing a `medPortalApp` root overwrote one another — as did the two sharing `MedPortalApp` and the
two sharing `error` — and it served 327 of 509 keys. The test now
switches language against the genuinely built bundle, and "switching to Persian through the navbar
retranslates the page and flips it to RTL" in `medportal.e2e.spec.ts` covers the switch itself:
it reads the expected strings out of `i18n/en/global.json` and `i18n/fa/global.json` and asserts
the navbar text becomes the Persian one and `document.documentElement.dir` becomes `rtl`.
Falsified by removing the `fa` line from `webpack.custom.js`: `i18n/fa.json` 404s and the test
fails on both the text and the direction.

## Future Enhancements

- [ ] Token simulation
- [ ] Color picker
- [ ] BPMN linting
- [ ] Element templates
- [ ] Advanced validation

## Troubleshooting

### Canvas not rendering

Ensure the canvas container div has width and height set. The component expects `height: 100%` and `width: 100%`, and therefore a parent with a resolved height — on `/bpmn-editor` that is the shell's `fullscreen-mode` column. See "The 47px, fixed" for the chain it depends on.

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
