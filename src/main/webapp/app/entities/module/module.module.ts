import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { ModuleComponent } from './list/module.component';
import { ModuleDetailComponent } from './detail/module-detail.component';
import { ModuleUpdateComponent } from './update/module-update.component';
import { ModuleDeleteDialogComponent } from './delete/module-delete-dialog.component';
import { ModuleRoutingModule } from './route/module-routing.module';

@NgModule({
  imports: [SharedModule, ModuleRoutingModule],
  declarations: [ModuleComponent, ModuleDetailComponent, ModuleUpdateComponent, ModuleDeleteDialogComponent],
})
export class ModuleModule {}
