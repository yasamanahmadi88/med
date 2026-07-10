import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IInstance, NewInstance } from '../instance.model';

export type PartialUpdateInstance = Partial<IInstance> & Pick<IInstance, 'id'>;

type RestOf<T extends IInstance | NewInstance> = Omit<T, 'lastUpdateDate'> & {
  lastUpdateDate?: string | null;
};

export type RestInstance = RestOf<IInstance>;

export type NewRestInstance = RestOf<NewInstance>;

export type PartialUpdateRestInstance = RestOf<PartialUpdateInstance>;

export type EntityResponseType = HttpResponse<IInstance>;
export type EntityArrayResponseType = HttpResponse<IInstance[]>;

@Injectable({ providedIn: 'root' })
export class InstanceService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/instances');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(instance: NewInstance): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(instance);
    return this.http
      .post<RestInstance>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(instance: IInstance): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(instance);
    return this.http
      .put<RestInstance>(`${this.resourceUrl}/${this.getInstanceIdentifier(instance)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(instance: PartialUpdateInstance): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(instance);
    return this.http
      .patch<RestInstance>(`${this.resourceUrl}/${this.getInstanceIdentifier(instance)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestInstance>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestInstance[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getInstanceIdentifier(instance: Pick<IInstance, 'id'>): number {
    return instance.id;
  }

  compareInstance(o1: Pick<IInstance, 'id'> | null, o2: Pick<IInstance, 'id'> | null): boolean {
    return o1 && o2 ? this.getInstanceIdentifier(o1) === this.getInstanceIdentifier(o2) : o1 === o2;
  }

  addInstanceToCollectionIfMissing<Type extends Pick<IInstance, 'id'>>(
    instanceCollection: Type[],
    ...instancesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const instances: Type[] = instancesToCheck.filter(isPresent);
    if (instances.length > 0) {
      const instanceCollectionIdentifiers = instanceCollection.map(instanceItem => this.getInstanceIdentifier(instanceItem)!);
      const instancesToAdd = instances.filter(instanceItem => {
        const instanceIdentifier = this.getInstanceIdentifier(instanceItem);
        if (instanceCollectionIdentifiers.includes(instanceIdentifier)) {
          return false;
        }
        instanceCollectionIdentifiers.push(instanceIdentifier);
        return true;
      });
      return [...instancesToAdd, ...instanceCollection];
    }
    return instanceCollection;
  }

  protected convertDateFromClient<T extends IInstance | NewInstance | PartialUpdateInstance>(instance: T): RestOf<T> {
    return {
      ...instance,
      lastUpdateDate: instance.lastUpdateDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restInstance: RestInstance): IInstance {
    return {
      ...restInstance,
      lastUpdateDate: restInstance.lastUpdateDate ? dayjs(restInstance.lastUpdateDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestInstance>): HttpResponse<IInstance> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestInstance[]>): HttpResponse<IInstance[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
