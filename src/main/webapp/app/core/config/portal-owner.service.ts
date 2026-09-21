import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

import { PortalOwner } from './portal-owner.model';

@Injectable({ providedIn: 'root' })
export class PortalOwnerService {
  private readonly resourceUrl: string;

  constructor(
    private readonly http: HttpClient,
    applicationConfigService: ApplicationConfigService,
  ) {
    this.resourceUrl = applicationConfigService.getEndpointFor('api/portal-owner/current');
  }

  getCurrent(): Observable<PortalOwner> {
    return this.http.get<PortalOwner>(this.resourceUrl);
  }
}
