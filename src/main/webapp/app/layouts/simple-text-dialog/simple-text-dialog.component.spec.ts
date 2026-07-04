import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimpleTextDialogComponent } from './simple-text-dialog.component';

describe('SimpleTextDialogComponent', () => {
  let component: SimpleTextDialogComponent;
  let fixture: ComponentFixture<SimpleTextDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SimpleTextDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimpleTextDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
