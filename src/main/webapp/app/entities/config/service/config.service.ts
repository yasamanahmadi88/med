import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IConfig, NewConfig } from '../config.model';

export type PartialUpdateConfig = Partial<IConfig> & Pick<IConfig, 'id'>;

export type EntityResponseType = HttpResponse<IConfig>;
export type EntityArrayResponseType = HttpResponse<IConfig[]>;

@Injectable({ providedIn: 'root' })
export class ConfigService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/configs');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(config: NewConfig): Observable<EntityResponseType> {
    return this.http.post<IConfig>(this.resourceUrl, config, { observe: 'response' });
  }

  update(config: IConfig): Observable<EntityResponseType> {
    return this.http.put<IConfig>(`${this.resourceUrl}/${this.getConfigIdentifier(config)}`, config, { observe: 'response' });
  }

  partialUpdate(config: PartialUpdateConfig): Observable<EntityResponseType> {
    return this.http.patch<IConfig>(`${this.resourceUrl}/${this.getConfigIdentifier(config)}`, config, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IConfig>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IConfig[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getConfigIdentifier(config: Pick<IConfig, 'id'>): number {
    return config.id;
  }

  compareConfig(o1: Pick<IConfig, 'id'> | null, o2: Pick<IConfig, 'id'> | null): boolean {
    return o1 && o2 ? this.getConfigIdentifier(o1) === this.getConfigIdentifier(o2) : o1 === o2;
  }

  addConfigToCollectionIfMissing<Type extends Pick<IConfig, 'id'>>(
    configCollection: Type[],
    ...configsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const configs: Type[] = configsToCheck.filter(isPresent);
    if (configs.length > 0) {
      const configCollectionIdentifiers = configCollection.map(configItem => this.getConfigIdentifier(configItem)!);
      const configsToAdd = configs.filter(configItem => {
        const configIdentifier = this.getConfigIdentifier(configItem);
        if (configCollectionIdentifiers.includes(configIdentifier)) {
          return false;
        }
        configCollectionIdentifiers.push(configIdentifier);
        return true;
      });
      return [...configsToAdd, ...configCollection];
    }
    return configCollection;
  }
}
