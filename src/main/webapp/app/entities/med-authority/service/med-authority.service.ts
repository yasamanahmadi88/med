import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IMedAuthority, NewMedAuthority } from '../med-authority.model';

export type PartialUpdateMedAuthority = Partial<IMedAuthority> & Pick<IMedAuthority, 'id'>;

export type EntityResponseType = HttpResponse<IMedAuthority>;
export type EntityArrayResponseType = HttpResponse<IMedAuthority[]>;

@Injectable({ providedIn: 'root' })
export class MedAuthorityService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/med-authorities');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(medAuthority: NewMedAuthority): Observable<EntityResponseType> {
    return this.http.post<IMedAuthority>(this.resourceUrl, medAuthority, { observe: 'response' });
  }

  update(medAuthority: IMedAuthority): Observable<EntityResponseType> {
    return this.http.put<IMedAuthority>(`${this.resourceUrl}/${this.getMedAuthorityIdentifier(medAuthority)}`, medAuthority, {
      observe: 'response',
    });
  }

  partialUpdate(medAuthority: PartialUpdateMedAuthority): Observable<EntityResponseType> {
    return this.http.patch<IMedAuthority>(`${this.resourceUrl}/${this.getMedAuthorityIdentifier(medAuthority)}`, medAuthority, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IMedAuthority>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IMedAuthority[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getMedAuthorityIdentifier(medAuthority: Pick<IMedAuthority, 'id'>): number {
    return medAuthority.id;
  }

  compareMedAuthority(o1: Pick<IMedAuthority, 'id'> | null, o2: Pick<IMedAuthority, 'id'> | null): boolean {
    return o1 && o2 ? this.getMedAuthorityIdentifier(o1) === this.getMedAuthorityIdentifier(o2) : o1 === o2;
  }

  addMedAuthorityToCollectionIfMissing<Type extends Pick<IMedAuthority, 'id'>>(
    medAuthorityCollection: Type[],
    ...medAuthoritiesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const medAuthorities: Type[] = medAuthoritiesToCheck.filter(isPresent);
    if (medAuthorities.length > 0) {
      const medAuthorityCollectionIdentifiers = medAuthorityCollection.map(
        medAuthorityItem => this.getMedAuthorityIdentifier(medAuthorityItem)!
      );
      const medAuthoritiesToAdd = medAuthorities.filter(medAuthorityItem => {
        const medAuthorityIdentifier = this.getMedAuthorityIdentifier(medAuthorityItem);
        if (medAuthorityCollectionIdentifiers.includes(medAuthorityIdentifier)) {
          return false;
        }
        medAuthorityCollectionIdentifiers.push(medAuthorityIdentifier);
        return true;
      });
      return [...medAuthoritiesToAdd, ...medAuthorityCollection];
    }
    return medAuthorityCollection;
  }
}
