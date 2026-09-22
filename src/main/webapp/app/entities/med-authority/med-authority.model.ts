export interface IMedAuthority {
  id: number;
  name?: string | null;
  displayName?: string | null;
  parentDisplayName?: string;
  parentId?: number | null;
}

export type NewMedAuthority = Omit<IMedAuthority, 'id'> & { id: null };
