import Create from 'diagram-js/lib/features/create/Create';
import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory';
import BpmnFactory from 'bpmn-js/lib/features/modeling/BpmnFactory';

import CustomIconLibrary from './CustomIconLibrary';
import { CUSTOM_TASK_TYPE, CustomIcon } from './icon-library';

/** The class the palette entry carries, so the stylesheet can size an arbitrary image into a 46px square. */
export const PALETTE_ENTRY_CLASS = 'custom-icon-entry';

/** The `data-action` a custom-icon entry gets, per icon. */
export function paletteEntryId(icon: CustomIcon): string {
  return `create.custom-icon-${icon.id}`;
}

interface RegisterablePalette {
  registerProvider(provider: unknown): void;
}

/**
 * One palette entry per icon in the diagram's library.
 *
 * Registered as a second provider rather than by subclassing the palette provider in force: the
 * editor picks between the stock one, `EnhancementPaletteProvider` and `RewritePaletteProvider`
 * from `paletteMode`, and diagram-js merges the entries of every registered provider
 * (`Palette.js:115-119`). So custom icons appear whichever palette is configured, and none of the
 * three had to learn about them.
 *
 * The icon reaches the DOM as `imageUrl`, which diagram-js renders by setting `src` on an `<img>`
 * it creates (`Palette.js:275-280`) — never as markup. An SVG loaded through `<img>` is an image:
 * the browser runs no script in it and resolves no external reference from it.
 */
class CustomIconPaletteProvider {
  static $inject = ['palette', 'create', 'elementFactory', 'bpmnFactory', 'customIcons'];

  constructor(
    palette: RegisterablePalette,
    private readonly create: Create,
    private readonly elementFactory: ElementFactory,
    private readonly bpmnFactory: BpmnFactory,
    private readonly customIcons: CustomIconLibrary,
  ) {
    palette.registerProvider(this);
  }

  getPaletteEntries(): Record<string, unknown> {
    const entries: Record<string, unknown> = {};

    for (const icon of this.customIcons.getIcons()) {
      const start = (event: Event): void => this.startCreate(event, icon);

      entries[paletteEntryId(icon)] = {
        // The group the integration modules use, so custom icons land beside the other tasks
        // rather than in a section of their own.
        group: 'activity',
        className: PALETTE_ENTRY_CLASS,
        title: `Create ${icon.name}`,
        imageUrl: icon.contents,
        action: { dragstart: start, click: start },
      };
    }

    return entries;
  }

  /**
   * Place one shape of the single custom type, carrying the icon's id.
   *
   * One moddle type for every icon. The Vue palette asked for `Custom:${icon.name}` — a namespace
   * nothing registers — so `createShape` threw on the first click; see the README's port status.
   */
  private startCreate(event: Event, icon: CustomIcon): void {
    const businessObject = this.bpmnFactory.create(CUSTOM_TASK_TYPE, { iconId: icon.id, name: icon.name });
    const shape = this.elementFactory.createShape({ type: CUSTOM_TASK_TYPE, businessObject });

    this.create.start(event, shape);
  }
}

export default CustomIconPaletteProvider;
