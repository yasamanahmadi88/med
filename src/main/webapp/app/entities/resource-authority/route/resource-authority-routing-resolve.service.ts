import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IResourceAuthority } from '../resource-authority.model';
import { ResourceAuthorityService } from '../service/resource-authority.service';

@Injectable({ providedIn: 'root' })
export class ResourceAuthorityRoutingResolveService implements Resolve<IResourceAuthority | null> {
  constructor(
    protected service: ResourceAuthorityService,
    protected router: Router,
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IResourceAuthority | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((resourceAuthority: HttpResponse<IResourceAuthority>) => {
          if (resourceAuthority.body) {
            return of(resourceAuthority.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        }),
      );
    }
    return of(null);
  }
}
