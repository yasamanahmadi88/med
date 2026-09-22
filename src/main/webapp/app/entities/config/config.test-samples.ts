import { IConfig, NewConfig } from './config.model';

export const sampleWithRequiredData: IConfig = {
  id: 13409,
  property: 'Refined Berkshire',
  pValue: 'programming Beauty Quality',
};

export const sampleWithPartialData: IConfig = {
  id: 23192,
  property: 'models',
  pValue: 'Bedfordshire',
};

export const sampleWithFullData: IConfig = {
  id: 66724,
  property: 'RAM',
  pValue: 'concept Berkshire Outdoors',
  commentDesc: 'Outdoors',
};

export const sampleWithNewData: NewConfig = {
  property: 'navigate JSON',
  pValue: 'Fresh Expressway',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
