import type { ChangeDetectorRef } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

import type { BpmnEditorService } from '../../services/bpmn-editor.service';
import type { BpmnElementAccessService } from '../../services/bpmn-element-access.service';
import { ContextMenuComponent } from './context-menu.component';

interface AppendOpenEvent {
  x: number;
  y: number;
}

type AppendOpenHandler = (event: AppendOpenEvent) => void;

class EventBusStub {
  readonly onCalls: Array<{ event: string; callback: AppendOpenHandler }> = [];
  readonly offCalls: Array<{ event: string; callback: AppendOpenHandler }> = [];

  private listener: AppendOpenHandler | null = null;

  on(event: string, callback: AppendOpenHandler): void {
    this.onCalls.push({ event, callback });
    this.listener = callback;
  }

  off(event: string, callback: AppendOpenHandler): void {
    this.offCalls.push({ event, callback });

    if (this.listener === callback) {
      this.listener = null;
    }
  }

  fire(event: AppendOpenEvent): void {
    this.listener?.(event);
  }

  currentHandler(): AppendOpenHandler | null {
    return this.listener;
  }
}

class ChangeDetectorStub {
  detectChangesCalls = 0;

  detectChanges(): void {
    this.detectChangesCalls++;
  }
}

class ElementAccessStub {
  readonly allowedTypes = new Set<string>();

  isTypeAllowed(type: string | null | undefined): boolean {
    return type != null && this.allowedTypes.has(type);
  }
}

interface ModelerStub {
  get(name: string): unknown;
}

function createModeler(eventBus: EventBusStub): ModelerStub {
  return {
    get(name: string): unknown {
      if (name === 'eventBus') {
        return eventBus;
      }

      throw new Error(`Unexpected modeler service requested: ${name}`);
    },
  };
}

function build(): {
  component: ContextMenuComponent;
  modeler$: BehaviorSubject<ModelerStub | null>;
  access: ElementAccessStub;
  changeDetector: ChangeDetectorStub;
} {
  const modeler$ = new BehaviorSubject<ModelerStub | null>(null);
  const access = new ElementAccessStub();
  const changeDetector = new ChangeDetectorStub();

  const bpmnEditorService = {
    bpmnModeler$: modeler$.asObservable(),
  } as unknown as BpmnEditorService;

  const component = new ContextMenuComponent(
    bpmnEditorService,
    changeDetector as unknown as ChangeDetectorRef,
    access as unknown as BpmnElementAccessService,
  );

  return {
    component,
    modeler$,
    access,
    changeDetector,
  };
}

describe('ContextMenuComponent access lifecycle', () => {
  it('attaches one append listener when a modeler becomes available', () => {
    const { component, modeler$ } = build();
    const eventBus = new EventBusStub();

    component.ngOnInit();

    expect(eventBus.onCalls).toHaveLength(0);

    modeler$.next(createModeler(eventBus));

    expect(eventBus.onCalls).toHaveLength(1);
    expect(eventBus.onCalls[0].event).toBe('contextMenu.append.open');
    expect(eventBus.currentHandler()).toBe(eventBus.onCalls[0].callback);

    component.ngOnDestroy();
  });

  it('re-evaluates element access every time the append menu opens', () => {
    const { component, modeler$, access, changeDetector } = build();
    const eventBus = new EventBusStub();

    access.allowedTypes.add('bpmn:StartEvent');

    component.ngOnInit();
    modeler$.next(createModeler(eventBus));

    eventBus.fire({ x: 100, y: 200 });

    expect(component.open).toBe(true);
    expect(component.x).toBe(100);
    expect(component.y).toBe(200);
    expect(component.options.length).toBeGreaterThan(0);
    expect(component.options.every(option => option.target.type === 'bpmn:StartEvent')).toBe(true);
    expect(changeDetector.detectChangesCalls).toBe(1);

    // Simulate an access-policy change after the menu has already been used.
    access.allowedTypes.clear();

    eventBus.fire({ x: 300, y: 400 });

    expect(component.options).toHaveLength(0);
    expect(component.open).toBe(false);
    expect(changeDetector.detectChangesCalls).toBe(2);

    component.ngOnDestroy();
  });

  it('detaches the old event bus before attaching a replacement modeler', () => {
    const { component, modeler$ } = build();

    const firstEventBus = new EventBusStub();
    const secondEventBus = new EventBusStub();

    component.ngOnInit();

    modeler$.next(createModeler(firstEventBus));

    const originalHandler = firstEventBus.currentHandler();

    expect(originalHandler).not.toBeNull();

    component.open = true;

    modeler$.next(createModeler(secondEventBus));

    expect(firstEventBus.offCalls).toHaveLength(1);
    expect(firstEventBus.offCalls[0].event).toBe('contextMenu.append.open');
    expect(firstEventBus.offCalls[0].callback).toBe(originalHandler);
    expect(firstEventBus.currentHandler()).toBeNull();

    expect(secondEventBus.onCalls).toHaveLength(1);
    expect(secondEventBus.onCalls[0].callback).toBe(originalHandler);

    expect(component.open).toBe(false);

    component.ngOnDestroy();
  });

  it('detaches the active listener on destroy and ignores later modeler emissions', () => {
    const { component, modeler$ } = build();

    const firstEventBus = new EventBusStub();

    component.ngOnInit();
    modeler$.next(createModeler(firstEventBus));

    const handler = firstEventBus.currentHandler();

    expect(handler).not.toBeNull();

    component.ngOnDestroy();

    expect(firstEventBus.offCalls).toHaveLength(1);
    expect(firstEventBus.offCalls[0].event).toBe('contextMenu.append.open');
    expect(firstEventBus.offCalls[0].callback).toBe(handler);
    expect(firstEventBus.currentHandler()).toBeNull();

    const afterDestroyEventBus = new EventBusStub();

    modeler$.next(createModeler(afterDestroyEventBus));

    expect(afterDestroyEventBus.onCalls).toHaveLength(0);
  });
});
