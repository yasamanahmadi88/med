import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ConfigComponent } from '../list/config.component';
import { ConfigDetailComponent } from '../detail/config-detail.component';
import { ConfigUpdateComponent } from '../update/config-update.component';
import { ConfigRoutingResolveService } from './config-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const configRoute: Routes = [
  {
    path: '',
    component: ConfigComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: ConfigDetailComponent,
    resolve: {
      config: ConfigRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: ConfigUpdateComponent,
    resolve: {
      config: ConfigRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: ConfigUpdateComponent,
    resolve: {
      config: ConfigRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(configRoute)],
  exports: [RouterModule],
})
export class ConfigRoutingModule {}
