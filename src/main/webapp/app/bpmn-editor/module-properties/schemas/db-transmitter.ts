import { ModuleSchema } from '../schema';

/**
 * `DbTransmitter` — a JDBC/HikariCP connection: the datasource itself, then the pool tuning.
 *
 * Field order follows `case 'DbTransmitter:DbTransmitter'` in the Vue panel's
 * `components/Panel/index.tsx`, which is the only place the per-field components were composed.
 *
 * One deviation from that list: the Vue panel has `DbTransmitterOutputMsgType` commented out, so
 * the field was unreachable in the running editor even though its component and its getter/setter
 * (`dbTransmitterOutPutMsgTypeUtil.ts`, writing `outputMsgType`) both exist and are complete. It is
 * declared here, at the position the commented-out line occupies, so the module is configurable in
 * full; drop this one entry to reproduce the Vue form exactly.
 */
export const dbTransmitterSchema: ModuleSchema = {
  type: 'DbTransmitter:DbTransmitter',
  label: 'DbTransmitter',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'url', label: 'Url', kind: 'text' },
    { name: 'username', label: 'Username', kind: 'text' },
    // The Vue control was `<input type="password">` with a show/hide eye toggle. The panel has no
    // masked entry, so this renders as plain text — same property, same value, visible.
    { name: 'password', label: 'Password', kind: 'text' },
    { name: 'driverClassName', label: 'Driver Class Name', kind: 'text' },
    { name: 'minimumIdle', label: 'Minimum Idle', kind: 'number' },
    { name: 'maximumPoolSize', label: 'Maximum Pool Size', kind: 'number' },
    { name: 'idleTimeout', label: 'Idle Timeout', kind: 'number' },
    { name: 'maxLifeTime', label: 'Max Life Time', kind: 'number' },
    { name: 'connectionTimeout', label: 'Connection Timeout', kind: 'number' },
    { name: 'poolName', label: 'Pool Name', kind: 'text' },
    { name: 'outputMsgType', label: 'Output Msg Type', kind: 'text' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
