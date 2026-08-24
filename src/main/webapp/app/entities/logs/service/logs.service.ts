import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IReportLogs } from '../logs.model';

export type EntityResponseType = HttpResponse<IReportLogs>;
export type EntityArrayResponseType = HttpResponse<IReportLogs[]>;

@Injectable({ providedIn: 'root' })
export class LogReportService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/logs');
  protected resourceReportUrl = this.applicationConfigService.getEndpointFor('api/logs/summary');
  protected resourceReportSearchUrl = this.applicationConfigService.getEndpointFor('api/logs/search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  /* create(flow: NewFlow): Observable<EntityResponseType> {
    return this.http.post<IReportLogs>(this.resourceUrl, flow, { observe: 'response' });
  }

  */
  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IReportLogs>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IReportLogs[]>(this.resourceReportUrl, { params: options, observe: 'response' });
  }
  search(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IReportLogs[]>(this.resourceReportSearchUrl, { params: options, observe: 'response' });
  }
  getLogIdentifier(log: Pick<IReportLogs, 'id'>): number {
    return log.id as number;
  }
}
