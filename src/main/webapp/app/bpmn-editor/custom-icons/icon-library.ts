import { ModdleElement } from 'bpmn-moddle';

import { isStoredIconSafe } from './svg-icon';

/**
 * Reading and writing the icon library that lives in the diagram's own XML.
 *
 * The library is a `customIcon:IconLibrary` inside `bpmn:Definitions`' `bpmn:extensionElements`,
 * which is where it has to be for the icons to travel with the flow: `FlowEntity.flow` holds one
 * document and nothing else, so anything not in that document is invisible to the next person who
 * opens it. `bpmn:Definitions` extends `bpmn:BaseElement` and therefore already has
 * `extensionElements` (`bpmn-moddle/resources/bpmn/json/bpmn.json`, `BaseElement`), so no property
 * is added to `bpmn:Definitions` — the one place camunda and cdrParser already collide.
 *
 * A placed shape is a `customIcon:CustomTask` carrying only the icon's id. One moddle type for
 * every icon, not one per icon: minting `Custom:${name}` types is what made the Vue palette throw,
 * because nothing had registered those namespaces (see the README's port status).
 */

export const ICON_LIBRARY_TYPE = 'customIcon:IconLibrary';
export const ICON_TYPE = 'customIcon:Icon';
export const CUSTOM_TASK_TYPE = 'customIcon:CustomTask';

const EXTENSION_ELEMENTS_TYPE = 'bpmn:ExtensionElements';

/** One icon, as the rest of the editor sees it. `contents` is always a `data:` URI. */
export interface CustomIcon {
  id: string;
  name: string;
  contents: string;
}

/** The subset of `BpmnFactory` this file needs, so its callers can be tested without a modeler. */
export interface ModdleElementFactory {
  create(type: string, attrs?: Record<string, unknown>): ModdleElement;
}

function extensionValues(definitions: ModdleElement | undefined): ModdleElement[] {
  return (definitions?.get('extensionElements') as ModdleElement | undefined)?.get('values') ?? [];
}

/** The library element itself, if this diagram has one. */
export function findIconLibrary(definitions: ModdleElement | undefined): ModdleElement | undefined {
  return extensionValues(definitions).find(value => value.$type === ICON_LIBRARY_TYPE);
}

/** The library's `customIcon:Icon` elements, exactly as they are stored. */
export function rawIcons(definitions: ModdleElement | undefined): ModdleElement[] {
  return (findIconLibrary(definitions)?.get('icons') as ModdleElement[] | undefined) ?? [];
}

/**
 * The icons this diagram offers, with everything unusable dropped.
 *
 * A `.bpmn` file is user input — imported from disk, or fetched from a flow somebody else saved —
 * so what comes back out of it is validated exactly as an upload is, rather than trusted because
 * it is already in the document. An icon that fails simply is not in the library, and any shape
 * pointing at it draws the missing-icon placeholder.
 */
export function readIconLibrary(definitions: ModdleElement | undefined): CustomIcon[] {
  const icons: CustomIcon[] = [];
  const seen = new Set<string>();

  for (const icon of rawIcons(definitions)) {
    const id = icon.get('iconId') as string | undefined;
    const contents = icon.get('contents');

    if (!id || seen.has(id) || !isStoredIconSafe(contents)) {
      continue;
    }

    seen.add(id);
    icons.push({ id, name: (icon.get('name') as string | undefined) ?? '', contents });
  }

  return icons;
}

/** How many characters this library adds to the saved document. base64 is ASCII, so also bytes. */
export function libraryBytes(icons: readonly CustomIcon[]): number {
  return icons.reduce((total, icon) => total + icon.contents.length, 0);
}

/**
 * An id no icon in `existing` uses.
 *
 * Sequential rather than random: it is written into the diagram, so it is read by people, and a
 * timestamp-and-random id like the Vue registry's tells them nothing (`customIconRegistry-fixed.ts:45`).
 */
export function nextIconId(existing: readonly CustomIcon[]): string {
  const taken = new Set(existing.map(icon => icon.id));
  for (let index = 1; ; index++) {
    const id = `Icon_${index}`;
    if (!taken.has(id)) {
      return id;
    }
  }
}

/** Build the stored form of an icon. */
export function createIconElement(factory: ModdleElementFactory, icon: CustomIcon): ModdleElement {
  return factory.create(ICON_TYPE, { iconId: icon.id, name: icon.name, contents: icon.contents });
}

/**
 * Replace the diagram's library with exactly these icon elements.
 *
 * An empty library is removed rather than left behind as an empty element, and the
 * `bpmn:extensionElements` it lived in goes with it if nothing else is using it — otherwise
 * uploading an icon and undoing it would leave a mark on every diagram that had ever been touched.
 */
export function applyIcons(definitions: ModdleElement, icons: readonly ModdleElement[], factory: ModdleElementFactory): void {
  const existing = findIconLibrary(definitions);

  if (icons.length === 0) {
    if (!existing) {
      return;
    }

    const extensionElements = definitions.get('extensionElements') as ModdleElement;
    const values = (extensionElements.get('values') as ModdleElement[]).filter(value => value !== existing);

    if (values.length === 0) {
      definitions.set('extensionElements', undefined);
    } else {
      extensionElements.set('values', values);
    }
    return;
  }

  const library = existing ?? createLibrary(definitions, factory);
  for (const icon of icons) {
    icon.$parent = library;
  }
  library.set('icons', [...icons]);
}

function createLibrary(definitions: ModdleElement, factory: ModdleElementFactory): ModdleElement {
  let extensionElements = definitions.get('extensionElements') as ModdleElement | undefined;

  if (!extensionElements) {
    extensionElements = factory.create(EXTENSION_ELEMENTS_TYPE, { values: [] });
    extensionElements.$parent = definitions;
    definitions.set('extensionElements', extensionElements);
  }

  const library = factory.create(ICON_LIBRARY_TYPE, { icons: [] });
  library.$parent = extensionElements;
  extensionElements.set('values', [...((extensionElements.get('values') as ModdleElement[] | undefined) ?? []), library]);

  return library;
}
