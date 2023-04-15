import dayjs from 'dayjs/esm';

import { IInstance, NewInstance } from './instance.model';

export const sampleWithRequiredData: IInstance = {
  id: 8386,
  moduleName: 'initiatives Future-proofed',
  ip: 'Synergized Books',
  port: 'Object-bas',
  moduleStatus: 'Polarised synergize Vermont',
  lastUpdateDate: dayjs('2023-04-15'),
};

export const sampleWithPartialData: IInstance = {
  id: 3460,
  moduleName: 'Chief Handcrafted Manager',
  ip: 'Intranet Account',
  port: 'withdrawal',
  moduleStatus: 'bus Beauty',
  lastUpdateDate: dayjs('2023-04-15'),
};

export const sampleWithFullData: IInstance = {
  id: 52383,
  moduleName: 'firewall',
  ip: 'payment Home',
  port: 'Open-sourc',
  moduleStatus: 'payment programming',
  lastUpdateDate: dayjs('2023-04-14'),
  threadPoolQueueSize: 22174,
  moduleStartTime: 27482,
  totalProcessedWork: 99479,
  totalProcessedTask: 48798,
  processedStatistics: 'applications Generic port',
};

export const sampleWithNewData: NewInstance = {
  moduleName: '17(E.U.A.-17) Account',
  ip: 'payment Streets neutral',
  port: 'Borders in',
  moduleStatus: 'Frozen unleash quantifying',
  lastUpdateDate: dayjs('2023-04-14'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
