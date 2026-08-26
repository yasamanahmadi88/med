import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil'

export function getFileReceiverDirectoryPathValue(element: Base): string | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:directoryPath`)
}
export function setFileReceiverDirectoryPathValue(
  element: Base,
  directoryPath: string | undefined
) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:directoryPath`]: directoryPath
  })
}