interface ColorDefinition {
  label: string;
  fill?: string;
  stroke?: string;
}

const DEFAULT_COLORS: ColorDefinition[] = [
  { label: 'Default', fill: undefined, stroke: undefined },
  { label: 'Blue', fill: '#BBDEFB', stroke: '#0D4372' },
  { label: 'Orange', fill: '#FFE0B2', stroke: '#6B3C00' },
  { label: 'Green', fill: '#C8E6C9', stroke: '#205022' },
  { label: 'Red', fill: '#FFCDD2', stroke: '#831311' },
  { label: 'Purple', fill: '#E1BEE7', stroke: '#5B176D' },
];

const COLOR_ICON = `<svg xmlns="http://www.w3.org/2000/svg" width="22" height="22"><path d="m12.5 5.5.3-.4 3.6-3.6c.5-.5 1.3-.5 1.7 0l1 1c.5.4.5 1.2 0 1.7l-3.6 3.6-.4.2v.2c0 1.4.6 2 1 2.7v.6l-1.7 1.6c-.2.2-.4.2-.6 0L7.3 6.6a.4.4 0 0 1 0-.6l.3-.3.5-.5.8-.8c.2-.2.4-.1.6 0 .9.5 1.5 1.1 3 1.1zm-9.9 6 4.2-4.2 6.3 6.3-4.2 4.2c-.3.3-.9.3-1.2 0l-.8-.8-.9-.8-2.3-2.9" /></svg>`;
const COLOR_ICON_URL = `data:image/svg+xml;utf8,${encodeURIComponent(COLOR_ICON)}`;

export class ColorContextPadProvider {
  static $inject = ['contextPad', 'popupMenu', 'canvas', 'translate'];

  constructor(
    private readonly contextPad: any,
    private readonly popupMenu: any,
    private readonly canvas: any,
    private readonly translate: (label: string) => string,
  ) {
    this.contextPad.registerProvider(this);
  }

  getContextPadEntries(element: unknown): Record<string, unknown> {
    return this.createPopupAction([element]);
  }

  getMultiElementContextPadEntries(elements: unknown[]): Record<string, unknown> {
    return this.createPopupAction(elements);
  }

  private createPopupAction(elements: unknown[]): Record<string, unknown> {
    return {
      'set-color': {
        group: 'edit',
        className: 'bpmn-icon-color',
        title: this.translate('Set Color'),
        imageUrl: COLOR_ICON_URL,
        action: {
          click: (event: { x: number; y: number }) => {
            const position = {
              ...this.startPosition(elements),
              cursor: { x: event.x, y: event.y },
            };

            this.popupMenu.open(elements, 'color-picker', position);
          },
        },
      },
    };
  }

  private startPosition(elements: unknown[]): { x: number; y: number } {
    const diagramContainer: HTMLElement = this.canvas.getContainer();
    const pad: HTMLElement = this.contextPad.getPad(elements).html;
    const diagramRect = diagramContainer.getBoundingClientRect();
    const padRect = pad.getBoundingClientRect();

    return {
      x: padRect.left - diagramRect.left,
      y: padRect.top - diagramRect.top + padRect.height + 5,
    };
  }
}

export class ColorPopupProvider {
  static $inject = ['config.colorPicker', 'popupMenu', 'modeling', 'translate'];

  private readonly colors: ColorDefinition[];

  constructor(
    config: { colors?: ColorDefinition[] } | undefined,
    private readonly popupMenu: any,
    private readonly modeling: any,
    private readonly translate: (label: string) => string,
  ) {
    this.colors = config?.colors ?? DEFAULT_COLORS;
    this.popupMenu.registerProvider('color-picker', this);
  }

  getEntries(elements: unknown[]): Array<Record<string, unknown>> {
    return this.colors.map(color => ({
      title: this.translate(color.label),
      id: `${color.label.toLowerCase()}-color`,
      imageUrl: this.colorImageUrl(color),
      action: () => this.modeling.setColor(elements, color),
    }));
  }

  private colorImageUrl(color: ColorDefinition): string {
    const fill = color.fill ?? 'white';
    const stroke = color.stroke ?? 'rgb(34, 36, 42)';
    const icon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" height="100%"><rect rx="2" x="1" y="1" width="22" height="22" fill="${fill}" stroke="${stroke}"></rect></svg>`;

    return `data:image/svg+xml;utf8,${encodeURIComponent(icon)}`;
  }
}

const BpmnColorPickerModule = {
  __init__: ['colorContextPadProvider', 'colorPopupProvider'],
  colorContextPadProvider: ['type', ColorContextPadProvider],
  colorPopupProvider: ['type', ColorPopupProvider],
};

export default BpmnColorPickerModule;
