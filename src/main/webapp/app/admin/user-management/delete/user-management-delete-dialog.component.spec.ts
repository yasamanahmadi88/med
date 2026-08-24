import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { UserManagementService } from '../service/user-management.service';
import { UserManagementDeleteDialogComponent } from './user-management-delete-dialog.component';

describe('User Management Delete Component', () => {
  let comp: UserManagementDeleteDialogComponent;
  let fixture: ComponentFixture<UserManagementDeleteDialogComponent>;
  let service: UserManagementService;
  let mockActiveModal: NgbActiveModal;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [UserManagementDeleteDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { close: vi.fn(), dismiss: vi.fn() } }],
    })
      .overrideTemplate(UserManagementDeleteDialogComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(UserManagementDeleteDialogComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(UserManagementService);
    mockActiveModal = TestBed.inject(NgbActiveModal);
  });

  describe('confirmDelete', () => {
    it('Should call delete service on confirmDelete', () => {
      vi.spyOn(service, 'delete').mockReturnValue(of({}));
      comp.confirmDelete('user');
      expect(service.delete).toHaveBeenCalledWith('user');
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
