import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faCog } from '@fortawesome/free-solid-svg-icons';
import { EditorSettings } from '../../types/editor/settings';

@Component({
  selector: 'jhi-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, FontAwesomeModule],
})
export class SettingsComponent implements OnInit {
  @Input() settings!: EditorSettings;
  @Output() settingsUpdate = new EventEmitter<Partial<EditorSettings>>();

  /**
   * The one icon this panel draws. Same reason the toolbar carries its own definitions: the
   * project ships FontAwesome as SVG components and loads no webfont stylesheet, so the
   * `<i class="fas fa-cog">` this button used rendered nothing — a blank blue circle.
   */
  readonly icons = { settings: faCog };

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

  /**
   * Applies one control's new value.
   *
   * The template passes `$event`, so this used to store the DOM `Event` itself as the setting.
   * Nothing threw: `toolbar` became a truthy object, so the checkbox could never hide the
   * toolbar; `bg` stopped equalling `'grid-image'`, so touching any control dropped the grid
   * background; and `language` reached `BpmnEditorService.updateConfiguration`, which writes it
   * to `sessionStorage`, leaving the string `[object Event]` behind for the next visit to load.
   */
  onSettingChange(key: keyof EditorSettings, event: Event): void {
    const target = event.target as HTMLInputElement | HTMLSelectElement;
    const value = target instanceof HTMLInputElement && target.type === 'checkbox' ? target.checked : target.value;

    this.localSettings = { ...this.localSettings, [key]: value };
    this.settingsUpdate.emit({ [key]: value });
  }

  closeSettings(): void {
    this.showSettings = false;
  }
}
