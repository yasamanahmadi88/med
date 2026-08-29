import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory';
import BpmnFactory from 'bpmn-js/lib/features/modeling/BpmnFactory';
import BpmnModdle from 'bpmn-moddle';
import { Translate } from 'diagram-js/lib/i18n/translate';
import { Dimensions } from 'diagram-js/lib/core/Canvas';
import { getBusinessObject, getDi, is } from 'bpmn-js/lib/util/ModelUtil';

type ElementConfig = Record<string, Dimensions>;

class CustomElementFactory extends ElementFactory {
  _config: ElementConfig | undefined;
  constructor(config: Record<string, Dimensions>, bpmnFactory: BpmnFactory, moddle: BpmnModdle, translate: Translate) {
    super(bpmnFactory, moddle, translate);
    this._config = config;
  }

  getDefaultSize(element: any, di: any) {
    const bo = getBusinessObject(element);
    const types: string[] = Object.keys(this._config || {});
    for (const type of types) {
      if (is(bo, type)) {
        return this._config![type];
      }
    }
    return super.getDefaultSize(element, di);
  }
}

// @ts-ignore
CustomElementFactory.$inject = ['config.elementFactory', 'bpmnFactory', 'moddle', 'translate'];
// The upstream file also reassigned ElementFactory.$inject here with exactly the list bpmn-js
// already sets. That was a no-op that mutated a library class at module scope, and would have
// silently broken the base factory had bpmn-js ever changed its own list, so it is dropped.

export default CustomElementFactory;
