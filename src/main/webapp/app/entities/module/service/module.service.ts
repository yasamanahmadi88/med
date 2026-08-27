import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IModule, NewModule } from '../module.model';

export type PartialUpdateModule = Partial<IModule> & Pick<IModule, 'id'>;

export type EntityResponseType = HttpResponse<IModule>;
export type EntityArrayResponseType = HttpResponse<IModule[]>;

@Injectable({ providedIn: 'root' })
export class ModuleService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/modules');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(module: NewModule): Observable<EntityResponseType> {
    return this.http.post<IModule>(this.resourceUrl, module, { observe: 'response' });
  }

  update(module: IModule): Observable<EntityResponseType> {
    return this.http.put<IModule>(`${this.resourceUrl}/${this.getModuleIdentifier(module)}`, module, { observe: 'response' });
  }

  partialUpdate(module: PartialUpdateModule): Observable<EntityResponseType> {
    return this.http.patch<IModule>(`${this.resourceUrl}/${this.getModuleIdentifier(module)}`, module, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IModule>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IModule[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getModuleIdentifier(module: Pick<IModule, 'id'>): number {
    return module.id;
  }

  compareModule(o1: Pick<IModule, 'id'> | null, o2: Pick<IModule, 'id'> | null): boolean {
    return o1 && o2 ? this.getModuleIdentifier(o1) === this.getModuleIdentifier(o2) : o1 === o2;
  }

  addModuleToCollectionIfMissing<Type extends Pick<IModule, 'id'>>(
    moduleCollection: Type[],
    ...modulesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const modules: Type[] = modulesToCheck.filter(isPresent);
    if (modules.length > 0) {
      const moduleCollectionIdentifiers = moduleCollection.map(moduleItem => this.getModuleIdentifier(moduleItem)!);
      const modulesToAdd = modules.filter(moduleItem => {
        const moduleIdentifier = this.getModuleIdentifier(moduleItem);
        if (moduleCollectionIdentifiers.includes(moduleIdentifier)) {
          return false;
        }
        moduleCollectionIdentifiers.push(moduleIdentifier);
        return true;
      });
      return [...modulesToAdd, ...moduleCollection];
    }
    return moduleCollection;
  }
}
