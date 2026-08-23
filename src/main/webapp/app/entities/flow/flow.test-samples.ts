import { IFlow, NewFlow } from './flow.model';

export const sampleWithRequiredData: IFlow = {
  id: 4923,
  flowName: 'hackin',
  flowDesc: 'Cambridgeshire next',
  flow: 'Fresh',
};

export const sampleWithPartialData: IFlow = {
  id: 26569,
  flowName: 'functi',
  flowDesc: 'Bike Dirham',
  flow: 'Representative',
};

export const sampleWithFullData: IFlow = {
  id: 61495,
  flowName: 'Generi',
  flowDesc: 'Analyst',
  flow: 'Bedfordshire',
};

export const sampleWithNewData: NewFlow = {
  flowName: 'front-',
  flowDesc: 'Grocery',
  flow: 'integrated',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
