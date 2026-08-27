import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { BpmnEditorRoutingModule } from './bpmn-editor-routing.module';
import { BpmnEditorComponent } from './components/bpmn-editor.component';
import { DesignerComponent } from './components/designer/designer.component';
import { ToolbarComponent } from './components/toolbar/toolbar.component';
import { PaletteComponent } from './components/palette/palette.component';
import { PanelComponent } from './components/panel/panel.component';
import { SettingsComponent } from './components/settings/settings.component';
import { ContextMenuComponent } from './components/context-menu/context-menu.component';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    FormsModule,
    BpmnEditorRoutingModule,
    BpmnEditorComponent,
    DesignerComponent,
    ToolbarComponent,
    PaletteComponent,
    PanelComponent,
    SettingsComponent,
    ContextMenuComponent,
  ],
})
export class BpmnEditorModule {}
