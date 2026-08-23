import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ICustomAuditEvent } from '../custom-audit-event.model';
import { CustomAuditEventService } from '../service/custom-audit-event.service';

@Injectable({ providedIn: 'root' })
export class CustomAuditEventRoutingResolveService implements Resolve<ICustomAuditEvent | null> {
  constructor(protected service: CustomAuditEventService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<ICustomAuditEvent | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((customAuditEvent: HttpResponse<ICustomAuditEvent>) => {
          if (customAuditEvent.body) {
            return of(customAuditEvent.body);
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
