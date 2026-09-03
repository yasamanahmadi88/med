import { ComponentFixture, TestBed } from '@angular/core/testing';

import { defaultSettings } from '../../config';
import { EditorSettings } from '../../types/editor/settings';
import { SettingsComponent } from './settings.component';

describe('BPMN SettingsComponent', () => {
  let fixture: ComponentFixture<SettingsComponent>;
  let component: SettingsComponent;
  let settings: EditorSettings;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [SettingsComponent] }).compileComponents();

    settings = { ...defaultSettings };
    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('settings', settings);
    fixture.detectChanges();
  });

  it('copies settings input instead of mutating the service value', () => {
    expect(component.localSettings).toEqual(settings);
    expect(component.localSettings).not.toBe(settings);
  });

  it('emits a primitive language value from a select change', () => {
    const emit = vi.spyOn(component.settingsUpdate, 'emit');
    component.toggleSettings();
    fixture.detectChanges();

    const select: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    select.value = 'en_US';
    select.dispatchEvent(new Event('change'));

    expect(component.localSettings?.language).toBe('en_US');
    expect(emit).toHaveBeenCalledWith({ language: 'en_US' });
  });

  it('emits a boolean from a checkbox change', () => {
    const emit = vi.spyOn(component.settingsUpdate, 'emit');
    component.toggleSettings();
    fixture.detectChanges();

    const checkbox: HTMLInputElement = fixture.nativeElement.querySelector('input[type="checkbox"]');
    checkbox.checked = false;
    checkbox.dispatchEvent(new Event('change'));

    expect(component.localSettings?.toolbar).toBe(false);
    expect(emit).toHaveBeenCalledWith({ toolbar: false });
  });

  it('refreshes its local copy when the input changes', () => {
    fixture.componentRef.setInput('settings', { ...settings, bg: 'white' });
    fixture.detectChanges();

    expect(component.localSettings?.bg).toBe('white');
  });
});
