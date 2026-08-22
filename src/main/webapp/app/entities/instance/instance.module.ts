import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { InstanceComponent } from './list/instance.component';
import { InstanceDetailComponent } from './detail/instance-detail.component';
import { InstanceUpdateComponent } from './update/instance-update.component';
import { InstanceDeleteDialogComponent } from './delete/instance-delete-dialog.component';
import { InstanceRoutingModule } from './route/instance-routing.module';

@NgModule({
  imports: [SharedModule, InstanceRoutingModule],
  declarations: [InstanceComponent, InstanceDetailComponent, InstanceUpdateComponent, InstanceDeleteDialogComponent],
})
export class InstanceModule {}
