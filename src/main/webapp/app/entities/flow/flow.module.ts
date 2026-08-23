import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { FlowComponent } from './list/flow.component';
import { FlowDetailComponent } from './detail/flow-detail.component';
import { FlowUpdateComponent } from './update/flow-update.component';
import { FlowDeleteDialogComponent } from './delete/flow-delete-dialog.component';
import { FlowRoutingModule } from './route/flow-routing.module';
import { FlowNewComponent } from './new/flow-new.component';

@NgModule({
  imports: [SharedModule, FlowRoutingModule],
  declarations: [FlowComponent, FlowDetailComponent, FlowUpdateComponent, FlowDeleteDialogComponent, FlowNewComponent],
})
export class FlowModule {}
