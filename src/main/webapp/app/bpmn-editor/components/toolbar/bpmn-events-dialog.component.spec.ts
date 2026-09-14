import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { BpmnEventsDialogComponent } from './bpmn-events-dialog.component';

describe('BpmnEventsDialogComponent', () => {
  let fixture: ComponentFixture<BpmnEventsDialogComponent>;
  let component: BpmnEventsDialogComponent;

  const activeModal = {
    dismiss: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [BpmnEventsDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: activeModal }],
    }).compileComponents();

    fixture = TestBed.createComponent(BpmnEventsDialogComponent);
    component = fixture.componentInstance;

    component.events = ['canvas.viewbox.changed', 'commandStack.changed', 'shape.added'];

    fixture.detectChanges();
  });

  it('shows the event names in numbered order', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).toContain('1：canvas.viewbox.changed');
    expect(text).toContain('2：commandStack.changed');
    expect(text).toContain('3：shape.added');
  });

  it('uses the same case-sensitive includes filter as the reference', () => {
    component.filter = 'command';

    expect(component.visibleEvents).toEqual(['commandStack.changed']);
  });

  it('clears the filter', () => {
    component.filter = 'shape';

    component.clearFilter();

    expect(component.filter).toBe('');
  });
});
