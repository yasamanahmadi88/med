import { BpmnElementAccessConfig, isBpmnTypeAllowed } from '../../services/bpmn-element-access.types';
import { isLegacyReadOnlyPaletteAction } from '../../services/bpmn-legacy-read-only';
import { CUSTOM_TASK_TYPE } from '../../custom-icons/icon-library';

interface PaletteLike {
  registerProvider(priority: number, provider: unknown): void;
}

const GENERIC_TOOL_IDS = new Set(['hand-tool', 'lasso-tool', 'space-tool', 'global-connect-tool', 'tool-separator']);

/**
 * Final palette middleware.
 *
 * The visible palette is fail-closed:
 * - exactly the Vue generic tools survive unconditionally;
 * - BPMN creation entries survive only when the active-owner policy allows them;
 * - unknown stock helpers do not leak into the UI after a bpmn-js upgrade.
 */
export default class ElementAccessPaletteFilter {
  static $inject = ['palette', 'config.elementAccess'];

  constructor(
    palette: PaletteLike,
    private readonly elementAccess?: BpmnElementAccessConfig,
  ) {
    palette.registerProvider(1, this);
  }

  getPaletteEntries(): (entries: Record<string, unknown>) => Record<string, unknown> {
    return entries => {
      const allowedActions = new Set(this.elementAccess?.allowedPaletteActions ?? []);

      const filtered: Record<string, unknown> = {};

      for (const [entryId, entry] of Object.entries(entries)) {
        if (GENERIC_TOOL_IDS.has(entryId)) {
          filtered[entryId] = entry;
          continue;
        }

        // Recognition is intentionally separate from creation: keeping descriptors/renderers
        // allows old flows to load, while these two actions never reappear through stale DB data.
        if (isLegacyReadOnlyPaletteAction(entryId)) {
          continue;
        }

        if (entryId.startsWith('create.custom-icon-') && isBpmnTypeAllowed(this.elementAccess, CUSTOM_TASK_TYPE)) {
          filtered[entryId] = entry;
          continue;
        }

        if (entryId.startsWith('create.') && allowedActions.has(entryId)) {
          filtered[entryId] = entry;
        }
      }

      return filtered;
    };
  }
}
