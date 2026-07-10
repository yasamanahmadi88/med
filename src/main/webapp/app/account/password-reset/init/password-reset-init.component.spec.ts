import { vi } from 'vitest';
import { ElementRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { PasswordResetInitComponent } from './password-reset-init.component';
import { PasswordResetInitService } from './password-reset-init.service';

describe('PasswordResetInitComponent', () => {
  let fixture: ComponentFixture<PasswordResetInitComponent>;
  let comp: PasswordResetInitComponent;
  let service: PasswordResetInitService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [PasswordResetInitComponent],
      providers: [FormBuilder],
    }).overrideTemplate(PasswordResetInitComponent, '');
    fixture = TestBed.createComponent(PasswordResetInitComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(PasswordResetInitService);
  });

  it('sets focus after the view has been initialized', () => {
    const node = { focus: vi.fn() };
    comp.email = new ElementRef(node);
    comp.ngAfterViewInit();
    expect(node.focus).toHaveBeenCalled();
  });

  it('notifies of success upon successful requestReset', () => {
    vi.spyOn(service, 'save').mockReturnValue(of({}));
    comp.resetRequestForm.patchValue({ email: 'user@domain.com' });
    comp.requestReset();
    expect(service.save).toHaveBeenCalledWith('user@domain.com');
    expect(comp.success).toBe(true);
  });

  it('no notification of success upon error response', () => {
    vi.spyOn(service, 'save').mockReturnValue(
      throwError(() => ({
        status: 503,
        data: 'something else',
      }))
    );
    comp.resetRequestForm.patchValue({ email: 'user@domain.com' });
    comp.requestReset();
    expect(service.save).toHaveBeenCalledWith('user@domain.com');
    expect(comp.success).toBe(false);
  });
});
