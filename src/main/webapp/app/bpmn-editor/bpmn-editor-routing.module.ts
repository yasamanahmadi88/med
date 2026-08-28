import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FlowBpmnEditorComponent } from './components/flow/flow-bpmn-editor.component';

const routes: Routes = [
  {
    path: '',
    component: FlowBpmnEditorComponent,
    data: { title: 'BPMN Editor' },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class BpmnEditorRoutingModule {}
