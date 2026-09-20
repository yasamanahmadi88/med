import { ModuleDeclaration } from 'didi';
import ElementAccessRules from './ElementAccessRules';
import ElementAccessPaletteFilter from './ElementAccessPaletteFilter';
import ElementAccessReplaceMenuFilter from './ElementAccessReplaceMenuFilter';

const ElementAccessModule: ModuleDeclaration = {
  __init__: ['elementAccessRules', 'elementAccessPaletteFilter', 'elementAccessReplaceMenuFilter'],
  elementAccessRules: ['type', ElementAccessRules],
  elementAccessPaletteFilter: ['type', ElementAccessPaletteFilter],
  elementAccessReplaceMenuFilter: ['type', ElementAccessReplaceMenuFilter],
};

export default ElementAccessModule;
