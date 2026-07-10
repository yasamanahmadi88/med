import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ICustomAuditEvent } from '../custom-audit-event.model';

@Component({
  selector: 'jhi-custom-audit-event-detail',
  templateUrl: './custom-audit-event-detail.component.html',
})
export class CustomAuditEventDetailComponent implements OnInit {
  customAuditEvent: ICustomAuditEvent | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ customAuditEvent }) => {
      this.customAuditEvent = customAuditEvent;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
