export interface IVersion {
  id: number;
  tableName?: string | null;
  moduleName?: string | null;
  tableVersion?: number | null;
}

export type NewVersion = Omit<IVersion, 'id'> & { id: null };
