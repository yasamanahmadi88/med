import type { BpmnEditorService } from '../../services/bpmn-editor.service';
import type { BpmnElementAccessService } from '../../services/bpmn-element-access.service';
import type { RoleModulesService } from '../../services/role-modules.service';

import { DesignerComponent } from './designer.component';

describe('DesignerComponent XML access guard', () => {
  function build(): {
    component: DesignerComponent;
    access: {
      findDisallowedXmlElements: ReturnType<typeof vi.fn>;
    };
    modeler: {
      importXML: ReturnType<typeof vi.fn>;
    };
  } {
    const access = {
      findDisallowedXmlElements: vi.fn(),
    };

    const modeler = {
      importXML: vi.fn().mockResolvedValue({}),
    };

    const component = new DesignerComponent(
      {} as BpmnEditorService,
      {} as RoleModulesService,
      access as unknown as BpmnElementAccessService,
    );

    (component as any).bpmnModeler = modeler;

    return {
      component,
      access,
      modeler,
    };
  }

  it('imports XML only after the access guard allows it', () => {
    const { component, access, modeler } = build();

    access.findDisallowedXmlElements.mockReturnValue([]);

    const xml = '<definitions />';

    (component as any).loadXml(xml);

    expect(access.findDisallowedXmlElements).toHaveBeenCalledOnce();
    expect(access.findDisallowedXmlElements).toHaveBeenCalledWith(xml);

    expect(modeler.importXML).toHaveBeenCalledOnce();
    expect(modeler.importXML).toHaveBeenCalledWith(xml);
  });

  it('does not import XML containing disallowed BPMN elements', () => {
    const { component, access, modeler } = build();

    access.findDisallowedXmlElements.mockReturnValue([
      {
        namespaceUri: 'http://www.omg.org/spec/BPMN/20100524/MODEL',
        localName: 'startEvent',
      },
    ]);

    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    (component as any).loadXml('<definitions />');

    expect(access.findDisallowedXmlElements).toHaveBeenCalledOnce();
    expect(modeler.importXML).not.toHaveBeenCalled();

    expect(consoleError).toHaveBeenCalledWith(
      'Could not import BPMN 2.0 diagram: 1 element type(s) are not allowed for the selected product.',
    );

    consoleError.mockRestore();
  });

  it('does not import malformed or unsafe XML when the guard throws', () => {
    const { component, access, modeler } = build();

    const error = new Error('Invalid or unsafe BPMN XML');

    access.findDisallowedXmlElements.mockImplementation(() => {
      throw error;
    });

    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    (component as any).loadXml('<!DOCTYPE definitions><definitions />');

    expect(access.findDisallowedXmlElements).toHaveBeenCalledOnce();
    expect(modeler.importXML).not.toHaveBeenCalled();

    expect(consoleError).toHaveBeenCalledWith(
      'Could not import BPMN 2.0 diagram: invalid or unsafe XML',
      error,
    );

    consoleError.mockRestore();
  });
});