import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { ResourceComponent } from './list/resource.component';
import { ResourceDetailComponent } from './detail/resource-detail.component';
import { ResourceUpdateComponent } from './update/resource-update.component';
import { ResourceDeleteDialogComponent } from './delete/resource-delete-dialog.component';
import { ResourceRoutingModule } from './route/resource-routing.module';

@NgModule({
  imports: [SharedModule, ResourceRoutingModule],
  declarations: [ResourceComponent, ResourceDetailComponent, ResourceUpdateComponent, ResourceDeleteDialogComponent],
})
export class ResourceModule {}
