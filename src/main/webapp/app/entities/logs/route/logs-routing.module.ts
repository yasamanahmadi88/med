import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { LogsComponent } from '../list/logs.component';
import { ASC } from 'app/config/navigation.constants';
import {LogsDetailComponent} from "../detail/logs-detail.component";
import {LogsRoutingResolveService} from "./logs-routing-resolve.service";

const flowRoute: Routes = [
  {
    path: '',
    component: LogsComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: LogsDetailComponent,
    resolve: {
      log: LogsRoutingResolveService,
    },
    // canActivate: [UserRouteAccessService],
  },

  /* {
    path: 'new',
    component: LogsNewComponent,
    resolve: {
      flow: LogsRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: LogsUpdateComponent,
    resolve: {
      flow: LogsRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },*/
];

@NgModule({
  imports: [RouterModule.forChild(flowRoute)],
  exports: [RouterModule],
})
export class LogsRoutingModule {}
