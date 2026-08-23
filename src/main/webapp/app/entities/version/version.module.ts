import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { VersionComponent } from './list/version.component';
import { VersionDetailComponent } from './detail/version-detail.component';
import { VersionUpdateComponent } from './update/version-update.component';
import { VersionDeleteDialogComponent } from './delete/version-delete-dialog.component';
import { VersionRoutingModule } from './route/version-routing.module';

@NgModule({
  imports: [SharedModule, VersionRoutingModule],
  declarations: [VersionComponent, VersionDetailComponent, VersionUpdateComponent, VersionDeleteDialogComponent],
})
export class VersionModule {}
