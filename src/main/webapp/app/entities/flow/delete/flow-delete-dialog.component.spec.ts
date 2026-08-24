import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { FlowService } from '../service/flow.service';

import { FlowDeleteDialogComponent } from './flow-delete-dialog.component';

describe('Flow Management Delete Component', () => {
  let comp: FlowDeleteDialogComponent;
  let fixture: ComponentFixture<FlowDeleteDialogComponent>;
  let service: FlowService;
  let mockActiveModal: NgbActiveModal;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [FlowDeleteDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { close: vi.fn(), dismiss: vi.fn() } }],
    })
      .overrideTemplate(FlowDeleteDialogComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(FlowDeleteDialogComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(FlowService);
    mockActiveModal = TestBed.inject(NgbActiveModal);
  });

  describe('confirmDelete', () => {
    it('Should call delete service on confirmDelete', () => {
      vi.spyOn(service, 'delete').mockReturnValue(of(new HttpResponse({ body: {} })));
      comp.confirmDelete(123);
      expect(service.delete).toHaveBeenCalledWith(123);
      expect(mockActiveModal.close).toHaveBeenCalledWith('deleted');
    });

    it('Should not call delete service on clear', () => {
      vi.spyOn(service, 'delete');
      comp.cancel();
      expect(service.delete).not.toHaveBeenCalled();
      expect(mockActiveModal.close).not.toHaveBeenCalled();
      expect(mockActiveModal.dismiss).toHaveBeenCalled();
    });
  });
});
