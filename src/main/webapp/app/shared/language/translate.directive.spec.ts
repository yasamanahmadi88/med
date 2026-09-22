import { SecurityContext, Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { translationNotFoundMessage } from 'app/config/translation.config';

import { TranslateDirective } from './translate.directive';

@Component({
  standalone: false,
  template: ` <div jhiTranslate="test"></div> `,
})
class TestTranslateDirectiveComponent {}

describe('TranslateDirective Tests', () => {
  let fixture: ComponentFixture<TestTranslateDirectiveComponent>;
  let translateService: TranslateService;
  let sanitizer: DomSanitizer;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      declarations: [TranslateDirective, TestTranslateDirectiveComponent],
    });
  });

  beforeEach(() => {
    translateService = TestBed.inject(TranslateService);
    sanitizer = TestBed.inject(DomSanitizer);
    fixture = TestBed.createComponent(TestTranslateDirectiveComponent);
  });

  it('requests the configured translation', () => {
    const spy = vi.spyOn(translateService, 'get');

    fixture.detectChanges();

    expect(spy).toHaveBeenCalled();
  });

  it('preserves safe translation markup', () => {
    const translation = '<strong>Saved!</strong> Please continue.';

    vi.spyOn(translateService, 'get').mockReturnValue(of(translation));

    fixture.detectChanges();

    const host = fixture.nativeElement.querySelector('div') as HTMLElement;

    expect(host.querySelector('strong')?.textContent).toBe('Saved!');
    expect(host.textContent).toContain('Please continue.');
  });

  it('sanitizes unsafe translated HTML before rendering it', () => {
    const payload = '<strong>Safe</strong><img src="x" onerror="alert(1)"><script>alert(1)</script>';
    const sanitizeSpy = vi.spyOn(sanitizer, 'sanitize');

    vi.spyOn(translateService, 'get').mockReturnValue(of(payload));

    fixture.detectChanges();

    const host = fixture.nativeElement.querySelector('div') as HTMLElement;
    expect(sanitizeSpy).toHaveBeenCalledWith(SecurityContext.HTML, payload);
    expect(host.querySelector('strong')?.textContent).toBe('Safe');
    expect(host.querySelector('script')).toBeNull();
    expect(host.innerHTML).not.toContain('onerror');
  });

  it('renders the translation-not-found fallback through the sanitizer', () => {
    const sanitizeSpy = vi.spyOn(sanitizer, 'sanitize');

    vi.spyOn(translateService, 'get').mockReturnValue(throwError(() => new Error('translation failed')));

    fixture.detectChanges();

    const fallback = `${translationNotFoundMessage}[test]`;
    const host = fixture.nativeElement.querySelector('div') as HTMLElement;

    expect(sanitizeSpy).toHaveBeenCalledWith(SecurityContext.HTML, fallback);
    expect(host.textContent).toBe(fallback);
  });
});
