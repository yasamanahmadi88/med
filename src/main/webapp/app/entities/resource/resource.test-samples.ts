import { IResource, NewResource } from './resource.model';

export const sampleWithRequiredData: IResource = {
  id: 29528,
  name: 'Maine enable',
  displayName: 'Account embrace Computers',
  apiUri: 'HTTP California circuit',
  resourceType: 'Hat Loan Auto',
};

export const sampleWithPartialData: IResource = {
  id: 96828,
  name: 'Central model',
  displayName: 'Kwacha Global Radial',
  apiUri: 'Brook 1080p',
  resourceType: 'wireless',
};

export const sampleWithFullData: IResource = {
  id: 87733,
  name: 'Metal',
  displayName: 'turquoise Wisconsin Tasty',
  apiUri: 'multi-byte Designer Via',
  resourceType: 'Engineer',
};

export const sampleWithNewData: NewResource = {
  name: 'Pants',
  displayName: 'virtual',
  apiUri: 'SMTP Island withdrawal',
  resourceType: 'Operations back-end Technician',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
