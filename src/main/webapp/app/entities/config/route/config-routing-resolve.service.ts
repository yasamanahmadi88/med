import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IConfig } from '../config.model';
import { ConfigService } from '../service/config.service';

@Injectable({ providedIn: 'root' })
export class ConfigRoutingResolveService implements Resolve<IConfig | null> {
  constructor(protected service: ConfigService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IConfig | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((config: HttpResponse<IConfig>) => {
          if (config.body) {
            return of(config.body);
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
