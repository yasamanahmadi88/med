import {
  Component,
  OnInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef,
  AfterViewInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import BpmnModeler from 'bpmn-js/lib/Modeler';
import { BpmnEditorService } from '../../services/bpmn-editor.service';

@Component({
  selector: 'app-designer',
  templateUrl: './designer.component.html',
  styleUrls: ['./designer.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class DesignerComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('canvas') canvas!: ElementRef;
  @Input() xml: string | undefined;
  @Output() xmlUpdate = new EventEmitter<string>();

  private bpmnModeler: BpmnModeler | null = null;
  private destroy$ = new Subject<void>();

  constructor(private bpmnEditorService: BpmnEditorService) {}

  ngOnInit(): void {}

  ngAfterViewInit(): void {
    this.initModeler();
  }

  private initModeler(): void {
    if (!this.canvas) {
      setTimeout(() => this.initModeler(), 100);
      return;
    }

    try {
      this.bpmnModeler = new BpmnModeler({
        container: this.canvas.nativeElement,
        keyboard: {
          bindTo: window
        }
      });

      this.bpmnEditorService.setBpmnModeler(this.bpmnModeler);

      if (this.xml) {
        this.loadXml(this.xml);
      }

      // Listen for xml changes
      this.bpmnModeler.on('commandStack.changed', () => {
        this.updateXml();
      });
    } catch (error) {
      console.error('Failed to initialize BPMN Modeler:', error);
    }
  }

  private loadXml(xml: string): void {
    if (!this.bpmnModeler) return;

    this.bpmnModeler.importXML(xml).catch((error: any) => {
      console.error('Could not import BPMN 2.0 diagram', error);
    });
  }

  private updateXml(): void {
    if (!this.bpmnModeler) return;

    this.bpmnModeler.saveXML({ format: true }).then((result: any) => {
      this.xmlUpdate.emit(result.xml);
    });
  }

  ngOnDestroy(): void {
    if (this.bpmnModeler) {
      this.bpmnModeler.destroy();
    }
    this.destroy$.next();
    this.destroy$.complete();
  }
}
