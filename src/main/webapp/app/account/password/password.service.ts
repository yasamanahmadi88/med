import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface PasswordValidationResponse {
  valid: boolean;
  validationException?: string;
}
@Injectable({ providedIn: 'root' })
export class PasswordService {
  constructor(private http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

  validatePassword(password: string): Observable<PasswordValidationResponse> {
    return this.http.post<PasswordValidationResponse>('/api/password/validate', { password });
  }
  save(newPassword: string, currentPassword: string): Observable<{}> {
    return this.http.post(this.applicationConfigService.getEndpointFor('api/account/change-password'), { currentPassword, newPassword });
  }

  resetPassword(keyAndPassword: any): Observable<any> {
    return this.http.post(this.applicationConfigService.getEndpointFor('api/account/reset-password/finish'), keyAndPassword);
  }
  resetPasswordByAdmin(login: string, newPassword: string): Observable<void> {
    return this.http.post<void>(
      this.applicationConfigService.getEndpointFor(`api/admin/users/${encodeURIComponent(login)}/reset-password`),
      { newPassword }
    );
  }
}
