import { IMedAuthority } from 'app/entities/med-authority/med-authority.model';
import { IResource } from 'app/entities/resource/resource.model';

export interface IResourceAuthority {
  id: number;
  verb?: string | null;
  medAuthority?: Pick<IMedAuthority, 'id'> | null;
  resource?: Pick<IResource, 'id'> | null;
}

export type NewResourceAuthority = Omit<IResourceAuthority, 'id'> & { id: null };
