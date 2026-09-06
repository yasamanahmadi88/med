import { SHORTCUT_GROUPS } from './shortcuts';

/**
 * The shortcut dialog is a promise to the user. These specs do not re-check the bindings against
 * bpmn-js — that was done by reading the modules the Modeler registers and by pressing the keys
 * in the Playwright suite — but they do keep the list from rotting into duplicates or blanks.
 */
describe('keyboard shortcuts', () => {
  const all = SHORTCUT_GROUPS.flatMap(group => group.shortcuts);

  it('groups the shortcuts so the dialog is scannable', () => {
    expect(SHORTCUT_GROUPS.map(group => group.title)).toEqual(['Editing', 'Tools', 'View']);
  });

  it('gives every row both an action and its keys', () => {
    for (const shortcut of all) {
      expect(shortcut.action.trim(), JSON.stringify(shortcut)).not.toBe('');
      expect(shortcut.keys.trim(), JSON.stringify(shortcut)).not.toBe('');
    }
  });

  it('lists each action once', () => {
    const actions = all.map(shortcut => shortcut.action);

    expect(new Set(actions).size).toBe(actions.length);
  });

  it('covers the bindings the Vue list left out', () => {
    // Delete, copy/paste, find, the connect tool and the arrow keys are all registered by
    // bpmn-js's default modules and were missing from the dialog the Vue toolbar showed.
    const actions = all.map(shortcut => shortcut.action);

    for (const action of ['Delete selection', 'Copy', 'Paste', 'Find element', 'Connect tool', 'Move the selection', 'Pan the canvas']) {
      expect(actions, action).toContain(action);
    }
  });

  it('names the modifier in a way both platforms recognise', () => {
    // The bindings go through `keyboard.isCmd`, which accepts Ctrl and the Mac command key
    // alike; naming only one of them would be wrong for half the users.
    const modified = all.filter(shortcut => shortcut.keys.includes('Ctrl') || shortcut.keys.includes('⌘'));

    expect(modified.length).toBeGreaterThan(0);
    for (const shortcut of modified) {
      expect(shortcut.keys, shortcut.action).toContain('Ctrl/⌘');
    }
  });
});
