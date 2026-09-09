import { Dimensions } from 'diagram-js/lib/core/Canvas';

import CustomElementFactory from './CustomElementFactory';

export const DEFAULT_ELEMENT_SIZES: Record<string, Dimensions> = {
  'bpmn:Task': { width: 120, height: 120 },
};

export default {
  elementFactory: ['type', CustomElementFactory],
};
