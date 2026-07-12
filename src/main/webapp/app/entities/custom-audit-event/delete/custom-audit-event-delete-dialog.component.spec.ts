import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { CustomAuditEventService } from '../service/custom-audit-event.service';

import { CustomAuditEventDeleteDialogComponent } from './custom-audit-event-delete-dialog.component';

describe('CustomAuditEvent Management Delete Component', () => {
  let comp: CustomAuditEventDeleteDialogComponent;
  let fixture: ComponentFixture<CustomAuditEventDeleteDialogComponent>;
  let service: CustomAuditEventService;
  let mockActiveModal: NgbActiveModal;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [CustomAuditEventDeleteDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { close: vi.fn(), dismiss: vi.fn() } }],
    })
      .overrideTemplate(CustomAuditEventDeleteDialogComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(CustomAuditEventDeleteDialogComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(CustomAuditEventService);
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
