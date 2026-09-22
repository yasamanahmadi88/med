import { IMedAuthority } from 'app/entities/med-authority/med-authority.model';
import { IResource } from 'app/entities/resource/resource.model';

export interface IResourceAuthority {
  id: number;
  verb?: string | null;
  medAuthority?: IMedAuthority | null;
  resource?: IResource | null;
}

export type NewResourceAuthority = Omit<IResourceAuthority, 'id'> & { id: null };
