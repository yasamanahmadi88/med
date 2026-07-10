import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IFlow, NewFlow } from '../flow.model';

export type PartialUpdateFlow = Partial<IFlow> & Pick<IFlow, 'id'>;

export type EntityResponseType = HttpResponse<IFlow>;
export type EntityArrayResponseType = HttpResponse<IFlow[]>;

@Injectable({ providedIn: 'root' })
export class FlowService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/flows');

  public xmlTemp = '';

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(flow: NewFlow): Observable<EntityResponseType> {
    return this.http.post<IFlow>(this.resourceUrl, flow, { observe: 'response' });
  }

  update(flow: IFlow): Observable<EntityResponseType> {
    return this.http.put<IFlow>(`${this.resourceUrl}/${this.getFlowIdentifier(flow)}`, flow, { observe: 'response' });
  }

  partialUpdate(flow: PartialUpdateFlow): Observable<EntityResponseType> {
    return this.http.patch<IFlow>(`${this.resourceUrl}/${this.getFlowIdentifier(flow)}`, flow, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IFlow>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IFlow[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getFlowIdentifier(flow: Pick<IFlow, 'id'>): number {
    return flow.id;
  }

  compareFlow(o1: Pick<IFlow, 'id'> | null, o2: Pick<IFlow, 'id'> | null): boolean {
    return o1 && o2 ? this.getFlowIdentifier(o1) === this.getFlowIdentifier(o2) : o1 === o2;
  }

  saveFlow(xml: string): Observable<HttpResponse<any>> {
    return this.http.post(`${this.resourceUrl}/save`, xml, { observe: 'response' });
  }

  isFlowNameValid(flowName: any): Observable<HttpResponse<any>> {
    return this.http.post(`${this.resourceUrl}/isFlowNameValid`, flowName, { observe: 'response' });
  }

  addFlowToCollectionIfMissing<Type extends Pick<IFlow, 'id'>>(
    flowCollection: Type[],
    ...flowsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const flows: Type[] = flowsToCheck.filter(isPresent);
    if (flows.length > 0) {
      const flowCollectionIdentifiers = flowCollection.map(flowItem => this.getFlowIdentifier(flowItem));
      const flowsToAdd = flows.filter(flowItem => {
        const flowIdentifier = this.getFlowIdentifier(flowItem);
        if (flowCollectionIdentifiers.includes(flowIdentifier)) {
          return false;
        }
        flowCollectionIdentifiers.push(flowIdentifier);
        return true;
      });
      return [...flowsToAdd, ...flowCollection];
    }
    return flowCollection;
  }
}
