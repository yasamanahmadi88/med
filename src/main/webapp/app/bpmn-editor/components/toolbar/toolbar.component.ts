import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class ToolbarComponent {
  onSave(): void { console.log('Save'); }
  onExport(): void { console.log('Export'); }
  onImport(): void { console.log('Import'); }
  onUndo(): void { console.log('Undo'); }
  onRedo(): void { console.log('Redo'); }
}
