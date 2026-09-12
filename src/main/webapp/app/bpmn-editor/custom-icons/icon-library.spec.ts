import BpmnModdle, { ModdleElement } from 'bpmn-moddle';

import customIcon from '../moddle-extensions/customIcons.json';
import {
  CUSTOM_TASK_TYPE,
  CustomIcon,
  applyIcons,
  createIconElement,
  findIconLibrary,
  libraryBytes,
  nextIconId,
  rawIcons,
  readIconLibrary,
} from './icon-library';
import { toIconDataUri } from './svg-icon';

/**
 * The storage half of the feature, against the real moddle — the same thing bpmn-js builds from
 * `moddleExtensions` (`bpmn-js/lib/BaseViewer.js:61,646`). Asserting on plain objects would say
 * nothing about whether the library survives being written to XML and read back, which is the
 * entire claim: the icons travel with the flow.
 */
describe('the icon library in the diagram', () => {
  const CLEAN = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7" /></svg>';
  const CONTENTS = toIconDataUri(CLEAN);

  const moddle = new BpmnModdle({ customIcon });
  const factory = { create: (type: string, attrs?: Record<string, unknown>) => moddle.create(type, attrs ?? {}) };

  const diagramWith = (extensionElements: string, flowElements = ''): string => `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:customIcon="${customIcon.uri}" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  ${extensionElements}
  <bpmn:process id="Process_1" isExecutable="true">${flowElements}</bpmn:process>
</bpmn:definitions>`;

  const libraryOf = (...icons: string[]): string =>
    `<bpmn:extensionElements><customIcon:iconLibrary>${icons.join('')}</customIcon:iconLibrary></bpmn:extensionElements>`;

  const parse = async (xml: string): Promise<ModdleElement> => {
    const { rootElement, warnings } = await moddle.fromXML(xml, 'bpmn:Definitions');
    expect(warnings.map(warning => warning.message)).toEqual([]);
    return rootElement;
  };

  const serialise = async (definitions: ModdleElement): Promise<string> => (await moddle.toXML(definitions, { format: true })).xml;

  describe('reading', () => {
    it('reads the icons a diagram carries', async () => {
      const definitions = await parse(diagramWith(libraryOf(`<customIcon:icon iconId="Icon_1" name="Payment" contents="${CONTENTS}" />`)));

      expect(readIconLibrary(definitions)).toEqual([{ id: 'Icon_1', name: 'Payment', contents: CONTENTS }]);
    });

    it('has no library, and no icons, in a diagram that never used one', async () => {
      const definitions = await parse(diagramWith(''));

      expect(findIconLibrary(definitions)).toBeUndefined();
      expect(readIconLibrary(definitions)).toEqual([]);
      expect(readIconLibrary(undefined)).toEqual([]);
    });

    it('drops an icon whose contents is not the encoding we produce', async () => {
      // A `.bpmn` file is user input. This one is hand-written, and the only thing standing
      // between `contents` and an `<img src>` is this filter.
      const definitions = await parse(
        diagramWith(
          libraryOf(
            `<customIcon:icon iconId="Icon_1" name="hostile" contents="javascript:alert(1)" />`,
            `<customIcon:icon iconId="Icon_2" name="ok" contents="${CONTENTS}" />`,
          ),
        ),
      );

      expect(readIconLibrary(definitions).map(icon => icon.id)).toEqual(['Icon_2']);
    });

    it('drops an icon whose encoded SVG would not have passed upload', () => {
      const hostile = toIconDataUri('<svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script></svg>');
      const definitions = moddle.create('bpmn:Definitions', {});
      applyIcons(definitions, [createIconElement(factory, { id: 'Icon_1', name: 'hostile', contents: hostile })], factory);

      expect(readIconLibrary(definitions)).toEqual([]);
    });

    it('drops an icon with no id, and keeps only the first of a repeated one', async () => {
      const definitions = await parse(
        diagramWith(
          libraryOf(
            `<customIcon:icon name="nameless" contents="${CONTENTS}" />`,
            `<customIcon:icon iconId="Icon_1" name="first" contents="${CONTENTS}" />`,
            `<customIcon:icon iconId="Icon_1" name="second" contents="${CONTENTS}" />`,
          ),
        ),
      );

      expect(readIconLibrary(definitions).map(icon => icon.name)).toEqual(['first']);
    });
  });

  describe('writing', () => {
    it('writes the library into bpmn:Definitions, where it is saved with the diagram', async () => {
      const definitions = await parse(diagramWith(''));

      applyIcons(definitions, [createIconElement(factory, { id: 'Icon_1', name: 'Payment', contents: CONTENTS })], factory);
      const xml = await serialise(definitions);

      // Inside definitions' own extensionElements, not inside the process: one library per
      // document, and it is there whether or not any shape uses it yet.
      expect(xml).toContain('<bpmn:extensionElements>');
      expect(xml).toContain(`<customIcon:icon iconId="Icon_1" name="Payment" contents="${CONTENTS}" />`);
      expect(xml.indexOf('<customIcon:iconLibrary>')).toBeLessThan(xml.indexOf('<bpmn:process'));

      expect(readIconLibrary(await parse(xml))).toEqual([{ id: 'Icon_1', name: 'Payment', contents: CONTENTS }]);
    });

    it('adds no property to bpmn:Definitions itself', () => {
      // The one thing this schema must not do. `camunda` and `cdrParser` both add
      // `diagramRelationId` to bpmn:Definitions and cannot be registered together because of it.
      const extending = customIcon.types.filter(type => 'extends' in type);

      expect(extending).toEqual([]);
    });

    it('removes the library, and the extensionElements it made, when the last icon goes', async () => {
      const definitions = await parse(diagramWith(libraryOf(`<customIcon:icon iconId="Icon_1" name="Payment" contents="${CONTENTS}" />`)));

      applyIcons(definitions, [], factory);

      expect(findIconLibrary(definitions)).toBeUndefined();
      expect(await serialise(definitions)).not.toContain('extensionElements');
    });

    it('leaves any other extension element alone', async () => {
      const definitions = await parse(
        diagramWith(
          `<bpmn:extensionElements><customIcon:iconLibrary><customIcon:icon iconId="Icon_1" name="a" contents="${CONTENTS}" /></customIcon:iconLibrary></bpmn:extensionElements>`,
        ),
      );
      const other = moddle.create('bpmn:ExtensionElements', {});
      (definitions.get('extensionElements') as ModdleElement).set('values', [
        ...(definitions.get('extensionElements') as ModdleElement).get('values'),
        other,
      ]);

      applyIcons(definitions, [], factory);

      expect((definitions.get('extensionElements') as ModdleElement).get('values')).toEqual([other]);
    });

    it('round-trips a placed shape through XML', async () => {
      const definitions = await parse(
        diagramWith(
          libraryOf(`<customIcon:icon iconId="Icon_1" name="Payment" contents="${CONTENTS}" />`),
          `<customIcon:customTask id="Task_1" name="Payment" iconId="Icon_1" />`,
        ),
      );

      const reparsed = await parse(await serialise(definitions));
      const task = (reparsed.get('rootElements') as ModdleElement[])[0].get('flowElements')[0] as ModdleElement;

      // `$type` resolved rather than left as `$attrs` is what says the schema was applied: an
      // unregistered namespace survives a round trip too, as raw text moddle understands nothing of.
      expect(task.$type).toBe(CUSTOM_TASK_TYPE);
      expect(task.get('iconId')).toBe('Icon_1');
      expect(task.get('name')).toBe('Payment');
    });
  });

  describe('bookkeeping', () => {
    it('measures the library by what it adds to the saved document', () => {
      const icons: CustomIcon[] = [
        { id: 'Icon_1', name: 'a', contents: CONTENTS },
        { id: 'Icon_2', name: 'b', contents: CONTENTS },
      ];

      expect(libraryBytes(icons)).toBe(CONTENTS.length * 2);
      expect(libraryBytes([])).toBe(0);
    });

    it('numbers a new icon past the ones already there, including a gap', () => {
      const icon = (id: string): CustomIcon => ({ id, name: id, contents: CONTENTS });

      expect(nextIconId([])).toBe('Icon_1');
      expect(nextIconId([icon('Icon_1'), icon('Icon_2')])).toBe('Icon_3');
      expect(nextIconId([icon('Icon_1'), icon('Icon_3')])).toBe('Icon_2');
    });
  });

  it('keeps the icons out of the moddle element list nothing registered', async () => {
    // The check the Vue palette failed: it asked for `Custom:${icon.name}`, a namespace nothing
    // declares, so createShape threw on the first click. One registered type, whatever the icon.
    expect(() => moddle.getType(CUSTOM_TASK_TYPE)).not.toThrow();
    expect(() => moddle.getType('Custom:Payment')).toThrow();

    const rawFromDiagram = rawIcons(await parse(diagramWith(libraryOf(`<customIcon:icon iconId="Icon_1" contents="${CONTENTS}" />`))));
    expect(rawFromDiagram.map(icon => icon.$type)).toEqual(['customIcon:Icon']);
  });
});
