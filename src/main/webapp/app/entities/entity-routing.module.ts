import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'med-authority',
        data: { pageTitle: 'medPortalApp.medAuthority.home.title' },
        loadChildren: () => import('./med-authority/med-authority.module').then(m => m.MedAuthorityModule),
      },
      {
        path: 'resource',
        data: { pageTitle: 'medPortalApp.resource.home.title' },
        loadChildren: () => import('./resource/resource.module').then(m => m.ResourceModule),
      },
      {
        path: 'resource-authority',
        data: { pageTitle: 'medPortalApp.resourceAuthority.home.title' },
        loadChildren: () => import('./resource-authority/resource-authority.module').then(m => m.ResourceAuthorityModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
