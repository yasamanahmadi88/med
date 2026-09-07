import EventBus from 'diagram-js/lib/core/EventBus';
import ElementRegistry from 'diagram-js/lib/core/ElementRegistry';
import { Base } from 'diagram-js/lib/model';
import BpmnFactory from 'bpmn-js/lib/features/modeling/BpmnFactory';
import { is } from 'bpmn-js/lib/util/ModelUtil';
import { ModdleElement } from 'bpmn-moddle';

import {
  CUSTOM_TASK_TYPE,
  CustomIcon,
  applyIcons,
  createIconElement,
  libraryBytes,
  nextIconId,
  rawIcons,
  readIconLibrary,
} from './icon-library';
import { MAX_LIBRARY_BYTES, formatKb, validateSvgIcon } from './svg-icon';

/** The command every change to the library goes through, so it is undoable and marks the diagram dirty. */
export const UPDATE_ICON_LIBRARY = 'custom-icons.updateLibrary';

export type AddIconResult = { ok: true; icon: CustomIcon } | { ok: false; message: string };

/** What `bpmnjs` — the modeler itself, registered as a didi value at `bpmn-js/lib/BaseViewer.js:601` — is used for here. */
interface DefinitionsHolder {
  getDefinitions(): ModdleElement | undefined;
}

/**
 * diagram-js's palette rebuilds itself on `diagram.init` and on `i18n.changed` and offers no
 * public way to ask for another pass, so a library that changes after load has to reach for
 * `_rebuild` — the same method `registerProvider` calls (`Palette.js:106`). It is guarded: it
 * returns before touching the DOM if the diagram has not initialised yet.
 */
interface RebuildablePalette {
  _rebuild(): void;
}

interface UpdateContext {
  icons: ModdleElement[];
  oldIcons?: ModdleElement[];
}

/**
 * Replaces the whole library in one command.
 *
 * Whole-array rather than add/remove: the library is a handful of elements, and a command that
 * restores exactly what was there cannot drift from the diagram on undo.
 */
class UpdateIconLibraryHandler {
  static $inject = ['bpmnjs', 'bpmnFactory', 'elementRegistry'];

  constructor(
    private readonly bpmnjs: DefinitionsHolder,
    private readonly bpmnFactory: BpmnFactory,
    private readonly elementRegistry: ElementRegistry,
  ) {}

  execute(context: UpdateContext): Base[] {
    const definitions = this.bpmnjs.getDefinitions();
    if (!definitions) {
      return [];
    }
    context.oldIcons = [...rawIcons(definitions)];
    applyIcons(definitions, context.icons, this.bpmnFactory);
    return this.placedShapes();
  }

  revert(context: UpdateContext): Base[] {
    const definitions = this.bpmnjs.getDefinitions();
    if (!definitions) {
      return [];
    }
    applyIcons(definitions, context.oldIcons ?? [], this.bpmnFactory);
    return this.placedShapes();
  }

  /**
   * Every shape drawn from the library, dirty or not.
   *
   * Returning them is what makes diagram-js re-render them, and it is the whole reason removing an
   * icon does not leave a broken shape behind: the shapes that used it redraw as the placeholder.
   * Listing all of them rather than only the ones whose id changed also covers an icon replaced
   * under an id that already existed.
   */
  private placedShapes(): Base[] {
    return this.elementRegistry.filter((element: Base) => is(element, CUSTOM_TASK_TYPE));
  }
}

/**
 * The diagram's icon library, as the palette, the renderer and the upload dialog see it.
 *
 * Every read goes back to `bpmn:Definitions`, so there is one copy of the truth and it is the one
 * that gets saved — no module-scope singleton and no `localStorage`, which is where the two Vue
 * registries disagreed with each other. Reads are cached per definitions object because
 * {@link readIconLibrary} re-validates every icon it returns and the renderer asks per shape.
 */
