import { IProduct, NewProduct } from './product.model';

export const sampleWithRequiredData: IProduct = {
  id: 77672,
  productName: 'Cus',
  productDesc: 'cross-platform Unbranded deposit',
};

export const sampleWithPartialData: IProduct = {
  id: 49054,
  productName: 'del',
  productDesc: 'Administrator Health',
};

export const sampleWithFullData: IProduct = {
  id: 76905,
  productName: 'SAS',
  productDesc: 'Assurance hub',
};

export const sampleWithNewData: NewProduct = {
  productName: 'Cus',
  productDesc: 'Plastic Incredible Agent',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
