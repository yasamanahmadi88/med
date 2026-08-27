import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IResourceAuthority } from '../resource-authority.model';

@Component({
  selector: 'jhi-resource-authority-detail',
  templateUrl: './resource-authority-detail.component.html',
})
export class ResourceAuthorityDetailComponent implements OnInit {
  resourceAuthority: IResourceAuthority | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ resourceAuthority }) => {
      this.resourceAuthority = resourceAuthority;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
