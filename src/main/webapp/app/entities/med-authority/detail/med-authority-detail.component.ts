import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IMedAuthority } from '../med-authority.model';

@Component({
  selector: 'jhi-med-authority-detail',
  templateUrl: './med-authority-detail.component.html',
  standalone: false,
})
export class MedAuthorityDetailComponent implements OnInit {
  medAuthority: IMedAuthority | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ medAuthority }) => {
      this.medAuthority = medAuthority;
    });
  }

  previousState(): void {
    window.history.back();
  }
}
