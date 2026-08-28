import { ModuleSchema } from '../schema';

/**
 * `anyProcess` — NOT an integration module. Read the type caveat below before wiring this up.
 *
 * Despite living beside the module property sets, `anyProcess` is the property group the Vue
 * editor showed for a **connection between two modules**: `components/Panel/index.tsx` pushes its
 * components under `case 'bpmn:SequenceFlow'` and nowhere else. That fits what the fields say — a
 * routing key, a predicate, a transform and an ack mode are decisions about a hop, not about a
 * node. `bo-utils/anyProcessPropertiesUtil.ts` (the aggregate helper beside the per-field ones)
 * reads the same seven properties off one element, confirming they are one element's group.
 *
 * TYPE IS A PLACEHOLDER — needs a decision before this schema is added to `moduleSchemas`.
 * `bpmn:SequenceFlow` is what the Vue source says, not a guess, but it is not one of the types the
 * palette creates, and it behaves differently from every other schema here: registering it makes
 * the group appear on *every* sequence flow in *every* diagram, including plain BPMN ones that
 * have nothing to do with the integration modules. That may well be the intent — the Vue editor
 * did exactly that — but it is a product call, not a transcription one, so it is left to the
 * caller. Nothing else in this file depends on the choice; only the `type` line changes.
 *
 * Field order is the order `index.tsx` pushed the components. `Message Type` is the eighth field:
 * it has a component and a getter/setter in the Vue project but was never imported into
 * `index.tsx`, so the panel never rendered it; it is appended last, as `fileReceiverSchema` does
 * with its orphans.
 *
 * One behaviour does not survive the port: `setAnyProcessMsgTypeValue` also assigned
 * `element.businessObject.name = msgType`, so typing a message type renamed the flow on the
 * canvas. The generic provider only writes the one namespaced property, so editing `msgType` here
 * no longer relabels the element. Flagged in the port report.
 */
export const anyProcessSchema: ModuleSchema = {
  type: 'bpmn:SequenceFlow',
  label: 'anyProcess',
  fields: [
    { name: 'ackMode', label: 'Ack Mode', kind: 'select', options: ['NO_ACK', 'REC_ACK', 'VAL_ACK', 'PRC_ACK'] },
    { name: 'status', label: 'Status', kind: 'select', options: ['OPEN', 'CLOSE'] },
    // Labelled "Properties" — the only field in the port whose label has no trailing colon in the
    // Vue markup. Stored as `properties`, plural.
    { name: 'properties', label: 'Properties', kind: 'textarea' },
    // Stored as `predicates`, plural, though the component file is `anyProcessPredicate.vue`.
    { name: 'predicates', label: 'Predicates', kind: 'textarea' },
    { name: 'transform', label: 'Transform', kind: 'textarea' },
    // The component is imported into `index.tsx` as `AnyProcessOutputMsgType` but the file is
    // `anyProcessRouteKey.vue` and the util reads `${prefix}:routeKey`. The property name wins.
    { name: 'routeKey', label: 'Route Key', kind: 'text' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
    // Never rendered by the Vue panel; see the note about the lost rename side effect above.
    { name: 'msgType', label: 'Message Type', kind: 'text' },
  ],
};
