import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import {IReportLogs} from "../logs.model";

@Component({
  selector: 'jhi-log-detail',
  templateUrl: './logs-detail.component.html',
  standalone: false,
})
export class LogsDetailComponent implements OnInit {
  log: IReportLogs | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ log }) => {
      this.log = log;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
