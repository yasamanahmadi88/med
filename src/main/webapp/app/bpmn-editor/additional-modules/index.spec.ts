import EnhancementPalette from './Palette/EnhancementPalette';
import RewritePalette from './Palette/RewritePalette';
import EnhancementRenderer from './Renderer/EnhancementRenderer';
import RewriteRenderer from './Renderer/RewriteRenderer';
import CustomElementFactory from './ElementFactory';
import CustomRules from './Rules';
import MinimapModule from 'diagram-js-minimap';
import BpmnModdle, { ModdleElement, Package } from 'bpmn-moddle';
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';
import { additionalModulesFor, moddleExtensionsFor } from './index';
import { defaultSettings } from '../config';
import { EditorSettings } from '../types/editor/settings';

const settingsWith = (overrides: Partial<EditorSettings>): EditorSettings => ({ ...defaultSettings, ...overrides });

const engines = ['camunda', 'activiti', 'flowable', 'cdrParser'] as const;

// One element from a registered process engine and one from a registered integration module —
// the two kinds of extension `moddleExtensionsFor` returns.
const DIAGRAM = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:KafkaReceiver="KafkaReceiver" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:task id="Task_1" camunda:asyncBefore="true" />
    <KafkaReceiver:kafkaReceiver id="Receiver_1" name="orders" />
  </bpmn:process>
