import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';
import { Login } from './login.model';

@Injectable({ providedIn: 'root' })
export class LoginService {
  constructor(
    private accountService: AccountService,
    private authServerProvider: AuthServerProvider,
  ) {}

  login(credentials: Login): Observable<Account | null> {
    return this.authServerProvider.login(credentials).pipe(mergeMap(() => this.accountService.identity(true)));
  }

  logout(): void {
    // The server-side revoke is now awaited by AuthServerProvider (it clears the stored token in a
    // `finalize`), so it can settle after this call returns. Drop the client-side identity straight
    // away so the UI is logged out immediately and cannot bounce an in-flight session back to home,
    // and swallow a failing revoke: the local session is already gone either way.
    this.authServerProvider.logout().subscribe({ error: () => undefined });
    this.accountService.authenticate(null);
  }
}
