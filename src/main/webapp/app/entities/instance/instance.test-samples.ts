import dayjs from 'dayjs/esm';

import { IInstance, NewInstance } from './instance.model';

export const sampleWithRequiredData: IInstance = {
  id: 8386,
  moduleName: 'initiatives Future-proofed',
  port: 'Synergized',
  moduleStatus: 'Object-based SMTP',
  lastUpdateDate: dayjs('2023-04-14'),
  hostName: 'Checking',
};

export const sampleWithPartialData: IInstance = {
  id: 29188,
  moduleName: 'implementation',
  port: 'Industrial',
  moduleStatus: 'Account Personal calculate',
  lastUpdateDate: dayjs('2023-04-14'),
  totalProcessedWork: 23100,
  totalProcessedTask: 62620,
  processedStatistics: 'indexing success Vision-oriented',
  hostName: 'Oklahoma',
};

export const sampleWithFullData: IInstance = {
  id: 69026,
  moduleName: 'Honduras',
  port: 'Clothing 2',
  moduleStatus: 'backing Granite Liechtenstein',
  lastUpdateDate: dayjs('2023-04-15'),
  threadPoolQueueSize: 72894,
  moduleStartTime: 51656,
  totalProcessedWork: 63685,
  totalProcessedTask: 70391,
  processedStatistics: 'programming SSL Analyst',
  hostName: 'parsing withdrawal',
};

export const sampleWithNewData: NewInstance = {
  moduleName: '24/365',
  port: 'Communicat',
  moduleStatus: 'Freeway Tuna Account',
  lastUpdateDate: dayjs('2023-04-14'),
  hostName: 'Dollar Operations',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
