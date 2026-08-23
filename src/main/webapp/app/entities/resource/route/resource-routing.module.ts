import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ResourceComponent } from '../list/resource.component';
import { ResourceDetailComponent } from '../detail/resource-detail.component';
import { ResourceUpdateComponent } from '../update/resource-update.component';
import { ResourceRoutingResolveService } from './resource-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const resourceRoute: Routes = [
  {
    path: '',
    component: ResourceComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ResourceDetailComponent,
    resolve: {
      resource: ResourceRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ResourceUpdateComponent,
    resolve: {
      resource: ResourceRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ResourceUpdateComponent,
    resolve: {
      resource: ResourceRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(resourceRoute)],
  exports: [RouterModule],
})
export class ResourceRoutingModule {}
