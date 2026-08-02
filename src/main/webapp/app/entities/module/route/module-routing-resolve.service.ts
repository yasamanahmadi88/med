import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IModule } from '../module.model';
import { ModuleService } from '../service/module.service';

@Injectable({ providedIn: 'root' })
export class ModuleRoutingResolveService  {
  constructor(protected service: ModuleService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IModule | null> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((module: HttpResponse<IModule>) => {
          if (module.body) {
            return of(module.body);
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
