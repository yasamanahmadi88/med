import { Base } from 'diagram-js/lib/model';

/**
 * Fill and stroke for one swatch. `undefined` on both is the "Default" entry, and that is not the
 * same as white: `modeling.setColor` deletes `bioc:fill` / `bioc:stroke` from the element's DI
 * when the value is undefined, which is what returns a shape to the stock look rather than
 * painting it white.
 */
export interface ColorDefinition {
  readonly label: string;
  readonly fill?: string;
  readonly stroke?: string;
}

/** The six swatches the parity branch carried over from the Vue editor's picker. */
export const DEFAULT_COLORS: readonly ColorDefinition[] = [
  { label: 'Default', fill: undefined, stroke: undefined },
  { label: 'Blue', fill: '#BBDEFB', stroke: '#0D4372' },
  { label: 'Orange', fill: '#FFE0B2', stroke: '#6B3C00' },
  { label: 'Green', fill: '#C8E6C9', stroke: '#205022' },
  { label: 'Red', fill: '#FFCDD2', stroke: '#831311' },
  { label: 'Purple', fill: '#E1BEE7', stroke: '#5B176D' },
];

/**
 * The context-pad icon. bpmn-js's icon font has no paint glyph, so the entry carries its own
 * `imageUrl` — `className` alone would render an empty button.
 */
const COLOR_ICON =
  `<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22">` +
  `<path d="m12.5 5.5.3-.4 3.6-3.6c.5-.5 1.3-.5 1.7 0l1 1c.5.4.5 1.2 0 1.7l-3.6 3.6-.4.2v.2c0 1.4.6 2 1 2.7v.6l-1.7 1.6c-.2.2-.4.2-.6 0L7.3 6.6a.4.4 0 0 1 0-.6l.3-.3.5-.5.8-.8c.2-.2.4-.1.6 0 .9.5 1.5 1.1 3 1.1zm-9.9 6 4.2-4.2 6.3 6.3-4.2 4.2c-.3.3-.9.3-1.2 0l-.8-.8-.9-.8-2.3-2.9" />` +
  `</svg>`;

const COLOR_ICON_URL = `data:image/svg+xml;utf8,${encodeURIComponent(COLOR_ICON)}`;

/** The id the popup provider registers under, and the id the context-pad entry opens. */
const PROVIDER_ID = 'color-picker';

interface ContextPad {
  registerProvider(provider: ColorContextPadProvider): void;
  getPad(target: Base | Base[]): { html: HTMLElement };
}

interface PopupMenu {
  registerProvider(id: string, provider: ColorPopupProvider): void;
  open(target: Base | Base[], providerId: string, position: { x: number; y: number; cursor: { x: number; y: number } }): void;
}

interface Canvas {
  getContainer(): HTMLElement;
}

interface Modeling {
  setColor(elements: Base | Base[], colors: ColorDefinition): void;
}

type Translate = (label: string) => string;

interface ContextPadEntry {
  group: string;
  className: string;
  title: string;
  imageUrl: string;
  action: { click(event: { x: number; y: number }): void };
}

interface PopupMenuEntry {
  id: string;
  title: string;
  imageUrl: string;
  action(): void;
}

/**
 * Adds one "Set Color" button to the element context pad.
 *
 * This is the *additive* provider shape, which is the difference between this module and the two
 * `ContextPad` variants the Vue project left behind (see "The six `additional-modules` left over"
 * in the module README). diagram-js merges every registered context-pad provider, so returning a
 * single entry here leaves delete, connect and append in place; the Vue variants returned `{}`,
 * and the `rewrite` one did so under the stock provider name, which emptied the pad.
 */
export class ColorContextPadProvider {
  static $inject = ['contextPad', 'popupMenu', 'canvas', 'translate'];

  constructor(
    private readonly contextPad: ContextPad,
    private readonly popupMenu: PopupMenu,
    private readonly canvas: Canvas,
    private readonly translate: Translate,
  ) {
    this.contextPad.registerProvider(this);
  }

  getContextPadEntries(element: Base): Record<string, ContextPadEntry> {
    return this.entryFor([element]);
  }

