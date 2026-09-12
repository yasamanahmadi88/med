import zh_CN from './zh_CN';
import en_US from './en_US';
import { createBpmnTranslate, translationModuleFor } from './translate';

describe('bpmn-editor runtime translation', () => {
  it('leaves English on the identity path', () => {
    // en_US is deliberately not registered: the bundle is largely untranslated Chinese, so
    // wiring it up would turn the palette, context pad and popup menu Chinese. Falling through
    // gives bpmn-js's own English labels, which is what users have always seen.
    const translate = createBpmnTranslate('en_US');

    expect(translate('Task')).toBe('Task');
    expect(translate('Append EndEvent')).toBe('Append EndEvent');
  });

  it('proves that is the reason, not an oversight', () => {
    // If this ever fails because en_US has been translated, en_US may join TRANSLATED_LANGS.
    expect(en_US.elements.Task).toBe(zh_CN.elements.Task);
  });

  it('translates through the Chinese bundle when zh_CN is selected', () => {
    const translate = createBpmnTranslate('zh_CN');

    expect(translate('Task')).toBe(zh_CN.elements.Task);
  });

  it('reaches every area of the bundle, not just elements', () => {
    // diagram-js asks for one flat lookup; the bundle is split five ways, so a merge that missed
    // an area would leave that part of the UI untranslated with no error.
    const translate = createBpmnTranslate('zh_CN');
    const [panelKey] = Object.keys(zh_CN.panel);
    const [toolbarKey] = Object.keys(zh_CN.toolbar);

    expect(translate(panelKey)).toBe((zh_CN.panel as Record<string, string>)[panelKey]);
    expect(translate(toolbarKey)).toBe((zh_CN.toolbar as Record<string, string>)[toolbarKey]);
  });

  it('substitutes placeholders in the translated template', () => {
    const translate = createBpmnTranslate('zh_CN');
    const template = 'Create {type}';
    const expected = (zh_CN.elements as Record<string, string>)[template].replace('{type}', 'StartEvent');

    expect(translate(template, { type: 'StartEvent' })).toBe(expected);
  });

  it('substitutes placeholders on the identity path too', () => {
    expect(createBpmnTranslate('en_US')('Create {type}', { type: 'StartEvent' })).toBe('Create StartEvent');
  });

  it('leaves a placeholder alone when no replacement was supplied', () => {
    // diagram-js passes replacements for some labels and not others. A {type} that silently
    // became "undefined" would read as a rendering bug rather than a missing argument.
    const translate = createBpmnTranslate('en_US');

    expect(translate('Unknown {name}')).toBe('Unknown {name}');
    expect(translate('Unknown {name}', { other: 'x' })).toBe('Unknown {name}');
  });

  it('substitutes an empty or zero replacement rather than treating it as absent', () => {
    const translate = createBpmnTranslate('en_US');

    expect(translate('Value: {n}', { n: 0 })).toBe('Value: 0');
    expect(translate('Value: {n}', { n: '' })).toBe('Value: ');
  });

  it('does not translate the replacement values themselves', () => {
    // Only the template is looked up. A value that happens to be a bundle key must come through
    // as the caller passed it.
    const translate = createBpmnTranslate('zh_CN');

    expect(translate('Unknown {name}', { name: 'Task' })).toBe('Unknown Task');
  });

  it('falls back to identity for an unknown or absent language', () => {
    expect(createBpmnTranslate('fa_IR')('Task')).toBe('Task');
    expect(createBpmnTranslate(undefined)('Task')).toBe('Task');
    expect(createBpmnTranslate('')('Task')).toBe('Task');
  });

  it('exposes diagram-js value-provider shape', () => {
    // 'value' rather than 'type': the language is known when the modeler is built, and
    // designer.component.ts rebuilds the modeler when the settings change.
    const module = translationModuleFor('zh_CN');

    expect(module.translate[0]).toBe('value');
    expect(module.translate[1]('Task')).toBe(zh_CN.elements.Task);
  });
});
