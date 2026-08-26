import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getHttpTransmitterCacheExpiryTimeValue(element: Base): number {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:cacheExpiryTime`)
}
export function setHttpTransmitterCacheExpiryTimeValue(element: Base, cacheExpiryTime: number) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:cacheExpiryTime`]: cacheExpiryTime
  })
}
