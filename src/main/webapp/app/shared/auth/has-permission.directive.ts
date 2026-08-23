import { Directive, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { AccountService } from 'app/core/auth/account.service';

type PermissionOperator = 'AND' | 'OR';

@Directive({
  selector: '[jhiHasPermission]',
})
export class HasPermissionDirective implements OnDestroy {
  private permissions: string[][] = [];
  private operator: PermissionOperator = 'AND';
  private authenticationSubscription?: Subscription;
  private hasView = false;

  constructor(
    private accountService: AccountService,
    private templateRef: TemplateRef<unknown>,
    private viewContainerRef: ViewContainerRef
  ) {
    this.authenticationSubscription = this.accountService
      .getAuthenticationState()
      .subscribe(() => this.updateView());
  }

  @Input()
  set jhiHasPermission(value: string[] | string[][]) {
    this.permissions = this.normalizePermissions(value);
    this.updateView();
  }

  @Input()
  set jhiHasPermissionOp(value: PermissionOperator | string) {
    this.operator = String(value || 'AND').toUpperCase() === 'OR' ? 'OR' : 'AND';
    this.updateView();
  }

  ngOnDestroy(): void {
    this.authenticationSubscription?.unsubscribe();
  }

  private updateView(): void {
    const allowed = this.checkPermission();

    if (allowed && !this.hasView) {
      this.viewContainerRef.createEmbeddedView(this.templateRef);
      this.hasView = true;
      return;
    }

    if (!allowed && this.hasView) {
      this.viewContainerRef.clear();
      this.hasView = false;
    }
  }

  private checkPermission(): boolean {
    if (!this.permissions.length) {
      return false;
    }

    const resourceAuthorities = this.accountService.loggedInUser?.resourceAuthorities || [];

    const checks = this.permissions.map(([resourceName, verb]) =>
      resourceAuthorities.some(
        resourceAuthority =>
          resourceAuthority.resource?.name?.toUpperCase() === resourceName.toUpperCase() &&
          resourceAuthority.verb?.toUpperCase() === verb.toUpperCase()
      )
    );

    return this.operator === 'OR' ? checks.some(Boolean) : checks.every(Boolean);
  }

  private normalizePermissions(value: string[] | string[][]): string[][] {
    if (!Array.isArray(value)) {
      return [];
    }

    if (value.length === 2 && typeof value[0] === 'string' && typeof value[1] === 'string') {
      return [value as string[]];
    }

    return (value as string[][]).filter(
      permission =>
        Array.isArray(permission) &&
        permission.length === 2 &&
        typeof permission[0] === 'string' &&
        typeof permission[1] === 'string'
    );
  }


     // for use ->     *jhiHasPermission="[['config', 'create'], ['config', 'edit']]; op: 'OR'"

}
