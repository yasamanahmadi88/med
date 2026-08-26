import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BpmnEditorComponent } from './components/bpmn-editor.component';

const routes: Routes = [
  {
    path: '',
    component: BpmnEditorComponent,
    data: { title: 'BPMN Editor' }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BpmnEditorRoutingModule {}
