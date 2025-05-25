import { Directive, Input, OnDestroy, TemplateRef, ViewContainerRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { AccountService } from 'app/core/auth/account.service';
import { Router } from '@angular/router';

@Directive({
  selector: '[jhiHasPermission]',
})
export class HasPermissionDirective implements OnDestroy {
  private logicalOp = 'AND';
  private authenticationSubscription?: Subscription;
  private params?: any[];
  private isHidden = true;

  constructor(
    private accountService: AccountService,
    protected router: Router,
    private templateRef: TemplateRef<any>,
    private viewContainerRef: ViewContainerRef
  ) {}

  @Input()
  set jhiHasPermission(val: any) {
    this.params = val;
    this.updateView();
    this.authenticationSubscription = this.accountService.getAuthenticationState().subscribe(() => this.updateView());
  }

  @Input()
  set jhiHasPermissionOp(permop: any) {
    this.logicalOp = permop;
    this.updateView();
    this.authenticationSubscription = this.accountService.getAuthenticationState().subscribe(() => this.updateView());
  }

  ngOnDestroy(): void {
    if (this.authenticationSubscription) {
      this.authenticationSubscription.unsubscribe();
    }
  }

  private updateView(): void {
    if (this.checkPermission()) {
      if (this.isHidden) {
        this.viewContainerRef.createEmbeddedView(this.templateRef);
        this.isHidden = false;
      }
    } else {
      this.isHidden = true;
      this.viewContainerRef.clear();
      // this.router.navigate(['/not-permitted/forbidden']);
    }
  }

  private checkPermission(): any {
    let hasPermission = false;
    const resources = this.accountService.loggedInUser?.resourceAuthorities;
    if (resources) {
      for (const resAuth of resources) {
        if (
          this.params &&
          this.params[0]?.toUpperCase() === resAuth.resource?.name?.toUpperCase() &&
          this.params[1]?.toUpperCase() === resAuth.verb?.toUpperCase()
        ) {
          hasPermission = true;
          break;
        }
      }
    }
    return hasPermission;
  }
}
