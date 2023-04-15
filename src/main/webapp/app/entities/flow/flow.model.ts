import { IProduct } from 'app/entities/product/product.model';

export interface IFlow {
  id: number;
  flowName?: string | null;
  flowDesc?: string | null;
  flow?: string | null;
  product?: Pick<IProduct, 'id'> | null;
}

export type NewFlow = Omit<IFlow, 'id'> & { id: null };
