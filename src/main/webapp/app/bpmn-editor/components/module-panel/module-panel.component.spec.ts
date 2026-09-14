import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';

import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { ModulePanelComponent } from './module-panel.component';

describe('ModulePanelComponent', () => {
  let fixture: ComponentFixture<ModulePanelComponent>;
  let modeler$: BehaviorSubject<any>;
  let listeners: Map<string, (event?: any) => void>;
  let modeling: { updateModdleProperties: ReturnType<typeof vi.fn> };
  let registry: { find: ReturnType<typeof vi.fn> };
  let modeler: any;

  const httpReceiver = {
    id: 'HttpReceiver_1',
    type: 'HttpReceiver:HttpReceiver',
    businessObject: {
      $type: 'HttpReceiver:HttpReceiver',
      get: vi.fn((property: string) =>
        property === 'camunda:agreementMode' ? 'RUNNING' : property === 'camunda:authPassword' ? 'secret' : '',
      ),
    },
  };

  const kafkaReceiver = {
    id: 'KafkaReceiver_1',
    type: 'KafkaReceiver:KafkaReceiver',
    businessObject: {
      $type: 'KafkaReceiver:KafkaReceiver',
      get: vi.fn(() => ''),
    },
  };

  beforeEach(async () => {
    listeners = new Map();
    modeling = { updateModdleProperties: vi.fn() };
    registry = { find: vi.fn().mockReturnValue(undefined) };
    modeler = {
      get: vi.fn((name: string) => {
        if (name === 'modeling') return modeling;
        if (name === 'elementRegistry') return registry;
        return undefined;
      }),
      on: vi.fn((name: string, handler: (event?: any) => void) => listeners.set(name, handler)),
      off: vi.fn(),
    };
    modeler$ = new BehaviorSubject<any>(null);

    await TestBed.configureTestingModule({
      imports: [ModulePanelComponent],
      providers: [
        {
          provide: BpmnEditorService,
          useValue: {
            bpmnModeler$: modeler$.asObservable(),
            getProcessEngine: () => 'camunda',
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModulePanelComponent);
    fixture.detectChanges();
    modeler$.next(modeler);
    listeners.get('selection.changed')?.({ newSelection: [httpReceiver] });
    fixture.detectChanges();
  });

  afterEach(() => {
    if (!fixture.componentRef.hostView.destroyed) {
      fixture.destroy();
    }
  });

  it('renders the HttpReceiver fields in the exact order used by the Vue panel', () => {
    const labels = Array.from(fixture.nativeElement.querySelectorAll('.field-label label')).map((label: any) =>
      label.textContent.trim().replace(/:$/, ''),
    );

    expect(labels).toEqual([
      'Agreement Mode',
      'Validator',
      'Response Transformer',
      'Auth Token Verification Url',
      'Auth Token Verification Response Validator',
      'Auth Username',
      'Auth Password',
      'Async Message Types',
      'Comment Desc',
    ]);
    expect(fixture.nativeElement.querySelector('[data-field="transformer"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('[data-field="transferType"]')).toBeNull();
  });

  it('renders KafkaReceiver fields without role-module API gating', () => {
    listeners.get('selection.changed')?.({ newSelection: [kafkaReceiver] });
    fixture.detectChanges();

    const labels = Array.from(fixture.nativeElement.querySelectorAll('.field-label label')).map((label: any) =>
      label.textContent.trim().replace(/:$/, ''),
    );

    expect(labels).toEqual(['Agreement Mode', 'Comment Desc']);
    expect(fixture.componentInstance.visibleFields.map(field => field.name)).toEqual(['agreementMode', 'commentDesc']);
  });

  it('writes through modeling under the configured process-engine prefix', () => {
    fixture.componentInstance.updateField(fixture.componentInstance.visibleFields.find(field => field.name === 'agreementMode')!, 'DRAFT');

    expect(modeling.updateModdleProperties).toHaveBeenCalledWith(httpReceiver, httpReceiver.businessObject, {
      'camunda:agreementMode': 'DRAFT',
    });
  });

  it('masks a password until the user explicitly reveals it', () => {
    const password = (): HTMLInputElement => fixture.nativeElement.querySelector('[data-field="authPassword"] input');
    const toggle: HTMLButtonElement = fixture.nativeElement.querySelector('[data-field="authPassword"] .password-toggle');

    expect(password().type).toBe('password');
    toggle.click();
    fixture.detectChanges();
    expect(password().type).toBe('text');
  });

  it('hides the custom panel when the process root is selected', () => {
    const process = { id: 'Process_1', type: 'bpmn:Process', businessObject: { $type: 'bpmn:Process' } };

    listeners.get('selection.changed')?.({ newSelection: [process] });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-cy="bpmnModulePanel"]')).toBeNull();
  });

  it('detaches bpmn-js event listeners when destroyed', () => {
    fixture.destroy();

    expect(modeler.off).toHaveBeenCalledWith('import.done', expect.any(Function));
    expect(modeler.off).toHaveBeenCalledWith('selection.changed', expect.any(Function));
    expect(modeler.off).toHaveBeenCalledWith('element.changed', expect.any(Function));
  });
});
