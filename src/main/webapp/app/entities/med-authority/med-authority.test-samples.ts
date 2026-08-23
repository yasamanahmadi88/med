import { IMedAuthority, NewMedAuthority } from './med-authority.model';

export const sampleWithRequiredData: IMedAuthority = {
  id: 14868,
  name: 'Jewelery Georgia',
};

export const sampleWithPartialData: IMedAuthority = {
  id: 61860,
  name: 'out-of-the-box',
  displayName: 'purple Customer',
};

export const sampleWithFullData: IMedAuthority = {
  id: 11162,
  name: 'Open-source Metrics Cotton',
  displayName: 'Metal',
  parentId: 71414,
};

export const sampleWithNewData: NewMedAuthority = {
  name: 'Associate Identity Vision-oriented',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
