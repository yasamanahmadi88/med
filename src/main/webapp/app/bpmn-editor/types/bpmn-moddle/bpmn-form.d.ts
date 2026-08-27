declare interface ScriptForm extends BpmnScript {
  scriptType?: string
}

declare interface ExecutionListenerForm extends BpmnExecutionListener {
  type: string
  script?: ScriptForm
}
declare interface AryanForm extends BpmnAryan {
  type: string
  script?: ScriptForm
}
declare interface CdrParserPropertiesForm extends CdrParserProperties {
  agreementMode: string
  batchMode: string
  batchCdrType: string
}

declare interface FormItemVisible {
  listenerType: string
  scriptType: string
}

declare interface ConditionalForm {
  conditionType?: string
  expression?: string
  scriptType?: string
  language?: string
  body?: string
  resource?: string
}
