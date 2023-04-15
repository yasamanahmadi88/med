import { IModule, NewModule } from './module.model';

export const sampleWithRequiredData: IModule = {
  id: 49866,
  moduleName: 'Fantastic end-to-end Central',
  defaultPort: 'pane',
  redisKeyPrefix: 'exper',
  status: 1,
  loggingMode: 'partnerships',
};

export const sampleWithPartialData: IModule = {
  id: 31602,
  moduleName: 'black Markets',
  defaultPort: 'comp',
  redisKeyPrefix: 'magen',
  status: 1,
  loggingMode: 'generating Pants overriding',
};

export const sampleWithFullData: IModule = {
  id: 89907,
  moduleName: 'yellow View',
  defaultPort: 'Cros',
  redisKeyPrefix: 'Fresh',
  status: 1,
  loggingMode: 'XML',
  loggingFilter: 'Rubber',
};

export const sampleWithNewData: NewModule = {
  moduleName: 'Music',
  defaultPort: 'Smal',
  redisKeyPrefix: 'Ohio ',
  status: 0,
  loggingMode: 'withdrawal Concrete',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
