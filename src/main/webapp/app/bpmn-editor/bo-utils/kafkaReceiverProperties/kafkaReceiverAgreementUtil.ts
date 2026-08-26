import { Base } from 'diagram-js/lib/model'
import editor from '@/store/editor'
import modeler from '@/store/modeler'
import { getBusinessObject, is } from 'bpmn-js/lib/util/ModelUtil'


export function getKafkaReceiverAgreementValue(element: Base): string | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:agreementMode`)
}
export function getInitiatorMyValue(element: Base): string | undefined {
  const prefix = editor().getProcessEngine
  const businessObject = getBusinessObject(element)
  return businessObject.get(`${prefix}:agreementMode`)
}

export function setKafkaReceiverAgreementValue(element: Base, kafkaReceiverAgreement: any) {
  const prefix = editor().getProcessEngine
  const modeling = modeler().getModeling
  const businessObject = getBusinessObject(element)
  modeling.updateModdleProperties(element, businessObject, {
    [`${prefix}:agreementMode`]: kafkaReceiverAgreement
  })
}

export function isStartInitializable(element: Base): any {
  const prefix = editor().getProcessEngine
  return (
    is(element, `${prefix}:agreementMode`) &&
    !is(element.parent, 'bpmn:SubProcess')
  )
}
