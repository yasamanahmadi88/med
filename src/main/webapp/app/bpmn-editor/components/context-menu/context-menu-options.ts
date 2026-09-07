import * as replaceOptions from 'bpmn-js/lib/features/replace/ReplaceOptions';
import { isDifferentType } from 'bpmn-js/lib/features/popup-menu/util/TypeUtil';
import { isEventSubProcess, isExpanded } from 'bpmn-js/lib/util/DiUtil';
import { getBusinessObject, is, isAny } from 'bpmn-js/lib/util/ModelUtil';
import { Base } from 'diagram-js/lib/model';

export interface ContextMenuEntry {
  label: string;
  actionName: string;
  className: string;
  target: {
    type: string;
    [key: string]: unknown;
  };
}

const optionGroups = replaceOptions as unknown as Record<string, ContextMenuEntry[]>;
const options = (name: string): ContextMenuEntry[] => optionGroups[name] ?? [];

export const isAppendAction = (element?: Base): boolean =>
  !element || isAny(element, ['bpmn:Process', 'bpmn:Collaboration', 'bpmn:Participant', 'bpmn:SubProcess']);

export function contextMenuOptions(element?: Base): ContextMenuEntry[] {
  if (isAppendAction(element)) {
    // Match the menu users see in the Vue editor. Task and activity types remain available from
    // the palette and from Change Element; they are intentionally not offered on an empty canvas.
    return [...options('START_EVENT'), ...options('GATEWAY'), ...options('BOUNDARY_EVENT')].filter(hasTextLabel);
  }

  const currentElement = element!;
  const businessObject: any = currentElement.businessObject;
  const differentType = isDifferentType(currentElement);

  if (is(businessObject, 'bpmn:DataObjectReference')) {
    return options('DATA_OBJECT_REFERENCE');
  }

  if (is(businessObject, 'bpmn:DataStoreReference') && !is(currentElement.parent, 'bpmn:Collaboration')) {
    return options('DATA_STORE_REFERENCE');
  }

  if (is(businessObject, 'bpmn:StartEvent') && !is(businessObject.$parent, 'bpmn:SubProcess')) {
    return options('START_EVENT').filter(differentType);
  }

  if (is(businessObject, 'bpmn:Participant')) {
    return options('PARTICIPANT').filter(entry => isExpanded(currentElement) !== entry.target['isExpanded']);
  }

  if (is(businessObject, 'bpmn:StartEvent') && isEventSubProcess(businessObject.$parent)) {
    return options('EVENT_SUB_PROCESS_START_EVENT').filter(entry => {
      const isInterrupting = entry.target['isInterrupting'] !== false;
      const isInterruptingEqual = getBusinessObject(currentElement).isInterrupting === isInterrupting;
      return differentType(entry) || !isInterruptingEqual;
    });
  }

  if (is(businessObject, 'bpmn:StartEvent') && is(businessObject.$parent, 'bpmn:SubProcess')) {
    return options('START_EVENT_SUB_PROCESS').filter(differentType);
  }

  if (is(businessObject, 'bpmn:EndEvent')) {
    return options('END_EVENT').filter(entry => {
      if (entry.target['eventDefinitionType'] === 'bpmn:CancelEventDefinition' && !is(businessObject.$parent, 'bpmn:Transaction')) {
        return false;
      }
      return differentType(entry);
    });
  }

  if (is(businessObject, 'bpmn:BoundaryEvent')) {
    return options('BOUNDARY_EVENT').filter(entry => {
      if (entry.target['eventDefinitionType'] === 'bpmn:CancelEventDefinition' && !is(businessObject.attachedToRef, 'bpmn:Transaction')) {
        return false;
      }

      const cancelActivity = entry.target['cancelActivity'] !== false;
      return differentType(entry) || businessObject.cancelActivity !== cancelActivity;
    });
  }

  if (isAny(businessObject, ['bpmn:IntermediateCatchEvent', 'bpmn:IntermediateThrowEvent'])) {
    return options('INTERMEDIATE_EVENT').filter(differentType);
  }

  if (is(businessObject, 'bpmn:Gateway')) {
    return options('GATEWAY').filter(differentType);
  }

  if (is(businessObject, 'bpmn:Transaction')) {
    return options('TRANSACTION').filter(differentType);
  }

  if (isEventSubProcess(businessObject) && isExpanded(currentElement)) {
    return options('EVENT_SUB_PROCESS').filter(differentType);
  }

  if (is(businessObject, 'bpmn:SubProcess') && isExpanded(currentElement)) {
    return options('SUBPROCESS_EXPANDED').filter(differentType);
  }

  if (is(businessObject, 'bpmn:AdHocSubProcess') && !isExpanded(currentElement)) {
    return options('TASK').filter(entry => {
      const isTargetSubProcess = entry.target.type === 'bpmn:SubProcess';
      const isTargetExpanded = entry.target['isExpanded'] === true;
      return differentType(entry.target) && (!isTargetSubProcess || isTargetExpanded);
    });
  }

  if (is(businessObject, 'bpmn:FlowNode')) {
    const taskOptions = options('TASK').filter(differentType);
    return is(businessObject, 'bpmn:SubProcess') && !isExpanded(currentElement)
      ? taskOptions.filter(entry => entry.label !== 'Sub Process (collapsed)')
      : taskOptions;
  }

  return [];
}

function hasTextLabel(entry: ContextMenuEntry): boolean {
  return typeof entry.label === 'string';
}