class CustomIconLibrary {
  static $inject = ['eventBus', 'commandStack', 'bpmnjs', 'bpmnFactory', 'palette'];

  private cache: { definitions: ModdleElement | undefined; icons: CustomIcon[] } | undefined;

  constructor(
    eventBus: EventBus,
    private readonly commandStack: any,
    private readonly bpmnjs: DefinitionsHolder,
    private readonly bpmnFactory: BpmnFactory,
    palette: RebuildablePalette,
  ) {
    commandStack.registerHandler(UPDATE_ICON_LIBRARY, UpdateIconLibraryHandler);

    // Importing a diagram brings a different library with it, and our own command changes this
    // one; both have to reach the palette, which otherwise only rebuilds at startup. Not
    // `commandStack.changed`: that fires on every edit, and `_rebuild` forces the palette open.
    for (const event of ['import.done', `commandStack.${UPDATE_ICON_LIBRARY}.executed`, `commandStack.${UPDATE_ICON_LIBRARY}.reverted`]) {
      eventBus.on(event, () => {
        this.cache = undefined;
        palette._rebuild();
      });
    }
  }

  /** The icons this diagram carries, in the order they are stored. */
  getIcons(): CustomIcon[] {
    const definitions = this.bpmnjs.getDefinitions();

    if (!this.cache || this.cache.definitions !== definitions) {
      this.cache = { definitions, icons: readIconLibrary(definitions) };
    }

    return this.cache.icons;
  }

  getIcon(id: string | undefined): CustomIcon | undefined {
    return id ? this.getIcons().find(icon => icon.id === id) : undefined;
  }

  /** Characters the library still has room for, against {@link MAX_LIBRARY_BYTES}. */
  remainingBytes(): number {
    return Math.max(0, MAX_LIBRARY_BYTES - libraryBytes(this.getIcons()));
  }

  /**
   * Validate an uploaded SVG and, if it passes, store it in the diagram.
   *
   * The only way into the library, so no caller can skip the checks: the file picker's `accept`
   * filter is not one of them.
   */
  add(name: string, svgSource: string): AddIconResult {
    const label = name.trim();
    if (!label) {
      return { ok: false, message: 'Give the icon a name.' };
    }

    const validation = validateSvgIcon(svgSource);
    if (!validation.ok) {
      return validation;
    }

    const icons = this.getIcons();
    const size = validation.dataUri.length;
    if (size > this.remainingBytes()) {
      return {
        ok: false,
        message:
          `This diagram's icons would take ${formatKb(libraryBytes(icons) + size)} of the ${formatKb(MAX_LIBRARY_BYTES)} a diagram may carry. ` +
          `Remove an icon first, or use a smaller file.`,
      };
    }

    const icon: CustomIcon = { id: nextIconId(icons), name: label, contents: validation.dataUri };
    this.update([...this.storedIcons(), createIconElement(this.bpmnFactory, icon)]);

    return { ok: true, icon };
  }

  /**
   * Drop an icon from the library.
   *
   * Shapes already placed from it keep their `iconId` and redraw as the missing-icon placeholder,
   * rather than being deleted out from under the user — removing an icon is a library edit, not a
   * diagram edit, and silently deleting somebody's task is not a recoverable surprise.
   */
  remove(id: string): void {
    this.update(this.storedIcons().filter(icon => icon.get('iconId') !== id));
  }

  /** The stored elements, filtered to the ones {@link getIcons} accepted, so a rejected icon is dropped on the next write. */
  private storedIcons(): ModdleElement[] {
    const kept = new Set(this.getIcons().map(icon => icon.id));
    return rawIcons(this.bpmnjs.getDefinitions()).filter(icon => kept.has(icon.get('iconId')));
  }

  private update(icons: ModdleElement[]): void {
    this.commandStack.execute(UPDATE_ICON_LIBRARY, { icons });
  }
}

export default CustomIconLibrary;
