import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IResourceAuthority, NewResourceAuthority } from '../resource-authority.model';

export type PartialUpdateResourceAuthority = Partial<IResourceAuthority> & Pick<IResourceAuthority, 'id'>;

export type EntityResponseType = HttpResponse<IResourceAuthority>;
export type EntityArrayResponseType = HttpResponse<IResourceAuthority[]>;

@Injectable({ providedIn: 'root' })
export class ResourceAuthorityService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/resource-authorities');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(resourceAuthority: NewResourceAuthority): Observable<EntityResponseType> {
    return this.http.post<IResourceAuthority>(this.resourceUrl, resourceAuthority, { observe: 'response' });
  }

  update(resourceAuthority: IResourceAuthority): Observable<EntityResponseType> {
    return this.http.put<IResourceAuthority>(
      `${this.resourceUrl}/${this.getResourceAuthorityIdentifier(resourceAuthority)}`,
      resourceAuthority,
      { observe: 'response' },
    );
  }

  partialUpdate(resourceAuthority: PartialUpdateResourceAuthority): Observable<EntityResponseType> {
    return this.http.patch<IResourceAuthority>(
      `${this.resourceUrl}/${this.getResourceAuthorityIdentifier(resourceAuthority)}`,
      resourceAuthority,
      { observe: 'response' },
    );
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IResourceAuthority>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IResourceAuthority[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getResourceAuthorityIdentifier(resourceAuthority: Pick<IResourceAuthority, 'id'>): number {
    return resourceAuthority.id;
  }

  compareResourceAuthority(o1: Pick<IResourceAuthority, 'id'> | null, o2: Pick<IResourceAuthority, 'id'> | null): boolean {
    return o1 && o2 ? this.getResourceAuthorityIdentifier(o1) === this.getResourceAuthorityIdentifier(o2) : o1 === o2;
  }

  addResourceAuthorityToCollectionIfMissing<Type extends Pick<IResourceAuthority, 'id'>>(
    resourceAuthorityCollection: Type[],
    ...resourceAuthoritiesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const resourceAuthorities: Type[] = resourceAuthoritiesToCheck.filter(isPresent);
    if (resourceAuthorities.length > 0) {
      const resourceAuthorityCollectionIdentifiers = resourceAuthorityCollection.map(resourceAuthorityItem =>
        this.getResourceAuthorityIdentifier(resourceAuthorityItem),
      );
      const resourceAuthoritiesToAdd = resourceAuthorities.filter(resourceAuthorityItem => {
        const resourceAuthorityIdentifier = this.getResourceAuthorityIdentifier(resourceAuthorityItem);
        if (resourceAuthorityCollectionIdentifiers.includes(resourceAuthorityIdentifier)) {
          return false;
        }
        resourceAuthorityCollectionIdentifiers.push(resourceAuthorityIdentifier);
        return true;
      });
      return [...resourceAuthoritiesToAdd, ...resourceAuthorityCollection];
    }
    return resourceAuthorityCollection;
  }
}
