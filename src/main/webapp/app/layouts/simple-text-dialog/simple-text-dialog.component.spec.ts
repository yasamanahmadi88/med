import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { SimpleTextDialogComponent } from './simple-text-dialog.component';

describe('SimpleTextDialogComponent', () => {
  let component: SimpleTextDialogComponent;
  let fixture: ComponentFixture<SimpleTextDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SimpleTextDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { close: vi.fn(), dismiss: vi.fn() } }],
    })
      .overrideTemplate(SimpleTextDialogComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(SimpleTextDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
