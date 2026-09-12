/**
 * diagram-js-minimap ships no type declarations.
 *
 * Only what this editor uses is declared: the module itself, so it can be registered alongside
 * the other didi modules, and `toggle()`, which the toolbar button calls. The minimap's own
 * toggle widget is hidden by `styles/designer.scss`, so that button is the only way in.
 */
declare module 'diagram-js-minimap' {
  import { ModuleDeclaration } from 'didi';

  /** Opens, closes, or flips the minimap; called with no argument it flips. */
  export interface Minimap {
    open(): void;
    close(): void;
    toggle(open?: boolean): void;
  }

  const MinimapModule: ModuleDeclaration;
  export default MinimapModule;
}
