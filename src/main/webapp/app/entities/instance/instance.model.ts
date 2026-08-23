import dayjs from 'dayjs/esm';

export interface IInstance {
  id: number;
  moduleName?: string | null;
  port?: string | null;
  moduleStatus?: string | null;
  lastUpdateDate?: dayjs.Dayjs | null;
  threadPoolQueueSize?: number | null;
  moduleStartTime?: number | null;
  totalProcessedWork?: number | null;
  totalProcessedTask?: number | null;
  processedStatistics?: string | null;
  hostName?: string | null;
}

export type NewInstance = Omit<IInstance, 'id'> & { id: null };
