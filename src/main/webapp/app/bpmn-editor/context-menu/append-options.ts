import * as replaceOptions from 'bpmn-js/lib/features/replace/ReplaceOptions';

/**
 * The elements the "create element" menu offers when the canvas itself is right-clicked.
 *
 * bpmn-js has nothing to delegate to here: its `bpmn-replace` menu answers "what can this
 * element become", and asked about a Process it is empty. So this list is ours — but the
 * entries in it are bpmn-js's own `ReplaceOptions` data, not a reimplementation of its logic,
 * and the four groups are the ones the Vue editor offered.
 */
export interface AppendOption {
  readonly label: string;
  readonly actionName: string;
  readonly className: string;
  readonly target: { readonly type: string; readonly isExpanded?: boolean; readonly eventDefinitionType?: string };
}

export function appendOptions(): AppendOption[] {
  const { START_EVENT, TASK, GATEWAY, BOUNDARY_EVENT } = replaceOptions;

  return [...START_EVENT, ...TASK, ...GATEWAY, ...BOUNDARY_EVENT].filter(
    // Older bpmn-js gave some entries a function label, which has nowhere to render. Every
    // entry in the pinned version is a string; this keeps a bad upgrade from printing
    // "function () {…}" into the menu.
    (entry): entry is AppendOption => typeof entry.label === 'string',
  );
}
