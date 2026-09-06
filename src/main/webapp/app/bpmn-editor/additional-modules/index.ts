import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';
import MinimapModule from 'diagram-js-minimap';

import EnhancementPalette from './Palette/EnhancementPalette';
import RewritePalette from './Palette/RewritePalette';
import EnhancementRenderer from './Renderer/EnhancementRenderer';
import RewriteRenderer from './Renderer/RewriteRenderer';
import CustomElementFactory from './ElementFactory';
import CustomRules from './Rules';

import activiti from '../moddle-extensions/activiti.json';
import flowable from '../moddle-extensions/flowable.json';
import cdrParser from '../moddle-extensions/cdrParserProperties.json';
import CdrParser from '../moddle-extensions/cdrParserModule.json';
import CsvTransformer from '../moddle-extensions/csvTransformerCornerModule.json';
import DbReceiver from '../moddle-extensions/dbReceiverModule.json';
import DbTransmitter from '../moddle-extensions/dbTransmitterModule.json';
import EventaDbReceiver from '../moddle-extensions/eventaDbReceiverModule.json';
import FileReceiver from '../moddle-extensions/fileReceiverModule.json';
import FileTransmitter from '../moddle-extensions/fileTransmitterModule.json';
import Fragmenter from '../moddle-extensions/fragmenterModule.json';
import HttpReceiver from '../moddle-extensions/httpReceiverModule.json';
import HttpReceiverEventa from '../moddle-extensions/httpReceiverEventaModule.json';
import HttpTransmitter from '../moddle-extensions/httpTransmitterModule.json';
import KafkaReceiver from '../moddle-extensions/kafkaReceiverModule.json';
import KafkaTransmitter from '../moddle-extensions/kafkaTransmitterModule.json';
import Merger from '../moddle-extensions/mergerModule.json';
import miyue from '../moddle-extensions/miyue.json';
import Transformer from '../moddle-extensions/transformerModule.json';

import { EditorSettings } from '../types/editor/settings';

/**
 * Moddle extensions for the editor's own integration modules.
 *
 * Each key is the namespace prefix the palette builds shapes with — creating, say, a
 * `KafkaReceiver:KafkaReceiver` shape only resolves once `KafkaReceiver` is registered, so these
 * travel with the custom palette rather than being optional.
 */
const integrationModuleExtensions: Record<string, unknown> = {
  CdrParser,
  CsvTransformer,
  DbReceiver,
  DbTransmitter,
  EventaDbReceiver,
  FileReceiver,
  FileTransmitter,
  Fragmenter,
  HttpReceiver,
  HttpReceiverEventa,
  HttpTransmitter,
  KafkaReceiver,
  KafkaTransmitter,
  Merger,
  miyue,
  Transformer,
};

/**
 * The four process engines are competing flavours of the same schema — activiti, flowable and
 * cdrParser are the Camunda moddle with the prefix renamed — so exactly one may be registered.
 *
 * Registering camunda and cdrParser together is also a hard failure: both extend bpmn:Definitions
 * with a `diagramRelationId` — `camunda:` on one side, `cdrParser:` on the other, but moddle
 * collides on the local name — and it refuses the second one ("property
 * <diagramRelationId> already defined"). It does not refuse it at construction — moddle builds
 * type descriptors lazily, so `new BpmnModeler(...)` succeeds and the first createDiagram or
 * importXML is what throws. activiti and flowable extend bpmn:Definitions not at all and would
 * quietly coexist with anything, which is why the one-engine rule is enforced here rather than
 * left to moddle to catch. `index.spec.ts` holds both halves.
 */
const engineExtensions: Record<string, unknown> = {
  camunda: camundaModdleDescriptor,
  activiti,
  flowable,
  cdrParser,
};

export function moddleExtensionsFor(settings: EditorSettings | undefined): Record<string, unknown> {
  const engine = settings?.processEngine ?? 'camunda';

  return { ...integrationModuleExtensions, [engine]: engineExtensions[engine] };
}

/**
 * The didi modules the palette and renderer settings select.
 *
 * - `paletteMode: 'enhancement'` adds the integration-module entries alongside bpmn-js's own
 *   palette; `'rewrite'` replaces the palette provider outright. `'default'` and `'custom'`
 *   register nothing — `'custom'` renders the separate Angular panel instead.
 * - `rendererMode` picks how those custom element types are drawn. A custom palette without a
 *   renderer would place shapes bpmn-js cannot draw, so CustomElementFactory and a renderer are
 *   registered whenever a custom palette is active.
 * - `otherModule` carries the extras that are neither palette nor renderer. Only the delete rule
 *   travelled across; see the README for what the Vue editor kept under this flag and why the
 *   rest did not.
 * - `miniMap` registers diagram-js-minimap. The setting has existed since the port began but
 *   reached nothing, so turning it off changed nothing and turning it on gave no minimap.
 */
export function additionalModulesFor(settings: EditorSettings | undefined): unknown[] {
  const modules: unknown[] = [];

  const palette = settings?.paletteMode;
  if (palette === 'enhancement') {
    modules.push(EnhancementPalette);
  } else if (palette === 'rewrite') {
    modules.push(RewritePalette);
  }

  const renderer = settings?.rendererMode;
  if (renderer === 'enhancement') {
    modules.push(EnhancementRenderer);
  } else if (renderer === 'rewrite') {
    modules.push(RewriteRenderer);
  }

  if (modules.length > 0) {
    modules.push(CustomElementFactory);
  }

  // `otherModule` is the Vue editor's switch for the extras that are not palette or renderer;
  // the rule protecting start and end events travelled under it.
  if (settings?.otherModule ?? true) {
    modules.push(CustomRules);
  }

  // `designer.scss` hides the minimap's own toggle widget, so the toolbar button is the only
  // way to open it — and neither works unless the module is registered, which is what this
  // setting now decides. Until this it decided nothing at all.
  if (settings?.miniMap ?? true) {
    modules.push(MinimapModule);
  }

  return modules;
}
