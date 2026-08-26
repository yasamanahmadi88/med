import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getCsvTransformerNumberOfFieldsValue(element: Base): number | null {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:numberOfFields`)
}

export function setCsvTransformerNumberOfFieldsValue(element: Base, numberOfFields: number | null) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:numberOfFields`]: numberOfFields
  })
}
