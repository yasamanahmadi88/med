import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';

import EnhancementPalette from './Palette/EnhancementPalette';
import RewritePalette from './Palette/RewritePalette';
import EnhancementRenderer from './Renderer/EnhancementRenderer';
import RewriteRenderer from './Renderer/RewriteRenderer';
import CustomElementFactory from './ElementFactory';

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
 * Registering more than one makes moddle refuse the duplicate extension of bpmn:Definitions
 * ("property <diagramRelationId> already defined") and the modeler fails to construct at all.
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

  return modules;
}
