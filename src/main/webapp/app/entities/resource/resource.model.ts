export interface IResource {
  id: number;
  name?: string | null;
  displayName?: string | null;
  apiUri?: string | null;
  resourceType?: string | null;
}

export type NewResource = Omit<IResource, 'id'> & { id: null };
