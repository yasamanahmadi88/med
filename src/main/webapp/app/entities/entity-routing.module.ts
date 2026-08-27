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
      {
        path: 'module',
        data: { pageTitle: 'medPortalApp.module.home.title' },
        loadChildren: () => import('./module/module.module').then(m => m.ModuleModule),
      },
      {
        path: 'config',
        data: { pageTitle: 'medPortalApp.config.home.title' },
        loadChildren: () => import('./config/config.module').then(m => m.ConfigModule),
      },
      {
        path: 'instance',
        data: { pageTitle: 'medPortalApp.instance.home.title' },
        loadChildren: () => import('./instance/instance.module').then(m => m.InstanceModule),
      },
      {
        path: 'version',
        data: { pageTitle: 'medPortalApp.version.home.title' },
        loadChildren: () => import('./version/version.module').then(m => m.VersionModule),
      },
      {
        path: 'product',
        data: { pageTitle: 'medPortalApp.product.home.title' },
        loadChildren: () => import('./product/product.module').then(m => m.ProductModule),
      },
      {
        path: 'flow',
        data: { pageTitle: 'medPortalApp.reportLogs.home.title' },
        loadChildren: () => import('./flow/flow.module').then(m => m.FlowModule),
      },
      {
        path: 'reportLogs',
        data: { pageTitle: 'medPortalApp.reportLogs.home.title' },
        loadChildren: () => import('./logs/logs.module').then(m => m.LogsModule),
      },
      {
        path: 'custom-audit-event',
        data: { pageTitle: 'medPortalApp.customAuditEvent.home.title' },
        loadChildren: () => import('./custom-audit-event/custom-audit-event.module').then(m => m.CustomAuditEventModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
