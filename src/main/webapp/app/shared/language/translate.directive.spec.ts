import { vi } from 'vitest';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { TranslateDirective } from './translate.directive';

@Component({
  standalone: false,
  template: ` <div jhiTranslate="test"></div> `,
})
class TestTranslateDirectiveComponent {}

describe('TranslateDirective Tests', () => {
  let fixture: ComponentFixture<TestTranslateDirectiveComponent>;
  let translateService: TranslateService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      declarations: [TranslateDirective, TestTranslateDirectiveComponent],
    });
  });

  beforeEach(() => {
    translateService = TestBed.inject(TranslateService);
    fixture = TestBed.createComponent(TestTranslateDirectiveComponent);
  });

  it('should change HTML', () => {
    const spy = vi.spyOn(translateService, 'get');

    fixture.detectChanges();

    expect(spy).toHaveBeenCalled();
  });
});
