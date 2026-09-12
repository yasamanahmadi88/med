import { ModuleDeclaration } from 'didi';

import CustomIconLibrary from './CustomIconLibrary';
import CustomIconPaletteProvider from './CustomIconPaletteProvider';
import CustomIconRenderer from './CustomIconRenderer';

/**
 * Custom icons: the library that travels in the diagram, its palette entries and their renderer.
 *
 * All three are in `__init__` because none of them is asked for by name. `customIcons` registers
 * the command handler and the palette refresh, the provider registers itself with the palette, and
 * the renderer registers itself with the event bus — didi only builds a service when something
 * needs it, and nothing does.
 *
 * `customIcons` is also the handle the upload dialog reaches through `modeler.get('customIcons')`.
 */
const CustomIcons: ModuleDeclaration = {
  __init__: ['customIcons', 'customIconPaletteProvider', 'customIconRenderer'],
  customIcons: ['type', CustomIconLibrary],
  customIconPaletteProvider: ['type', CustomIconPaletteProvider],
  customIconRenderer: ['type', CustomIconRenderer],
};

export default CustomIcons;
