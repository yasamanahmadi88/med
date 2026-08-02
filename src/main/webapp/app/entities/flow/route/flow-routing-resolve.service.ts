import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IFlow } from '../flow.model';
import { FlowService } from '../service/flow.service';

@Injectable({ providedIn: 'root' })
export class FlowRoutingResolveService  {
  constructor(protected service: FlowService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IFlow | null> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((flow: HttpResponse<IFlow>) => {
          if (flow.body) {
            return of(flow.body);
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