  /**
   * The same entry for a multi-selection. diagram-js calls this instead of
   * `getContextPadEntries` when the pad's target is an array (`ContextPad.js:183-184`), and
   * `modeling.setColor` already takes a list, so one selection paints in one command — and
   * undoes in one.
   */
  getMultiElementContextPadEntries(elements: Base[]): Record<string, ContextPadEntry> {
    return this.entryFor(elements);
  }

  private entryFor(elements: Base[]): Record<string, ContextPadEntry> {
    return {
      'set-color': {
        group: 'edit',
        className: 'bpmn-icon-color',
        title: this.translate('Set Color'),
        imageUrl: COLOR_ICON_URL,
        action: {
          click: (event: { x: number; y: number }): void => {
            this.popupMenu.open(elements, PROVIDER_ID, {
              ...this.padPosition(elements),
              // diagram-js keeps the menu inside the viewport by measuring from the cursor, so
              // this is required even though the anchor below decides where it opens.
              cursor: { x: event.x, y: event.y },
            });
          },
        },
      },
    };
  }

  /**
   * Anchor the menu just below the pad, in coordinates relative to the diagram container —
   * which is what `popupMenu.open` expects, and why both rects are measured rather than only
   * the pad's.
   */
  private padPosition(elements: Base[]): { x: number; y: number } {
    const diagramRect = this.canvas.getContainer().getBoundingClientRect();
    const padRect = this.contextPad.getPad(elements).html.getBoundingClientRect();

    return {
      x: padRect.left - diagramRect.left,
      y: padRect.top - diagramRect.top + padRect.height + 5,
    };
  }
}

/**
 * The swatch list behind that button.
 *
 * Registered as a popup-menu provider rather than built as an Angular component so the menu
 * inherits diagram-js's own positioning, outside-click handling and keyboard navigation — the
 * same reason the right-click menu defers to `bpmn-replace`.
 */
export class ColorPopupProvider {
  static $inject = ['config.colorPicker', 'popupMenu', 'modeling', 'translate'];

  private readonly colors: readonly ColorDefinition[];

  constructor(
    config: { colors?: readonly ColorDefinition[] } | undefined,
    private readonly popupMenu: PopupMenu,
    private readonly modeling: Modeling,
    private readonly translate: Translate,
  ) {
    // didi passes `undefined` for a `config.*` key nothing sets, which is the normal case here:
    // no caller configures `colorPicker`, so the defaults are what ship.
    this.colors = config?.colors ?? DEFAULT_COLORS;
    this.popupMenu.registerProvider(PROVIDER_ID, this);
  }

  getEntries(elements: Base[]): PopupMenuEntry[] {
    return this.colors.map(color => ({
      id: `${color.label.toLowerCase()}-color`,
      title: this.translate(color.label),
      imageUrl: swatchUrl(color),
      action: (): void => this.modeling.setColor(elements, color),
    }));
  }
}

/**
 * The swatch itself, as a data URI.
 *
 * `encodeURIComponent` is what makes this safe to build by interpolation: it percent-encodes the
 * quotes and angle brackets that would otherwise let a colour string close the `rect` and add
 * markup of its own. The colours are compile-time constants today, so this is defence for the
 * `config.colorPicker` hook rather than for anything currently reachable.
 */
function swatchUrl(color: ColorDefinition): string {
  const fill = color.fill ?? 'white';
  const stroke = color.stroke ?? 'rgb(34, 36, 42)';
  const icon =
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" height="100%">` +
    `<rect rx="2" x="1" y="1" width="22" height="22" fill="${fill}" stroke="${stroke}"></rect>` +
    `</svg>`;

  return `data:image/svg+xml;utf8,${encodeURIComponent(icon)}`;
}

const BpmnColorPickerModule = {
  __init__: ['colorContextPadProvider', 'colorPopupProvider'],
  colorContextPadProvider: ['type', ColorContextPadProvider],
  colorPopupProvider: ['type', ColorPopupProvider],
};

export default BpmnColorPickerModule;
