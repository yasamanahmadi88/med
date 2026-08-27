import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IMedAuthority } from '../med-authority.model';
import { MedAuthorityService } from '../service/med-authority.service';

@Injectable({ providedIn: 'root' })
export class MedAuthorityRoutingResolveService implements Resolve<IMedAuthority | null> {
  constructor(
    protected service: MedAuthorityService,
    protected router: Router,
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IMedAuthority | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((medAuthority: HttpResponse<IMedAuthority>) => {
          if (medAuthority.body) {
            return of(medAuthority.body);
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
