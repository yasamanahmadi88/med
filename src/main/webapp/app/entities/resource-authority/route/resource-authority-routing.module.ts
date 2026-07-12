import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ResourceAuthorityComponent } from '../list/resource-authority.component';
import { ResourceAuthorityDetailComponent } from '../detail/resource-authority-detail.component';
import { ResourceAuthorityUpdateComponent } from '../update/resource-authority-update.component';
import { ResourceAuthorityRoutingResolveService } from './resource-authority-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const resourceAuthorityRoute: Routes = [
  {
    path: '',
    component: ResourceAuthorityComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ResourceAuthorityDetailComponent,
    resolve: {
      resourceAuthority: ResourceAuthorityRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ResourceAuthorityUpdateComponent,
    resolve: {
      resourceAuthority: ResourceAuthorityRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ResourceAuthorityUpdateComponent,
    resolve: {
      resourceAuthority: ResourceAuthorityRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(resourceAuthorityRoute)],
  exports: [RouterModule],
})
export class ResourceAuthorityRoutingModule {}
