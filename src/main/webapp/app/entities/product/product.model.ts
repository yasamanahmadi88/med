export interface IProduct {
  id: number;
  productName?: string | null;
  productDesc?: string | null;
}

export type NewProduct = Omit<IProduct, 'id'> & { id: null };
