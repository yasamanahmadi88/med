import { Dimensions } from 'diagram-js/lib/core/Canvas';

import CustomElementFactory from './CustomElementFactory';

/**
 * The shape sizes `CustomElementFactory` exists to apply.
 *
 * The Vue editor passed these as `options.elementFactory` whenever `otherModule` was on, which is
 * the default (`components/Designer/modulesAndModdle.ts:154-157`). Without them the factory reads
 * `config.elementFactory` as undefined and every lookup falls through to `super.getDefaultSize`,
 * so the whole class is a no-op — which is what it was here until this change.
 *
 * Every integration module declares `superClass: ["bpmn:Task"]` (see
 * `moddle-extensions/kafkaReceiverModule.json`), so this is the size a module is placed at: 120x120
 * rather than bpmn-js's 100x80. It is not only geometry. `RewriteRenderer` stretches the module's
 * corner icon to the shape's box (`RewriteRenderer.ts:1961-1981`) and those icons are square
 * (`content/bpmn-icons/corner-icons/kafkaReceiverModule_corner.svg` is `viewBox="0 0 551 553"`), so
 * a 100x80 shape draws every module icon squashed.
 *
 * The `'bpmn:SequenceFlow': { width: 100, height: 80 }` entry the Vue config also carried is not
 * here: `getDefaultSize` is only consulted for shapes, and `ElementFactory.createConnection` never
 * asks for one, so it changed nothing there and would change nothing here.
 */
export const DEFAULT_ELEMENT_SIZES: Record<string, Dimensions> = {
  'bpmn:Task': { width: 120, height: 120 },
};

export default {
  elementFactory: ['type', CustomElementFactory],
};
