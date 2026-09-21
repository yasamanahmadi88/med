import * as replaceOptions from 'bpmn-js/lib/features/replace/ReplaceOptions';
import { BpmnElementAccessConfig, isBpmnTypeAllowed } from '../../services/bpmn-element-access.types';
import { isLegacyReadOnlyBpmnType } from '../../services/bpmn-legacy-read-only';

interface ReplaceOption {
  actionName?: string;
  target?: { type?: string };
}

interface PopupMenuLike {
  registerProvider(providerId: string, provider: unknown): void;
  registerProvider(providerId: string, priority: number, provider: unknown): void;
}

/**
 * Lowest-priority middleware for bpmn-replace.  The stock provider contributes the menu first;
 * this provider then removes destinations whose target type is not in the active owner's DB policy.
 */
export default class ElementAccessReplaceMenuFilter {
  static $inject = ['popupMenu', 'config.elementAccess'];

  private readonly actionTypes = this.buildActionTypeMap();
  private readonly nonElementActions = this.buildNonElementActionSet();

  constructor(
    popupMenu: PopupMenuLike,
    private readonly elementAccess?: BpmnElementAccessConfig,
  ) {
    const register = popupMenu.registerProvider as unknown as (...args: unknown[]) => void;
    if (register.length >= 3) {
      register.call(popupMenu, 'bpmn-replace', 1, this);
    } else {
      // Compatibility fallback for older API shapes.  bpmn-js 11/diagram-js 11 supports the
      // priority overload above; this branch keeps the module harmless if the library changes.
      register.call(popupMenu, 'bpmn-replace', this);
    }
  }

  getPopupMenuEntries(): (entries: Record<string, unknown>) => Record<string, unknown> {
    return entries => {
      const filtered: Record<string, unknown> = {};
      for (const [entryId, entry] of Object.entries(entries)) {
        const targetType = this.actionTypes.get(entryId);

        if (targetType) {
          if (!isLegacyReadOnlyBpmnType(targetType) && isBpmnTypeAllowed(this.elementAccess, targetType)) {
            filtered[entryId] = entry;
          }
          continue;
        }

        // Keep only stock target-less replace actions. In the installed bpmn-js
        // version these are sequence-flow mutations, not BPMN element replacements.
        if (this.nonElementActions.has(entryId)) {
          filtered[entryId] = entry;
        }
      }
      return filtered;
    };
  }

  // Legacy alias used by older ReplaceMenuProvider contracts.
  getEntries(): (entries: Record<string, unknown>) => Record<string, unknown> {
    return this.getPopupMenuEntries();
  }

  private buildActionTypeMap(): Map<string, string> {
    const result = new Map<string, string>();
    for (const value of Object.values(replaceOptions)) {
      if (!Array.isArray(value)) {
        continue;
      }
      for (const option of value as ReplaceOption[]) {
        if (option.actionName && option.target?.type) {
          result.set(option.actionName, option.target.type);
        }
      }
    }
    return result;
  }

  private buildNonElementActionSet(): Set<string> {
    const result = new Set<string>();
    const sequenceFlowOptions = replaceOptions.SEQUENCE_FLOW as ReplaceOption[];

    for (const option of sequenceFlowOptions) {
      if (option.actionName && !option.target?.type) {
        result.add(option.actionName);
      }
    }

    return result;
  }
}
