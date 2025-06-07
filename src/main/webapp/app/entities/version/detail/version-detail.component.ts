import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IVersion } from '../version.model';

@Component({
  selector: 'jhi-version-detail',
  templateUrl: './version-detail.component.html',
})
export class VersionDetailComponent implements OnInit {
  version: IVersion | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ version }) => {
      this.version = version;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
