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
