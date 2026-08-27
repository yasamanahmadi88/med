import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BpmnEditorService } from '../../services/bpmn-editor.service';

@Component({
  selector: 'jhi-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class ToolbarComponent {
  constructor(private bpmnEditorService: BpmnEditorService) {}

  /** Keep the current diagram in the editor service so other views can read it back. */
  onSave(): void {
    void this.currentXml().then(xml => {
      if (xml) {
        this.bpmnEditorService.setProcessXml(xml);
      }
    });
  }

  /** Download the current diagram as a .bpmn file. */
  onExport(): void {
    void this.currentXml().then(xml => {
      if (!xml) {
        return;
      }
      const { processId } = this.bpmnEditorService.getProcessDefinition();
      const url = URL.createObjectURL(new Blob([xml], { type: 'application/xml' }));
      const link = document.createElement('a');
      link.href = url;
      link.download = `${processId || 'diagram'}.bpmn`;
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  /** Read a .bpmn/.xml file from disk and load it into the modeler. */
  onImport(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.bpmn,.xml';
    input.onchange = () => {
      const file = input.files?.[0];
      if (!file) {
        return;
      }
      void file.text().then(xml => {
        const modeler = this.bpmnEditorService.getBpmnModeler();
        if (!modeler) {
          return;
        }
        modeler.importXML(xml).catch((error: unknown) => console.error('Could not import BPMN 2.0 diagram', error));
        this.bpmnEditorService.setProcessXml(xml);
      });
    };
    input.click();
  }

  onUndo(): void {
    this.commandStack()?.undo();
  }

  onRedo(): void {
    this.commandStack()?.redo();
  }

  private commandStack(): any {
    return this.bpmnEditorService.getBpmnModeler()?.get('commandStack');
  }

  private async currentXml(): Promise<string | undefined> {
    const modeler = this.bpmnEditorService.getBpmnModeler();
    if (!modeler) {
      return undefined;
    }
    try {
      const { xml } = await modeler.saveXML({ format: true });
      return xml;
    } catch (error: unknown) {
      console.error('Could not export BPMN 2.0 diagram', error);
      return undefined;
    }
  }
}
