import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { defaultSettings } from '../config';
import { EditorSettings } from '../types/editor/settings';

@Injectable({
  providedIn: 'root',
})
export class BpmnEditorService {
  private editorSettingsSubject = new BehaviorSubject<EditorSettings>({
    ...defaultSettings,
  });
  private processXmlSubject = new BehaviorSubject<string | undefined>(undefined);
  private bpmnModelerSubject = new BehaviorSubject<any>(null);

  editorSettings$ = this.editorSettingsSubject.asObservable();
  processXml$ = this.processXmlSubject.asObservable();
  bpmnModeler$ = this.bpmnModelerSubject.asObservable();

  constructor() {
    this.loadSettings();
  }

  private loadSettings(): void {
    const savedLang = sessionStorage.getItem('lang');
    if (savedLang) {
      this.updateLanguage(savedLang);
    }
  }

  getEditorSettings(): EditorSettings {
    return this.editorSettingsSubject.value;
  }

  updateConfiguration(conf: Partial<EditorSettings>): void {
    if (conf.language) {
      sessionStorage.setItem('lang', conf.language);
    }
    const currentSettings = this.editorSettingsSubject.value;
    this.editorSettingsSubject.next({ ...currentSettings, ...conf });
  }

  updateLanguage(lang: string): void {
    sessionStorage.setItem('lang', lang);
    const currentSettings = this.editorSettingsSubject.value;
    this.editorSettingsSubject.next({
      ...currentSettings,
      language: lang || 'zh_CN',
    });
  }

  getProcessDefinition(): { processName: string; processId: string } {
    const settings = this.editorSettingsSubject.value;
    return {
      processName: settings.processName,
      processId: settings.processId,
    };
  }

  getProcessEngine(): string {
    return this.editorSettingsSubject.value.processEngine;
  }

  setProcessXml(xml: string | undefined): void {
    this.processXmlSubject.next(xml);
  }

  getProcessXml(): string | undefined {
    return this.processXmlSubject.value;
  }

  setBpmnModeler(modeler: any): void {
    this.bpmnModelerSubject.next(modeler);
  }

  getBpmnModeler(): any {
    return this.bpmnModelerSubject.value;
  }
}
