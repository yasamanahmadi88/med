import BaseRenderer from 'diagram-js/lib/draw/BaseRenderer';
import EventBus from 'diagram-js/lib/core/EventBus';
import TextRenderer from 'bpmn-js/lib/draw/TextRenderer';
import { Base, Shape } from 'diagram-js/lib/model';
import { getBusinessObject, is } from 'bpmn-js/lib/util/ModelUtil';
import { getFillColor, getLabelColor, getRoundRectPath, getStrokeColor } from 'bpmn-js/lib/draw/BpmnRenderUtil';
import { getLabel } from 'bpmn-js/lib/features/label-editing/LabelUtil';
import { append as svgAppend, attr as svgAttr, classes as svgClasses, create as svgCreate } from 'tiny-svg';
import { translate } from 'diagram-js/lib/util/SvgTransformUtil';

import CustomIconLibrary from './CustomIconLibrary';
import { CUSTOM_TASK_TYPE } from './icon-library';

/**
 * Above `EnhancementRenderer`, which registers at 3000 (`EnhancementRenderer.ts:16`), and above
 * `RewriteRenderer`, which takes the BaseRenderer default of 1000. Both answer `canRender` for
 * every `bpmn:BaseElement` and neither has a handler for this type, so whichever of them the
 * `rendererMode` setting selects, this one has to be asked first.
 */
const RENDER_PRIORITY = 4000;

const TASK_BORDER_RADIUS = 10;

/** Room left around the icon, and the strip along the bottom the label is drawn in. */
const ICON_PADDING = 10;
const LABEL_BAND = 24;

const DEFAULT_FILL_COLOR = '#ffffff';
const DEFAULT_STROKE_COLOR = '#000000';
const DEFAULT_LABEL_COLOR = '#000000';

/** On the outline of a shape whose icon the diagram no longer carries. */
export const MISSING_ICON_CLASS = 'custom-icon-missing';

/** diagram-js types a shape's bounds as optional; a rendered one always has them. */
function sizeOf(element: Shape): { width: number; height: number } {
  return { width: element.width ?? 0, height: element.height ?? 0 };
}

/**
 * Draws the shapes placed from the icon library.
 *
 * A task-shaped box with the icon inside it and the label along the bottom. The icon is an
 * `<image href="data:…">` — the same way `RewriteRenderer` draws the integration modules'
 * corner icons (`RewriteRenderer.ts:1928-1938`), and the only way the stored SVG is allowed to
 * reach the DOM. It is never inlined into the canvas's own SVG tree, so nothing in it is ever
 * parsed as part of this document.
 *
 * An icon that is not in the library — removed, or a diagram opened without it — draws a dashed
 * outline and the element's own name instead of throwing. A missing icon must not cost the user
 * the rest of the diagram.
 */
class CustomIconRenderer extends BaseRenderer {
  static $inject = ['eventBus', 'textRenderer', 'customIcons'];

  constructor(
    eventBus: EventBus,
    private readonly textRenderer: TextRenderer,
    private readonly customIcons: CustomIconLibrary,
  ) {
    super(eventBus, RENDER_PRIORITY);
  }

  canRender(element: Base): boolean {
    return is(element, CUSTOM_TASK_TYPE) && !(element as { labelTarget?: Base }).labelTarget;
  }

  drawShape(parentGfx: SVGElement, element: Shape): SVGRectElement {
    const icon = this.customIcons.getIcon(getBusinessObject(element).get('iconId'));
    const { width, height } = sizeOf(element);

    const box = svgCreate('rect');
    svgAttr(box, {
      x: 0,
      y: 0,
      width,
      height,
      rx: TASK_BORDER_RADIUS,
      ry: TASK_BORDER_RADIUS,
      stroke: getStrokeColor(element, DEFAULT_STROKE_COLOR),
      strokeWidth: 2,
      fill: getFillColor(element, DEFAULT_FILL_COLOR),
      ...(icon ? {} : { strokeDasharray: '6,4' }),
    });
    if (!icon) {
      svgClasses(box).add(MISSING_ICON_CLASS);
    }
    svgAppend(parentGfx, box);

    if (icon) {
      const image = svgCreate('image');
      svgAttr(image, {
        x: ICON_PADDING,
        y: ICON_PADDING,
        width: Math.max(0, width - 2 * ICON_PADDING),
        height: Math.max(0, height - ICON_PADDING - LABEL_BAND),
        // The default `preserveAspectRatio` letterboxes rather than stretches, so an icon that is
        // not square is drawn in proportion — the integration modules' own icons are stretched to
        // the shape and only look right because they are square.
        href: icon.contents,
      });
      svgAppend(parentGfx, image);
    }

    this.drawLabel(parentGfx, element);

    return box;
  }

  getShapePath(shape: Shape): string {
    return getRoundRectPath(shape, TASK_BORDER_RADIUS);
  }

  /**
   * The element's name, along the bottom.
   *
   * Below the icon rather than over it: diagram-js's text layout implements `top` and `middle`
   * vertically and nothing else (`diagram-js/lib/util/Text.js:312-329`), so the band is laid out
   * as its own box and moved into place.
   */
  private drawLabel(parentGfx: SVGElement, element: Shape): void {
    const { width, height } = sizeOf(element);
    const text = this.textRenderer.createText(getLabel(element) || '', {
      box: { x: 0, y: 0, width, height: LABEL_BAND },
      align: 'center-middle',
      padding: 2,
      style: { fill: getLabelColor(element, DEFAULT_LABEL_COLOR, DEFAULT_STROKE_COLOR) },
    });

    svgClasses(text).add('djs-label');
    translate(text, 0, height - LABEL_BAND);
    svgAppend(parentGfx, text);
  }
}

export default CustomIconRenderer;
