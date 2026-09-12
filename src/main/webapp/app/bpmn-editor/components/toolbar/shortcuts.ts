/**
 * The keyboard shortcuts this editor actually responds to.
 *
 * The Vue toolbar hard-coded a shorter list that had drifted from the library: it claimed the
 * replace tool (`R`) and append/create anything (`A`/`N`) appear only when `templateChooser` is
 * on, when bpmn-js registers all three unconditionally, and it left out delete, copy/paste,
 * find, the connect tool and every arrow-key binding.
 *
 * Every row below was read off the modules `bpmn-js/lib/Modeler` registers by default —
 * `KeyboardBindings` and `BpmnKeyboardBindings` for the editing and tool keys,
 * `CreateAppendKeyboardBindings` for `A`/`N`, `KeyboardMoveSelection` for the plain arrows and
 * `KeyboardMove` for the modified ones — and confirmed against a running editor. A shortcut
 * list that lies is worse than none, so if a binding changes, this file changes with it.
 */
export interface Shortcut {
  readonly action: string;
  readonly keys: string;
}

export interface ShortcutGroup {
  readonly title: string;
  readonly shortcuts: readonly Shortcut[];
}

/** `Ctrl` on Windows and Linux, `⌘` on a Mac — bpmn-js accepts whichever the platform uses. */
const CMD = 'Ctrl/⌘';

export const SHORTCUT_GROUPS: readonly ShortcutGroup[] = [
  {
    title: 'Editing',
    shortcuts: [
      { action: 'Undo', keys: `${CMD} + Z` },
      { action: 'Redo', keys: `${CMD} + Shift + Z, ${CMD} + Y` },
      { action: 'Copy', keys: `${CMD} + C` },
      { action: 'Paste', keys: `${CMD} + V` },
      { action: 'Select all', keys: `${CMD} + A` },
      // Bound, but only once the element's label editor has been left — placing an element opens
      // it, and the keystroke then belongs to the text. Escape closes it.
      { action: 'Delete selection', keys: 'Delete, Backspace' },
      { action: 'Edit label', keys: 'E' },
      { action: 'Leave label editing', keys: 'Escape' },
      { action: 'Find element', keys: `${CMD} + F` },
    ],
  },
  {
    title: 'Tools',
    shortcuts: [
      { action: 'Hand tool', keys: 'H' },
      { action: 'Lasso tool', keys: 'L' },
      { action: 'Space tool', keys: 'S' },
      { action: 'Connect tool', keys: 'C' },
      { action: 'Replace element', keys: 'R' },
      { action: 'Append element', keys: 'A' },
      { action: 'Create element', keys: 'N' },
    ],
  },
  {
    title: 'View',
    shortcuts: [
      { action: 'Zoom in', keys: `${CMD} + +` },
      { action: 'Zoom out', keys: `${CMD} + -` },
      { action: 'Reset zoom', keys: `${CMD} + 0` },
      { action: 'Zoom', keys: `${CMD} + mouse wheel` },
      { action: 'Scroll vertically', keys: 'Mouse wheel' },
      { action: 'Scroll horizontally', keys: 'Shift + mouse wheel' },
      { action: 'Move the selection', keys: 'Arrow keys (Shift for a larger step)' },
      { action: 'Pan the canvas', keys: `${CMD} + arrow keys` },
    ],
  },
];
