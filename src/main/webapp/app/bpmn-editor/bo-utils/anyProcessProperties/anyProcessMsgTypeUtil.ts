import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getAnyProcessMsgTypeValue(element: Base): string | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:msgType`)
}

export function setAnyProcessMsgTypeValue(element: Base, anyProcessMsgType: any) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  element.businessObject.name = anyProcessMsgType
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:msgType`]: anyProcessMsgType
  })
}
