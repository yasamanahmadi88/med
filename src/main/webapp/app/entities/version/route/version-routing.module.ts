import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { VersionComponent } from '../list/version.component';
import { VersionDetailComponent } from '../detail/version-detail.component';
import { VersionUpdateComponent } from '../update/version-update.component';
import { VersionRoutingResolveService } from './version-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const versionRoute: Routes = [
  {
    path: '',
    component: VersionComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: VersionDetailComponent,
    resolve: {
      version: VersionRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: VersionUpdateComponent,
    resolve: {
      version: VersionRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: VersionUpdateComponent,
    resolve: {
      version: VersionRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(versionRoute)],
  exports: [RouterModule],
})
export class VersionRoutingModule {}
