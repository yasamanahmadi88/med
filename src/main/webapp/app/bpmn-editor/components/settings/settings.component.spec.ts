import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SettingsComponent } from './settings.component';
import { defaultSettings } from '../../config';
import { EditorSettings } from '../../types/editor/settings';

/**
 * The panel's controls hand `$event` to the component, so what it does with that argument is the
 * whole behaviour: emitting the DOM event instead of the value type-checks, throws nothing, and
 * leaves every setting holding an object. These specs read the emitted payload rather than the
 * call, because the call was never the part that was wrong.
 */
describe('SettingsComponent', () => {
  let fixture: ComponentFixture<SettingsComponent>;
  let emitted: Partial<EditorSettings>[];

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [SettingsComponent] }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    fixture.componentInstance.settings = { ...defaultSettings };
    emitted = [];
    fixture.componentInstance.settingsUpdate.subscribe(update => emitted.push(update));
    fixture.detectChanges();

    // Every control lives inside the panel, which starts closed.
    fixture.nativeElement.querySelector('.settings-toggle').click();
    fixture.detectChanges();
  });

  const control = (label: string): HTMLInputElement | HTMLSelectElement => {
    const row = [...fixture.nativeElement.querySelectorAll('.setting-item')].find((item: HTMLElement) =>
      item.textContent?.includes(label),
    ) as HTMLElement;
    return row.querySelector('select, input') as HTMLInputElement | HTMLSelectElement;
  };

  it('emits the chosen option, not the change event', () => {
    // `bg` is compared against string literals to pick the canvas background class, so an Event
    // here silently drops the grid the editor opens with.
    const background = control('Background') as HTMLSelectElement;
    background.value = 'image';
    background.dispatchEvent(new Event('change'));

    expect(emitted).toEqual([{ bg: 'image' }]);
  });

  it('emits a checkbox as the boolean it is showing', () => {
    // Unchecking has to reach the parent as `false`. An Event object is truthy, so the toolbar
    // stayed visible no matter what this checkbox showed.
    const toolbar = control('Show Toolbar') as HTMLInputElement;
    expect(toolbar.checked).toBe(true);

    toolbar.click();

    expect(toolbar.checked).toBe(false);
    expect(emitted).toEqual([{ toolbar: false }]);
  });

  it('keeps its own copy of the settings in step with what it emits', () => {
    const background = control('Background') as HTMLSelectElement;
    background.value = 'grid';
    background.dispatchEvent(new Event('change'));

    expect(fixture.componentInstance.localSettings.bg).toBe('grid');
  });

  it('draws the gear as an SVG icon', () => {
    // `<i class="fas fa-cog">` drew nothing: this project ships FontAwesome as SVG components
    // and loads no webfont stylesheet, so the toggle was a blank blue circle.
    expect(fixture.nativeElement.querySelectorAll('.settings-toggle svg.svg-inline--fa')).toHaveLength(1);
  });
});
