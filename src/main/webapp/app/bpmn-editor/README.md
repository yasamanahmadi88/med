# BPMN Editor Module

This module provides a BPMN (Business Process Model and Notation) diagram editor integrated into the Angular application. It's a migration of the Vue.js SeenTa-BPMN-customize-property project to Angular.

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
  ]
})
export class YourModule { }
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

## Migration Notes

This module is a migration from the Vue.js project "SeenTa-BPMN-customize-property" to Angular. Key changes:

- Vue components → Angular standalone components
- Pinia stores → Angular services with RxJS observables
- Vue 3 composition API → Angular lifecycle hooks and component architecture
- SCSS/CSS styles → Scoped Angular component styles

## Dependencies

Main BPMN.js libraries used:

- `bpmn-js`: Core BPMN modeler
- `bpmn-js-properties-panel`: Properties panel for editing element properties
- `diagram-js`: Diagram drawing and manipulation library
- `camunda-bpmn-moddle`: Camunda BPMN extensions support

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