</bpmn:definitions>`;

describe('bpmn-editor additional modules', () => {
  describe('moddleExtensionsFor', () => {
    const moddleExtensions = moddleExtensionsFor(defaultSettings);

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

    it('keeps camunda on the camunda-bpmn-moddle descriptor itself, not a copy of it', () => {
      // The Vue editor carried `moddle-extensions/camunda.json`: camunda-bpmn-moddle 7.0.2's own
      // resources/camunda.json with one unreferenced `cdrParserProperties` type bolted on. A copy
      // like that keeps `name: 'Camunda'`, so identity is the only assertion that catches a fork.
      expect(defaultSettings.processEngine).toBe('camunda');
      expect(moddleExtensions['camunda']).toBe(camundaModdleDescriptor);
    });

    it('registers exactly one process-engine schema', () => {
      for (const engine of engines) {
        const registered = Object.keys(moddleExtensionsFor(settingsWith({ processEngine: engine })));

        expect(
          registered.filter(key => (engines as readonly string[]).includes(key)),
          `processEngine ${engine}`,
        ).toEqual([engine]);
      }
    });
  });

  /**
   * bpmn-js hands `moddleExtensions` straight to `new BpmnModdle(...)` (bpmn-js/lib/BaseViewer.js
   * lines 61 and 646), so this is the object the editor really builds. Asserting on the keys alone
   * — which is all the block above can do — never touches moddle, and moddle is where every way of
   * getting this wrong shows up.
   */
  describe('the moddle those extensions build', () => {
    // The prefix-keyed form, which is what bpmn-js hands moddle.
    const build = (extensions: Record<string, unknown>): BpmnModdle => new BpmnModdle(extensions);

    it('resolves bpmn:Definitions for every process engine', () => {
      // moddle builds type descriptors lazily, so a clashing pair of extensions does NOT throw in
      // the constructor: `new BpmnModeler(...)` succeeds and the first createDiagram/importXML is
      // what fails. Resolving the type is what forces the descriptor, so that is what is asserted.
      //
      // Only camunda and cdrParser can clash: both add a `diagramRelationId` to bpmn:Definitions
      // — `camunda:` on one side and `cdrParser:` on the other, but moddle collides on the local
      // name (camunda-bpmn-moddle/resources/camunda.json lines 10-23,
      // moddle-extensions/cdrParserProperties.json lines 10-21). activiti and flowable extend
      // bpmn:Definitions not at all, so they would coexist with anything — the one-engine rule is
      // about a diagram carrying one engine's schema, and only this pair also breaks the editor.
      for (const engine of engines) {
        const moddle = build(moddleExtensionsFor(settingsWith({ processEngine: engine })));

        expect(() => moddle.getType('bpmn:Definitions'), `processEngine ${engine}`).not.toThrow();
      }
    });

    it('resolves every type the custom palettes place', () => {
      // These are the exact strings the two palette providers hand to elementFactory.createShape.
      // An unresolved one throws "unknown type" the moment the entry is clicked.
      const moddle = build(moddleExtensionsFor(defaultSettings));

      for (const type of [
        'CdrParser:CdrParser',
        'CsvTransformer:CsvTransformer',
        'DbReceiver:DbReceiver',
        'DbTransmitter:DbTransmitter',
        'EventaDbReceiver:EventaDbReceiver',
        'FileReceiver:FileReceiver',
        'FileTransmitter:FileTransmitter',
        'Fragmenter:Fragmenter',
        'HttpReceiver:HttpReceiver',
        'HttpReceiverEventa:HttpReceiverEventa',
        'HttpTransmitter:HttpTransmitter',
        'KafkaReceiver:KafkaReceiver',
        'KafkaTransmitter:KafkaTransmitter',
        'Merger:Merger',
        'Transformer:Transformer',
        'miyue:SqlTask',
      ]) {
        expect(moddle.getType(type), type).toBeTruthy();
      }
    });

    it('parses and re-serialises what the extensions exist for', async () => {
      const moddle = build(moddleExtensionsFor(defaultSettings));
      const parsed = await moddle.fromXML(DIAGRAM, 'bpmn:Definitions');

      expect(parsed.warnings.map(warning => warning.message)).toEqual([]);

      // A namespace moddle does not know keeps its attributes as raw strings in `$attrs`, and its
      // elements never become flow elements at all — so a boolean `true` and a resolved `$type`
      // are what tell "the descriptor was applied" apart from "the value merely survived".
      const [task, receiver] = parsed.rootElement.rootElements[0].flowElements as ModdleElement[];
      expect(task.get('camunda:asyncBefore')).toBe(true);
      expect(receiver.$type).toBe('KafkaReceiver:KafkaReceiver');
      expect(receiver.get('name')).toBe('orders');

      const { xml } = await moddle.toXML(parsed.rootElement);
      expect(xml).toContain('camunda:asyncBefore="true"');
      expect(xml).toContain('<KafkaReceiver:kafkaReceiver id="Receiver_1" name="orders" />');
    });

    it('re-declares no package bpmn-moddle already owns', () => {
      // bpmn-moddle merges the extensions over its own packages BY KEY
      // (bpmn-moddle/dist/index.js lines 3730-3742), so a descriptor keyed `bpmn` silently
      // replaces the BPMN 2.0 schema and the same descriptor under any other key throws
      // "package with prefix <bpmn> already defined". The Vue editor's unused
      // `moddle-extensions/bpmn.json` is exactly that descriptor, which is why it is not here.
      const stock = build({}).getPackages();
      const prefixes = stock.map(({ prefix }) => prefix);
      const uris = stock.map(({ uri }) => uri);

      for (const engine of engines) {
        for (const [key, descriptor] of Object.entries(moddleExtensionsFor(settingsWith({ processEngine: engine })))) {
          const { prefix, uri } = descriptor as Package;

          expect(prefixes, `${key} prefix`).not.toContain(prefix);
          expect(uris, `${key} uri`).not.toContain(uri);
        }
      }
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

    it('registers no palette or renderer module when both are left on the stock behaviour', () => {
      const modules = additionalModulesFor(settingsWith({ paletteMode: 'default', rendererMode: 'default' }));

      for (const module of [EnhancementPalette, RewritePalette, EnhancementRenderer, RewriteRenderer, CustomElementFactory]) {
        expect(modules).not.toContain(module);
      }
    });

    it('protects the start and end events unless otherModule is off', () => {
      // The one extra the Vue editor kept under `otherModule` that carries behaviour; without it
      // a stray Delete leaves a process no engine will run.
      expect(additionalModulesFor(settingsWith({ otherModule: true }))).toContain(CustomRules);
      expect(additionalModulesFor(settingsWith({ otherModule: false }))).not.toContain(CustomRules);
    });

    it('treats the separate Angular palette panel as not needing a bpmn-js palette module', () => {
      const modules = additionalModulesFor(settingsWith({ paletteMode: 'custom', rendererMode: 'default' }));

      expect(modules).not.toContain(EnhancementPalette);
      expect(modules).not.toContain(RewritePalette);
    });

    it('registers the minimap only when the setting asks for it', () => {
      // The setting existed from the first commit of the port and reached nothing: no module was
      // ever registered, so the toolbar's toggle had nothing to toggle either way.
      expect(additionalModulesFor(settingsWith({ miniMap: true }))).toContain(MinimapModule);
      expect(additionalModulesFor(settingsWith({ miniMap: false }))).not.toContain(MinimapModule);
    });

    it('tolerates missing settings, keeping the delete rule and the minimap', () => {
      // Nothing to select a palette or renderer from, but the diagram still deserves its
      // start and end events, and both flags default to on.
      expect(additionalModulesFor(undefined)).toEqual([CustomRules, MinimapModule]);
    });
  });
});
