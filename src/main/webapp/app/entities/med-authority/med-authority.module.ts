import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { MedAuthorityComponent } from './list/med-authority.component';
import { MedAuthorityDetailComponent } from './detail/med-authority-detail.component';
import { MedAuthorityUpdateComponent } from './update/med-authority-update.component';
import { MedAuthorityDeleteDialogComponent } from './delete/med-authority-delete-dialog.component';
import { MedAuthorityRoutingModule } from './route/med-authority-routing.module';

@NgModule({
  imports: [SharedModule, MedAuthorityRoutingModule],
  declarations: [MedAuthorityComponent, MedAuthorityDetailComponent, MedAuthorityUpdateComponent, MedAuthorityDeleteDialogComponent],
})
export class MedAuthorityModule {}
