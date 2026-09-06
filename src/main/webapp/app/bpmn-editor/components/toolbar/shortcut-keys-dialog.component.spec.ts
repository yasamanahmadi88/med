import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ShortcutKeysDialogComponent } from './shortcut-keys-dialog.component';
import { SHORTCUT_GROUPS } from './shortcuts';

describe('ShortcutKeysDialogComponent', () => {
  let fixture: ComponentFixture<ShortcutKeysDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShortcutKeysDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { dismiss: vi.fn() } }],
    }).compileComponents();

    fixture = TestBed.createComponent(ShortcutKeysDialogComponent);
    fixture.detectChanges();
  });

  afterEach(() => fixture.destroy());

  it('renders every shortcut', () => {
    const expected = SHORTCUT_GROUPS.reduce((total, group) => total + group.shortcuts.length, 0);

    expect(fixture.nativeElement.querySelectorAll('dt')).toHaveLength(expected);
    expect(fixture.nativeElement.querySelectorAll('kbd')).toHaveLength(expected);
  });

  it('renders each group under its own heading', () => {
    const headings = Array.from(fixture.nativeElement.querySelectorAll('h5')).map(h => (h as HTMLElement).textContent);

    expect(headings).toEqual(SHORTCUT_GROUPS.map(group => group.title));
  });

  it('dismisses the modal on close', () => {
    fixture.componentInstance.close();

    expect(TestBed.inject(NgbActiveModal).dismiss).toHaveBeenCalled();
  });
});
