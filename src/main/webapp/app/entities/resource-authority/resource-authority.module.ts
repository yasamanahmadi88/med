import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { ResourceAuthorityComponent } from './list/resource-authority.component';
import { ResourceAuthorityDetailComponent } from './detail/resource-authority-detail.component';
import { ResourceAuthorityUpdateComponent } from './update/resource-authority-update.component';
import { ResourceAuthorityDeleteDialogComponent } from './delete/resource-authority-delete-dialog.component';
import { ResourceAuthorityRoutingModule } from './route/resource-authority-routing.module';

@NgModule({
  imports: [SharedModule, ResourceAuthorityRoutingModule],
  declarations: [
    ResourceAuthorityComponent,
    ResourceAuthorityDetailComponent,
    ResourceAuthorityUpdateComponent,
    ResourceAuthorityDeleteDialogComponent,
  ],
})
export class ResourceAuthorityModule {}
