import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IVersion } from '../version.model';
import { VersionService } from '../service/version.service';

@Injectable({ providedIn: 'root' })
export class VersionRoutingResolveService implements Resolve<IVersion | null> {
  constructor(
    protected service: VersionService,
    protected router: Router,
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IVersion | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((version: HttpResponse<IVersion>) => {
          if (version.body) {
            return of(version.body);
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
