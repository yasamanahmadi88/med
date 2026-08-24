export interface IReportLogs {
  id?: number;
  msgType: string;
  correlationId: string;
  referenceType: string;
  reference: string;
  moduleSource: string;
  moduleDestination: string;
  properties: string;
  reqMessage: string;
  resMessage: string;
  error: string;
  errorDetails: string;
  initialDate: string;
  createDate: string;
  logType: string;
}

export type NewFlow = Omit<IReportLogs, 'id'> & { id: null };
