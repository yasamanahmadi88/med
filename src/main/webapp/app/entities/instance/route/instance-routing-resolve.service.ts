import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IInstance } from '../instance.model';
import { InstanceService } from '../service/instance.service';

@Injectable({ providedIn: 'root' })
export class InstanceRoutingResolveService implements Resolve<IInstance | null> {
  constructor(protected service: InstanceService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IInstance | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((instance: HttpResponse<IInstance>) => {
          if (instance.body) {
            return of(instance.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(null);
  }
}
