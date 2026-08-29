import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EditorSettings } from '../../types/editor/settings';

@Component({
  selector: 'jhi-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class SettingsComponent implements OnInit {
  @Input() settings!: EditorSettings;
  @Output() settingsUpdate = new EventEmitter<Partial<EditorSettings>>();

  showSettings = false;
  localSettings!: EditorSettings;

  ngOnInit(): void {
    if (this.settings) {
      this.localSettings = { ...this.settings };
    }
  }

  toggleSettings(): void {
    this.showSettings = !this.showSettings;
  }

  onSettingChange(key: keyof EditorSettings, value: any): void {
    this.localSettings = { ...this.localSettings, [key]: value };
    this.settingsUpdate.emit({ [key]: value });
  }

  closeSettings(): void {
    this.showSettings = false;
  }
}
