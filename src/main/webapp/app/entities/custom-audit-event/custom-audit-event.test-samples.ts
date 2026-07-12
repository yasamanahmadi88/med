import dayjs from 'dayjs/esm';

import { ICustomAuditEvent, NewCustomAuditEvent } from './custom-audit-event.model';

export const sampleWithRequiredData: ICustomAuditEvent = {
  id: 38942,
  principal: 'interfaces Movies',
};

export const sampleWithPartialData: ICustomAuditEvent = {
  id: 53268,
  principal: 'adapter Ohio',
  eventDate: dayjs('2023-04-17'),
};

export const sampleWithFullData: ICustomAuditEvent = {
  id: 23496,
  principal: 'quantify parse Electronics',
  eventDate: dayjs('2023-04-18'),
  eventType: 'AI Car Practical',
};

export const sampleWithNewData: NewCustomAuditEvent = {
  principal: 'Borders system',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
