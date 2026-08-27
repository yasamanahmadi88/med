import { vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ActivateService } from './activate.service';
import { ActivateComponent } from './activate.component';

describe('ActivateComponent', () => {
  let comp: ActivateComponent;
  let service: ActivateService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      declarations: [ActivateComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({ key: 'ABC123' }) },
        },
      ],
    })
      .overrideTemplate(ActivateComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    const fixture = TestBed.createComponent(ActivateComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(ActivateService);
  });

  it('calls activate.get with the key from params', () => {
    vi.spyOn(service, 'get').mockReturnValue(of({}));

    comp.ngOnInit();

    expect(service.get).toHaveBeenCalledWith('ABC123');
  });

  it('should set set success to true upon successful activation', () => {
    vi.spyOn(service, 'get').mockReturnValue(of({}));

    comp.ngOnInit();

    expect(comp.error).toBe(false);
    expect(comp.success).toBe(true);
  });

  it('should set set error to true upon activation failure', () => {
    vi.spyOn(service, 'get').mockReturnValue(throwError(() => 'ERROR'));

    comp.ngOnInit();

    expect(comp.error).toBe(true);
    expect(comp.success).toBe(false);
  });
});
