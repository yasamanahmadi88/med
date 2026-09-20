import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { MedAuthorityComponent } from '../list/med-authority.component';
import { MedAuthorityDetailComponent } from '../detail/med-authority-detail.component';
import { MedAuthorityUpdateComponent } from '../update/med-authority-update.component';
import { MedAuthorityRoutingResolveService } from './med-authority-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const medAuthorityRoute: Routes = [
  {
    path: '',
    component: MedAuthorityComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: MedAuthorityDetailComponent,
    resolve: {
      medAuthority: MedAuthorityRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: MedAuthorityUpdateComponent,
    resolve: {
      medAuthority: MedAuthorityRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: MedAuthorityUpdateComponent,
    resolve: {
      medAuthority: MedAuthorityRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(medAuthorityRoute)],
  exports: [RouterModule],
})
export class MedAuthorityRoutingModule {}
