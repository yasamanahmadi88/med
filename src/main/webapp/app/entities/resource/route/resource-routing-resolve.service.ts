import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IResource } from '../resource.model';
import { ResourceService } from '../service/resource.service';

@Injectable({ providedIn: 'root' })
export class ResourceRoutingResolveService  {
  constructor(protected service: ResourceService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IResource | null> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((resource: HttpResponse<IResource>) => {
          if (resource.body) {
            return of(resource.body);
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
