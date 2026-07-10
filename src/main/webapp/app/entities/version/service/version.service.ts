import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IVersion, NewVersion } from '../version.model';

export type PartialUpdateVersion = Partial<IVersion> & Pick<IVersion, 'id'>;

export type EntityResponseType = HttpResponse<IVersion>;
export type EntityArrayResponseType = HttpResponse<IVersion[]>;

@Injectable({ providedIn: 'root' })
export class VersionService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/versions');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(version: NewVersion): Observable<EntityResponseType> {
    return this.http.post<IVersion>(this.resourceUrl, version, { observe: 'response' });
  }

  update(version: IVersion): Observable<EntityResponseType> {
    return this.http.put<IVersion>(`${this.resourceUrl}/${this.getVersionIdentifier(version)}`, version, { observe: 'response' });
  }

  partialUpdate(version: PartialUpdateVersion): Observable<EntityResponseType> {
    return this.http.patch<IVersion>(`${this.resourceUrl}/${this.getVersionIdentifier(version)}`, version, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IVersion>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IVersion[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getVersionIdentifier(version: Pick<IVersion, 'id'>): number {
    return version.id;
  }

  compareVersion(o1: Pick<IVersion, 'id'> | null, o2: Pick<IVersion, 'id'> | null): boolean {
    return o1 && o2 ? this.getVersionIdentifier(o1) === this.getVersionIdentifier(o2) : o1 === o2;
  }

  addVersionToCollectionIfMissing<Type extends Pick<IVersion, 'id'>>(
    versionCollection: Type[],
    ...versionsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const versions: Type[] = versionsToCheck.filter(isPresent);
    if (versions.length > 0) {
      const versionCollectionIdentifiers = versionCollection.map(versionItem => this.getVersionIdentifier(versionItem));
      const versionsToAdd = versions.filter(versionItem => {
        const versionIdentifier = this.getVersionIdentifier(versionItem);
        if (versionCollectionIdentifiers.includes(versionIdentifier)) {
          return false;
        }
        versionCollectionIdentifiers.push(versionIdentifier);
        return true;
      });
      return [...versionsToAdd, ...versionCollection];
    }
    return versionCollection;
  }
}
