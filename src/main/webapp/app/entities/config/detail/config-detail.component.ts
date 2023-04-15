import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IConfig } from '../config.model';

@Component({
  selector: 'jhi-config-detail',
  templateUrl: './config-detail.component.html',
})
export class ConfigDetailComponent implements OnInit {
  config: IConfig | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ config }) => {
      this.config = config;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
