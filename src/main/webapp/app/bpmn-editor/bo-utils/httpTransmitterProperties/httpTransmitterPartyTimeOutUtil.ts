import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getHttpTransmitterPartyTimeOutValue(element: Base): number {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:partyTimeOut`)
}
export function setHttpTransmitterPartyTimeOutValue(element: Base, partyTimeOut: number) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:partyTimeOut`]: partyTimeOut
  })
}
