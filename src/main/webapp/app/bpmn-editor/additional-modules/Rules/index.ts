import CustomRules from './CustomRules';

/**
 * didi module protecting a diagram's start and end events from deletion.
 *
 * Registered under its own name rather than replacing `bpmnRules`, so bpmn-js's own rules — what
 * may connect to what, where an element may be dropped — all still apply.
 */
export default {
  __init__: ['customRules'],
  customRules: ['type', CustomRules],
};

export { CustomRules };
