import { assign } from 'min-dash';
import PaletteProvider from 'bpmn-js/lib/features/palette/PaletteProvider';
import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory';
import Create from 'diagram-js/lib/features/create/Create';
import SpaceTool from 'diagram-js/lib/features/space-tool/SpaceTool';
import LassoTool from 'diagram-js/lib/features/lasso-tool/LassoTool';
import HandTool from 'diagram-js/lib/features/hand-tool/HandTool';
import GlobalConnect from 'diagram-js/lib/features/global-connect/GlobalConnect';
import Palette from 'diagram-js/lib/features/palette/Palette';

import { integrationModulePaletteEntries } from '../integration-modules';

class EnhancementPaletteProvider extends PaletteProvider {
  private readonly _palette: Palette;
  private readonly _create: Create;
  private readonly _elementFactory: ElementFactory;
  private readonly _spaceTool: SpaceTool;
  private readonly _lassoTool: LassoTool;
  private readonly _handTool: HandTool;
  private readonly _globalConnect: GlobalConnect;
  private readonly _translate: any;
  constructor(
    palette: any,
    create: any,
    elementFactory: any,
    spaceTool: any,
    lassoTool: any,
    handTool: any,
    globalConnect: any,
    translate: any,
  ) {
    super(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, translate);
    this._palette = palette;
    this._create = create;
    this._elementFactory = elementFactory;
    this._spaceTool = spaceTool;
    this._lassoTool = lassoTool;
    this._handTool = handTool;
    this._globalConnect = globalConnect;
    this._translate = translate;
  }
  getPaletteEntries() {
    const actions = {},
      create = this._create,
      elementFactory = this._elementFactory;

    // The integration-module entries are the same list the rewrite palette offers, and the same
    // allowlist decides them: see `integration-modules.ts`. Modules left off it still import,
    // render and edit — they are simply not offered as creation tools.
    assign(actions, integrationModulePaletteEntries(elementFactory, create));

    return actions;
  }
}

EnhancementPaletteProvider.$inject = [
  'palette',
  'create',
  'elementFactory',
  'spaceTool',
  'lassoTool',
  'handTool',
  'globalConnect',
  'translate',
];

export default EnhancementPaletteProvider;
