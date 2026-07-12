import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { CustomAuditEventComponent } from '../list/custom-audit-event.component';
import { CustomAuditEventDetailComponent } from '../detail/custom-audit-event-detail.component';
import { CustomAuditEventUpdateComponent } from '../update/custom-audit-event-update.component';
import { CustomAuditEventRoutingResolveService } from './custom-audit-event-routing-resolve.service';
import { DESC } from 'app/config/navigation.constants';

const customAuditEventRoute: Routes = [
  {
    path: '',
    component: CustomAuditEventComponent,
    data: {
      defaultSort: 'eventDate,' + DESC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: CustomAuditEventDetailComponent,
    resolve: {
      customAuditEvent: CustomAuditEventRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: CustomAuditEventUpdateComponent,
    resolve: {
      customAuditEvent: CustomAuditEventRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: CustomAuditEventUpdateComponent,
    resolve: {
      customAuditEvent: CustomAuditEventRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(customAuditEventRoute)],
  exports: [RouterModule],
})
export class CustomAuditEventRoutingModule {}
