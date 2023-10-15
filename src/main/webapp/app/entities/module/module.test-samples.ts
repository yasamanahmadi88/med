import { IModule, NewModule } from './module.model';

export const sampleWithRequiredData: IModule = {
  id: 49866,
  moduleName: 'Fantastic end-to-end Central',
  defaultPort: 'pane',
  redisKeyPrefix: 'exper',
  status: 1,
  loggingMode: 'partnerships',
  dnsName: 'function high-level',
};

export const sampleWithPartialData: IModule = {
  id: 20148,
  moduleName: 'compressing Practical Senior',
  defaultPort: 'mage',
  redisKeyPrefix: 'IB pa',
  status: 0,
  loggingMode: 'eyeballs indexing tan',
  dnsName: 'intranet',
};

export const sampleWithFullData: IModule = {
  id: 1620,
  moduleName: 'Fresh interactive Baby',
  defaultPort: 'succ',
  redisKeyPrefix: 'Small',
  status: 0,
  loggingMode: 'Planner Licensed Savings',
  loggingFilter: 'withdrawal Concrete',
  dnsName: 'Minnesota',
};

export const sampleWithNewData: NewModule = {
  moduleName: 'alarm cohesive',
  defaultPort: 'Camb',
  redisKeyPrefix: 'Inter',
  status: 1,
  loggingMode: 'North Sleek',
  dnsName: 'Directives calculating',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
