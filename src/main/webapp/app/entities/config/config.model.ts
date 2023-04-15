import { IModule } from 'app/entities/module/module.model';

export interface IConfig {
  id: number;
  property?: string | null;
  pValue?: string | null;
  commentDesc?: string | null;
  module?: Pick<IModule, 'id'> | null;
}

export type NewConfig = Omit<IConfig, 'id'> & { id: null };
