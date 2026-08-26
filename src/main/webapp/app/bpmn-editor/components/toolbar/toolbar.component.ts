import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class ToolbarComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}

  onSave(): void {
    console.log('Save diagram');
  }

  onExport(): void {
    console.log('Export diagram');
  }

  onImport(): void {
    console.log('Import diagram');
  }

  onUndo(): void {
    console.log('Undo');
  }

  onRedo(): void {
    console.log('Redo');
  }
}
