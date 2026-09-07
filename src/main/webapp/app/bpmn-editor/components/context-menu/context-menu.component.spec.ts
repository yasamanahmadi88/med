import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { ContextMenuComponent } from './context-menu.component';

describe('ContextMenuComponent', () => {
  let fixture: ComponentFixture<ContextMenuComponent>;
  let component: ContextMenuComponent;
  let service: BpmnEditorService;
  let modeler: any;
  let contextMenuHandler: (event: { element?: any; originalEvent?: MouseEvent }) => void;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContextMenuComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ContextMenuComponent);
    component = fixture.componentInstance;
    service = TestBed.inject(BpmnEditorService);
    modeler = {
      on: vi.fn((eventName: string, _priority: number, handler: typeof contextMenuHandler) => {
        if (eventName === 'element.contextmenu') contextMenuHandler = handler;
      }),
      off: vi.fn(),
      get: vi.fn(),
    };

    fixture.detectChanges();
    service.setBpmnModeler(modeler);
  });

  afterEach(() => {
    vi.useRealTimers();
    fixture.destroy();
  });

  it('opens the create-element menu for the canvas root', () => {
    const event = new MouseEvent('contextmenu', { clientX: 120, clientY: 160, cancelable: true });

    contextMenuHandler({ originalEvent: event });

    const menu: HTMLElement = fixture.nativeElement.querySelector('[data-cy="bpmnContextMenu"]');
    expect(event.defaultPrevented).toBe(true);
    expect(menu).toBeTruthy();
    expect(menu.textContent).toContain('Create Element');
    expect(menu.textContent).toContain('Start Event');
    expect(menu.textContent).toContain('Exclusive Gateway');

    const labels = Array.from(menu.querySelectorAll<HTMLElement>('[role="menuitem"]')).map(item => item.textContent?.trim());
    expect(labels).not.toContain('Task');
    expect(labels.indexOf('Exclusive Gateway')).toBeLessThan(labels.indexOf('Message Boundary Event'));
  });

  it('starts creating the selected element and closes the menu', () => {
    vi.useFakeTimers();
    const shape = { businessObject: { di: {} } };
    const elementFactory = { createShape: vi.fn(() => shape) };
    const create = { start: vi.fn() };
    modeler.get.mockImplementation((name: string) => (name === 'elementFactory' ? elementFactory : create));
    contextMenuHandler({ originalEvent: new MouseEvent('contextmenu', { cancelable: true }) });

    const startEventButton: HTMLButtonElement = fixture.nativeElement.querySelector('[data-action="replace-with-none-start"]');
    startEventButton.click();
    vi.advanceTimersByTime(30);

    expect(elementFactory.createShape).toHaveBeenCalledWith(expect.objectContaining({ type: 'bpmn:StartEvent' }));
    expect(create.start).toHaveBeenCalledWith(expect.any(MouseEvent), shape);
    expect(component.visible).toBe(false);
  });

  it('replaces the selected element from the change-element menu', () => {
    const task = {
      businessObject: {
        $type: 'bpmn:Task',
        $instanceOf: (type: string) => type === 'bpmn:Task' || type === 'bpmn:FlowNode',
      },
    };
    const bpmnReplace = { replaceElement: vi.fn() };
    modeler.get.mockReturnValue(bpmnReplace);

    contextMenuHandler({ element: task, originalEvent: new MouseEvent('contextmenu', { cancelable: true }) });

    const menu: HTMLElement = fixture.nativeElement.querySelector('[data-cy="bpmnContextMenu"]');
    expect(menu.textContent).toContain('Change Element');

    const userTaskButton: HTMLButtonElement = fixture.nativeElement.querySelector('[data-action="replace-with-user-task"]');
    userTaskButton.click();

    expect(bpmnReplace.replaceElement).toHaveBeenCalledWith(task, expect.objectContaining({ type: 'bpmn:UserTask' }));
    expect(component.visible).toBe(false);
  });

  it('does not open when custom context menus are disabled', () => {
    service.updateConfiguration({ customContextmenu: false });
    const event = new MouseEvent('contextmenu', { cancelable: true });

    contextMenuHandler({ originalEvent: event });

    expect(event.defaultPrevented).toBe(false);
    expect(component.visible).toBe(false);
  });
});
