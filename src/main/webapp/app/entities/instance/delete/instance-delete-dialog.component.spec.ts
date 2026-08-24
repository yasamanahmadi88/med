import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { InstanceService } from '../service/instance.service';

import { InstanceDeleteDialogComponent } from './instance-delete-dialog.component';

describe('Instance Management Delete Component', () => {
  let comp: InstanceDeleteDialogComponent;
  let fixture: ComponentFixture<InstanceDeleteDialogComponent>;
  let service: InstanceService;
  let mockActiveModal: NgbActiveModal;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [InstanceDeleteDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { close: vi.fn(), dismiss: vi.fn() } }],
    })
      .overrideTemplate(InstanceDeleteDialogComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(InstanceDeleteDialogComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(InstanceService);
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
