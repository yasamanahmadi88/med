import ContextMenuProvider from './ContextMenuProvider';

/**
 * didi module giving the editor its right-click behaviour. Added to the modeler alongside the
 * palette and renderer modules; see `designer.component.ts`.
 */
export default {
  __init__: ['contextMenuProvider'],
  contextMenuProvider: ['type', ContextMenuProvider],
};

export { ContextMenuProvider };
export { isAppendAction } from './ContextMenuProvider';
export * from './append-options';
