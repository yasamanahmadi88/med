import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ResourceAuthorityDetailComponent } from './resource-authority-detail.component';

describe('ResourceAuthority Management Detail Component', () => {
  let comp: ResourceAuthorityDetailComponent;
  let fixture: ComponentFixture<ResourceAuthorityDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ResourceAuthorityDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ resourceAuthority: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(ResourceAuthorityDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(ResourceAuthorityDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load resourceAuthority on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.resourceAuthority).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
