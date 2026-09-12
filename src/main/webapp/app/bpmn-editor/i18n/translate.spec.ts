import zh_CN from './zh_CN';
import { createBpmnTranslate, translationModuleFor } from './translate';

describe('BPMN runtime translation adapter', () => {
  it('keeps English on the safe identity translation path', () => {
    const translate = createBpmnTranslate('en_US');

    expect(translate('Task')).toBe('Task');
    expect(translate('Create {type}', { type: 'StartEvent' })).toBe('Create StartEvent');
  });

  it('uses the Chinese dictionary only for zh_CN', () => {
    const translate = createBpmnTranslate('zh_CN');

    expect(translate('Task')).toBe(zh_CN.elements.Task);

    const expected = zh_CN.elements['Create {type}'].replace('{type}', 'StartEvent');

    expect(translate('Create {type}', { type: 'StartEvent' })).toBe(expected);
  });

  it('does not translate replacement values a second time', () => {
    const translate = createBpmnTranslate('zh_CN');

    expect(translate('Unknown {name}', { name: 'Value' })).toBe('Unknown Value');
    expect(translate('Unknown {name}')).toBe('Unknown {name}');
  });

  it('exposes the diagram-js value-provider contract', () => {
    const module = translationModuleFor('zh_CN');

    expect(module.translate[0]).toBe('value');
    expect(typeof module.translate[1]).toBe('function');
    expect(module.translate[1]('Task')).toBe(zh_CN.elements.Task);
  });
});
