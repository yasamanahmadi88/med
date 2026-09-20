import { isAny } from 'bpmn-js/lib/util/ModelUtil';
import { Base } from 'diagram-js/lib/model';

const PRIORITY = 2000;
const CURSOR_OFFSET = 10;

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
  readonly enabled?: boolean;
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

        this.openAppendMenu(originalEvent);
        return;
      }

      this.openReplaceMenu(element, originalEvent);
    });

    /*
     * diagram-js raises element.contextmenu for BPMN elements, but an actually
     * empty part of the canvas may have no BPMN element at all.
     *
     * Handle that native contextmenu here.  Anything belonging to an element,
     * palette, popup, context pad or minimap is deliberately ignored so the
     * normal diagram-js handling continues to own those areas.
     */
    const container = this.canvas.getContainer();

    container.addEventListener('contextmenu', (originalEvent: MouseEvent) => {
      if (config?.custom === false) {
        return;
      }

      const target = originalEvent.target instanceof Element ? originalEvent.target : null;

      if (target?.closest('.djs-element, .djs-palette, .djs-context-pad, .djs-popup, .djs-minimap')) {
        return;
      }

      this.openAppendMenu(originalEvent);
    });
  }

  private openAppendMenu(event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();

    this.eventBus.fire('contextMenu.append.open', {
      x: event.clientX,
      y: event.clientY,
    });
  }

  private openReplaceMenu(element: Base, event: MouseEvent): void {
    if (this.popupMenu.isEmpty(element, 'bpmn-replace')) {
      return;
    }

    event.preventDefault();

    this.popupMenu.open(element, 'bpmn-replace', {
      cursor: {
        x: event.clientX + CURSOR_OFFSET,
        y: event.clientY + CURSOR_OFFSET,
      },
    });

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
