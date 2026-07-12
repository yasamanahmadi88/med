import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { IReportLogs } from '../logs.model';
import { LogReportService } from '../service/logs.service';

@Injectable({ providedIn: 'root' })
export class LogsRoutingResolveService implements Resolve<IReportLogs | null> {
  constructor(
    protected service: LogReportService,
    protected router: Router,
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IReportLogs | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((log: HttpResponse<IReportLogs>) => {
          if (log.body) {
            return of(log.body);
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
