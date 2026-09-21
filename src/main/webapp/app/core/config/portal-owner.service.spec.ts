import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { PortalOwnerService } from './portal-owner.service';

describe('PortalOwnerService', () => {
  let service: PortalOwnerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });

    service = TestBed.inject(PortalOwnerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load the current server-resolved portal owner', () => {
    const expected = {
      code: 'MEDIATION',
      displayName: 'Mediation Portal',
    };

    service.getCurrent().subscribe(result => {
      expect(result).toEqual(expected);
    });

    const request = httpMock.expectOne(req => req.method === 'GET' && req.url.endsWith('api/portal-owner/current'));

    request.flush(expected);
  });
});
