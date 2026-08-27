import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject, is } from 'bpmn-js/lib/util/ModelUtil'

export function getAnyProcessPropertiesValue(element: Base): string | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(
    `${prefix}:ackMode && ${prefix}:predicates && ${prefix}:properties && ${prefix}:transform && ${prefix}:status && ${prefix}:msgType && ${prefix}:outputMsgType`
  )
}
export function getInitiatorMyValue(element: Base): string | undefined {

  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(
    `${prefix}:ackMode && ${prefix}:predicates && ${prefix}:properties && ${prefix}:transform && ${prefix}:status && ${prefix}:msgType && ${prefix}:outputMsgType`
  )
}

export function setAnyProcessPropertiesValue(
  element: Base,
  ackMode: string | undefined,
  predicates: string | undefined,
  properties: string | undefined,
  transform: string | undefined,
  status: string | undefined,
  msgType: string | any,
  outputMsgType: string | undefined
) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)

  element.businessObject.name = msgType
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:ackMode`]: ackMode,
    [`${prefix}:predicates`]: predicates,
    [`${prefix}:properties`]: properties,
    [`${prefix}:transform`]: transform,
    [`${prefix}:status`]: status,
    [`${prefix}:msgType`]: msgType,
    [`${prefix}:outputMsgType`]: outputMsgType
  })
}

export function isStartInitializable(element: Base): any {
  const prefix = editor().getProcessEngine
  return (
    is(
      element,
      `${prefix}:ackMode && ${prefix}:predicates && ${prefix}:properties && ${prefix}:transform && ${prefix}:status && ${prefix}:msgType && ${prefix}:outputMsgType`
    ) && !is(element.parent, 'bpmn:SubProcess')
  )
}
