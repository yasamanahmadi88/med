import EnhancementPalette from './Palette/EnhancementPalette';
import RewritePalette from './Palette/RewritePalette';
import EnhancementRenderer from './Renderer/EnhancementRenderer';
import RewriteRenderer from './Renderer/RewriteRenderer';
import CustomElementFactory from './ElementFactory';
import { additionalModulesFor, moddleExtensions } from './index';
import { defaultSettings } from '../config';
import { EditorSettings } from '../types/editor/settings';

const settingsWith = (overrides: Partial<EditorSettings>): EditorSettings => ({ ...defaultSettings, ...overrides });

describe('bpmn-editor additional modules', () => {
  describe('moddleExtensions', () => {
    it('registers every namespace the enhancement palette creates shapes with', () => {
      // EnhancementPaletteProvider builds these types; without the matching moddle extension
      // elementFactory.createShape throws on an unknown namespace.
      for (const prefix of [
        'KafkaReceiver',
        'KafkaTransmitter',
        'HttpReceiver',
        'HttpTransmitter',
        'FileReceiver',
        'FileTransmitter',
        'DbReceiver',
        'DbTransmitter',
        'Fragmenter',
        'CdrParser',
        'CsvTransformer',
        'Merger',
        'miyue',
      ]) {
        expect(moddleExtensions[prefix]).toBeTruthy();
      }
    });

    it('declares each extension under its own moddle prefix', () => {
      for (const [key, descriptor] of Object.entries(moddleExtensions)) {
        expect((descriptor as { prefix?: string }).prefix).toBe(key);
      }
    });

    it('keeps camunda on the camunda-bpmn-moddle descriptor the properties provider expects', () => {
      expect((moddleExtensions['camunda'] as { name?: string }).name).toBe('Camunda');
    });
  });

  describe('additionalModulesFor', () => {
    it('registers the enhancement palette by default', () => {
      const modules = additionalModulesFor(defaultSettings);

      expect(defaultSettings.paletteMode).toBe('enhancement');
      expect(modules).toContain(EnhancementPalette);
    });

    it('replaces the palette provider in rewrite mode', () => {
      const modules = additionalModulesFor(settingsWith({ paletteMode: 'rewrite' }));

      expect(modules).toContain(RewritePalette);
      expect(modules).not.toContain(EnhancementPalette);
    });

    it('selects the renderer independently of the palette', () => {
      expect(additionalModulesFor(settingsWith({ rendererMode: 'rewrite' }))).toContain(RewriteRenderer);
      expect(additionalModulesFor(settingsWith({ rendererMode: 'enhancement' }))).toContain(EnhancementRenderer);
    });

    it('adds the custom element factory whenever a custom palette or renderer is active', () => {
      // The custom types the palette places have their own default sizes; without the factory
      // bpmn-js would fall back to the plain task dimensions.
      expect(additionalModulesFor(settingsWith({ paletteMode: 'enhancement', rendererMode: 'default' }))).toContain(CustomElementFactory);
    });

    it('registers nothing when both are left on the stock behaviour', () => {
      expect(additionalModulesFor(settingsWith({ paletteMode: 'default', rendererMode: 'default' }))).toEqual([]);
    });

    it('treats the separate Angular palette panel as not needing a bpmn-js palette module', () => {
      const modules = additionalModulesFor(settingsWith({ paletteMode: 'custom', rendererMode: 'default' }));

      expect(modules).not.toContain(EnhancementPalette);
      expect(modules).not.toContain(RewritePalette);
    });

    it('tolerates missing settings', () => {
      expect(additionalModulesFor(undefined)).toEqual([]);
    });
  });
});
