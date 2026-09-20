import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { LogsComponent } from './list/logs.component';
import { LogsRoutingModule } from './route/logs-routing.module';
import { LogsDetailComponent } from './detail/logs-detail.component';

@NgModule({
  imports: [SharedModule, LogsRoutingModule],
  declarations: [LogsComponent, LogsDetailComponent],
})
export class LogsModule {}
