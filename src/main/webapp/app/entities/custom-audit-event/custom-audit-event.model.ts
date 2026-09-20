import dayjs from 'dayjs/esm';

export interface ICustomAuditEvent {
  id: number;
  principal?: string | null;
  eventDate?: dayjs.Dayjs | null;
  eventType?: string | null;
  data?: any;
}

export type NewCustomAuditEvent = Omit<ICustomAuditEvent, 'id'> & { id: null };
