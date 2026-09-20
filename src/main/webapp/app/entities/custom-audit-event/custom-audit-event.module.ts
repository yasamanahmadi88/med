import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { CustomAuditEventComponent } from './list/custom-audit-event.component';
import { CustomAuditEventDetailComponent } from './detail/custom-audit-event-detail.component';
import { CustomAuditEventUpdateComponent } from './update/custom-audit-event-update.component';
import { CustomAuditEventDeleteDialogComponent } from './delete/custom-audit-event-delete-dialog.component';
import { CustomAuditEventRoutingModule } from './route/custom-audit-event-routing.module';

@NgModule({
  imports: [SharedModule, CustomAuditEventRoutingModule],
  declarations: [
    CustomAuditEventComponent,
    CustomAuditEventDetailComponent,
    CustomAuditEventUpdateComponent,
    CustomAuditEventDeleteDialogComponent,
  ],
})
export class CustomAuditEventModule {}
