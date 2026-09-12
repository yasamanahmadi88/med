import { isAny } from 'bpmn-js/lib/util/ModelUtil';
import { Base } from 'diagram-js/lib/model';

/**
 * Owns the right-click on the canvas, the way the Vue editor's `EnhancementContextmenu` did.
 *
 * Two cases, and only the second needs anything of our own:
 *
 *  - **Replace** — a right-clicked element becomes another type. bpmn-js already answers this
 *    with its `bpmn-replace` popup menu, so that is what opens. The Vue editor had its own flat
 *    list here, built by reimplementing `ReplaceMenuProvider.getEntries` — 130 lines of type
 *    cascade that would drift from the library on every upgrade. The stock menu is the same
 *    choices with search, grouping and keyboard navigation on top.
 *  - **Append** — the canvas, a pool or a subprocess is right-clicked and the user wants a new
 *    element. `bpmn-replace` is empty for those, so this fires `contextMenu.append.open` on the
 *    modeler's own event bus and `ContextMenuComponent` renders the list.
 *
 * That event bus is also why no `EventEmitter` came across from the Vue project: it carried
 * `show-contextmenu` between this module and that component, and bpmn-js already has a bus
 * both sides can reach.
 */

/** The priority Vue used, above the default context-pad handling so this wins the event. */
const PRIORITY = 2000;

/** How far from the pointer the replace menu opens, matching the Vue offset. */
const CURSOR_OFFSET = 10;

/**
 * Whether right-clicking `element` means "create something new" rather than "change this".
 *
 * Ported verbatim from the Vue `isAppendAction`: these are the containers, plus the empty
 * canvas, where there is no element to replace.
 */
export function isAppendAction(element?: Base): boolean {
  return !element || isAny(element, ['bpmn:Process', 'bpmn:Collaboration', 'bpmn:Participant', 'bpmn:SubProcess']);
}

interface PopupMenu {
  open(element: Base, providerId: string, position: { cursor: { x: number; y: number } }): void;
  isEmpty(element: Base, providerId: string): boolean;
  isOpen(): boolean;
  close(): void;
}

interface EventBus {
  on(event: string, priority: number, callback: (event: any) => void): void;
  fire(event: string, data: unknown): void;
}

interface Canvas {
  getContainer(): HTMLElement;
}

interface ContextMenuConfig {
  /** Right-click is left to the browser when false. */
  readonly enabled?: boolean;
  /** When false, appending falls back to the stock menu too, which is empty for containers. */
  readonly custom?: boolean;
}

export default class ContextMenuProvider {
  static $inject = ['eventBus', 'popupMenu', 'canvas', 'config.contextMenu'];

  private readonly popupMenu: PopupMenu;
  private readonly canvas: Canvas;
  private readonly eventBus: EventBus;

  constructor(eventBus: EventBus, popupMenu: PopupMenu, canvas: Canvas, config?: ContextMenuConfig) {
    this.popupMenu = popupMenu;
    this.canvas = canvas;
    this.eventBus = eventBus;

    if (config?.enabled === false) {
      return;
    }

    eventBus.on('element.contextmenu', PRIORITY, (event: { element: Base; originalEvent: MouseEvent }) => {
      const { element, originalEvent } = event;

      if (isAppendAction(element)) {
        if (config?.custom === false) {
          return;
        }
        // The browser menu would cover ours, and the click that opened it must not immediately
        // close it again — the component listens for the next click to dismiss.
        originalEvent.preventDefault();
        originalEvent.stopPropagation();
        this.eventBus.fire('contextMenu.append.open', {
          x: originalEvent.clientX,
          y: originalEvent.clientY,
        });
        return;
      }

      this.openReplaceMenu(element, originalEvent);
    });
  }

  private openReplaceMenu(element: Base, event: MouseEvent): void {
    if (this.popupMenu.isEmpty(element, 'bpmn-replace')) {
      // Nothing to offer: leave the browser menu rather than flashing an empty panel.
      return;
    }

    event.preventDefault();
    this.popupMenu.open(element, 'bpmn-replace', {
      cursor: { x: event.clientX + CURSOR_OFFSET, y: event.clientY + CURSOR_OFFSET },
    });

    // The popup menu closes on its own for most interactions but not for a click on bare
    // canvas, which is the obvious way to dismiss it. Vue added the same listener.
    const container = this.canvas.getContainer();
    const closeOnCanvasClick = (clickEvent: Event): void => {
      const target = clickEvent.target as Element | null;
      if (this.popupMenu.isOpen() && target?.tagName === 'svg') {
        this.popupMenu.close();
      }
      container.removeEventListener('click', closeOnCanvasClick);
    };
    container.addEventListener('click', closeOnCanvasClick);
  }
}
