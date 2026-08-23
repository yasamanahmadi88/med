import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { InstanceComponent } from '../list/instance.component';
import { InstanceDetailComponent } from '../detail/instance-detail.component';
import { InstanceUpdateComponent } from '../update/instance-update.component';
import { InstanceRoutingResolveService } from './instance-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const instanceRoute: Routes = [
  {
    path: '',
    component: InstanceComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: InstanceDetailComponent,
    resolve: {
      instance: InstanceRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: InstanceUpdateComponent,
    resolve: {
      instance: InstanceRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: InstanceUpdateComponent,
    resolve: {
      instance: InstanceRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(instanceRoute)],
  exports: [RouterModule],
})
export class InstanceRoutingModule {}
