import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getDbTransmitterMaxLifeTimeValue(element: Base): string | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:maxLifeTime`)
}

export function setDbTransmitterMaxLifeTimeValue(element: Base, dbTransmitterMaxLifeTime: string) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:maxLifeTime`]: dbTransmitterMaxLifeTime
  })
}
