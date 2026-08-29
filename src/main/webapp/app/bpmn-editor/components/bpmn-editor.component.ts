import { Component, OnInit, OnDestroy, ViewChild, ElementRef, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { BpmnEditorService } from '../services/bpmn-editor.service';
import { EditorSettings } from '../types/editor/settings';
import { DesignerComponent } from './designer/designer.component';
import { ToolbarComponent } from './toolbar/toolbar.component';
import { PaletteComponent } from './palette/palette.component';
import { PanelComponent } from './panel/panel.component';
import { SettingsComponent } from './settings/settings.component';
import { ContextMenuComponent } from './context-menu/context-menu.component';

@Component({
  selector: 'jhi-bpmn-editor',
  templateUrl: './bpmn-editor.component.html',
  styleUrls: ['./bpmn-editor.component.scss'],
  // bpmn-js builds its canvas, palette and properties panel as plain DOM at runtime, so those
  // nodes never receive the _ngcontent attribute emulated encapsulation scopes styles by.
  // Without None the whole editor renders unstyled.
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DesignerComponent,
    ToolbarComponent,
    PaletteComponent,
    PanelComponent,
    SettingsComponent,
    ContextMenuComponent,
  ],
})
export class BpmnEditorComponent implements OnInit, OnDestroy {
  @ViewChild('designerContainer') designerContainer!: ElementRef;

  editorSettings!: EditorSettings;
  processXml: string | undefined;
  private destroy$ = new Subject<void>();

  constructor(private bpmnEditorService: BpmnEditorService) {}

  ngOnInit(): void {
    this.bpmnEditorService.editorSettings$.pipe(takeUntil(this.destroy$)).subscribe(settings => {
      this.editorSettings = settings;
    });

    this.bpmnEditorService.processXml$.pipe(takeUntil(this.destroy$)).subscribe(xml => {
      this.processXml = xml;
    });

    // Prevent context menu
    document.addEventListener('contextmenu', ev => ev.preventDefault());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get customPalette(): boolean {
    return this.editorSettings?.paletteMode === 'custom';
  }

  get customPanel(): boolean {
    return this.editorSettings?.penalMode === 'custom';
  }

  get showToolbar(): boolean {
    return this.editorSettings?.toolbar ?? true;
  }

  get containerClasses(): string[] {
    const classes = ['designer-container'];
    if (this.customPalette) classes.push('designer-with-palette');
    if (this.customPanel) classes.push('designer-with-penal');
    if (this.editorSettings?.bg === 'grid-image') classes.push('designer-with-bg');
    if (this.editorSettings?.bg === 'image') classes.push('designer-with-image');
    return classes;
  }

  onXmlUpdate(xml: string): void {
    this.bpmnEditorService.setProcessXml(xml);
  }

  onSettingsUpdate(settings: Partial<EditorSettings>): void {
    this.bpmnEditorService.updateConfiguration(settings);
  }
}
