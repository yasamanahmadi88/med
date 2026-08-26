import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getCsvTransformerHaveHeaderValue(element: Base): any | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:haveHeader`)
}

export function setCsvTransformerHaveHeaderValue(element: Base, csvTransformerHaveHeader: any) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:haveHeader`]: csvTransformerHaveHeader
  })
}
