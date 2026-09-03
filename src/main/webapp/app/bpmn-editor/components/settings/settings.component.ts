import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EditorSettings } from '../../types/editor/settings';

type SelectSettingKey = 'language' | 'bg';
type CheckboxSettingKey = 'toolbar' | 'miniMap';

@Component({
  selector: 'jhi-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class SettingsComponent implements OnChanges {
  @Input() settings!: EditorSettings;
  @Output() settingsUpdate = new EventEmitter<Partial<EditorSettings>>();

  showSettings = false;
  localSettings: EditorSettings | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['settings'] && this.settings) {
      this.localSettings = { ...this.settings };
    }
  }

  toggleSettings(): void {
    this.showSettings = !this.showSettings;
  }

  onSelectChange(key: SelectSettingKey, event: Event): void {
    const select = event.target;
    if (!(select instanceof HTMLSelectElement)) return;

    this.updateSetting(key, select.value);
  }

  onCheckboxChange(key: CheckboxSettingKey, event: Event): void {
    const checkbox = event.target;
    if (!(checkbox instanceof HTMLInputElement)) return;

    this.updateSetting(key, checkbox.checked);
  }

  closeSettings(): void {
    this.showSettings = false;
  }

  private updateSetting<K extends keyof EditorSettings>(key: K, value: EditorSettings[K]): void {
    if (!this.localSettings) return;

    this.localSettings = { ...this.localSettings, [key]: value };
    this.settingsUpdate.emit({ [key]: value });
  }
}
