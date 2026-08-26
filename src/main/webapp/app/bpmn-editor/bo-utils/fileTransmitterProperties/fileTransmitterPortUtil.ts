import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getFileTransmitterPortValue(element: Base): string | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:port`)
}
export function setFileTransmitterPortValue(
  element: Base,
  port: string | undefined
) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:port`]: port
  })
}
