export interface IModule {
  id: number;
  moduleName?: string | null;
  defaultPort?: string | null;
  redisKeyPrefix?: string | null;
  status?: boolean| number | null;
  loggingMode?: string | null;
  loggingFilter?: string | null;
}

export type NewModule = Omit<IModule, 'id'> & { id: null };
