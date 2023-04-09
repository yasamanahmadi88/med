import { IResourceAuthority, NewResourceAuthority } from './resource-authority.model';

export const sampleWithRequiredData: IResourceAuthority = {
  id: 6323,
  verb: 'scale',
};

export const sampleWithPartialData: IResourceAuthority = {
  id: 65161,
  verb: 'Platinum Investor Agent',
};

export const sampleWithFullData: IResourceAuthority = {
  id: 43748,
  verb: 'hacking withdrawal',
};

export const sampleWithNewData: NewResourceAuthority = {
  verb: 'transmitting quantifying',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
