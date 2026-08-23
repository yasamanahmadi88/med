import { IVersion, NewVersion } from './version.model';

export const sampleWithRequiredData: IVersion = {
  id: 91276,
  tableName: 'Frozen Creek Tobago',
  moduleName: 'blue Principal cohesive',
  tableVersion: 31345,
};

export const sampleWithPartialData: IVersion = {
  id: 81903,
  tableName: 'Maryland Overpass Borders',
  moduleName: 'Communications deliverables',
  tableVersion: 78349,
};

export const sampleWithFullData: IVersion = {
  id: 61511,
  tableName: 'Home Wooden Uzbekistan',
  moduleName: 'Fresh',
  tableVersion: 1081,
};

export const sampleWithNewData: NewVersion = {
  tableName: 'Sheqel Landing',
  moduleName: 'withdrawal European auxiliary',
  tableVersion: 65086,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
