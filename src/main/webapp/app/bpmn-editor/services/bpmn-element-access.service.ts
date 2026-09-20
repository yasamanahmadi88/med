import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import {
  BpmnElementAccessConfig,
  BpmnOwnerElementAccess,
  BpmnXmlElementIdentity,
  EMPTY_BPMN_ELEMENT_ACCESS_CONFIG,
  isBpmnTypeAllowed,
  toBpmnElementAccessConfig,
} from './bpmn-element-access.types';

const BPMN_MODEL_NS = 'http://www.omg.org/spec/BPMN/20100524/MODEL';
const STRUCTURAL_FLOW_CHILDREN = new Set(['sequenceFlow', 'laneSet', 'documentation', 'extensionElements']);

/**
 * Portal-Owner-scoped BPMN element policy.
 *
 * Access is loaded once before the editor is constructed.  The snapshot then feeds both Angular
 * components and bpmn-js modules.  There is intentionally no "show all on error" fallback: a
 * failed/missing permission lookup must never expose elements that are not assigned to the active portal Owner.
 */
@Injectable({ providedIn: 'root' })
export class BpmnElementAccessService {
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/bpmn-element-access/current');
  private persistedInstances = new Map<string, string>();
  private access: BpmnOwnerElementAccess | null = null;
  private config: BpmnElementAccessConfig = EMPTY_BPMN_ELEMENT_ACCESS_CONFIG;

  constructor(
    private readonly http: HttpClient,
    private readonly applicationConfigService: ApplicationConfigService,
  ) {}

  loadCurrent(): Observable<BpmnOwnerElementAccess> {
    // Fail closed while the active Owner policy is being resolved.
    // A failed lookup must never leave a stale Owner policy active.
    this.clear();

    return this.http.get<BpmnOwnerElementAccess>(this.resourceUrl).pipe(
      tap(access => {
        this.access = {
          ...access,
          groups: access.groups ?? [],
          elements: access.elements ?? [],
        };
        this.config = toBpmnElementAccessConfig(this.access);
      }),
    );
  }

  clear(): void {
    this.persistedInstances.clear();
    this.access = null;
    this.config = EMPTY_BPMN_ELEMENT_ACCESS_CONFIG;
  }

  setPersistedDiagram(xml: string | null | undefined): void {
    this.persistedInstances.clear();
    if (xml?.trim()) {
      const document = new DOMParser().parseFromString(xml, 'application/xml');
      if (document.querySelector('parsererror') || document.doctype) throw new Error('Invalid BPMN XML');
      for (const element of Array.from(document.getElementsByTagName('*'))) {
        const id = element.getAttribute('id');
        if (id) this.persistedInstances.set(id, this.xmlKey(element.namespaceURI ?? '', element.localName));
      }
    }
  }

  currentAccess(): BpmnOwnerElementAccess | null {
    return this.access;
  }

  currentConfig(): BpmnElementAccessConfig {
    return this.config;
  }

  isTypeAllowed(type: string | null | undefined): boolean {
    return isBpmnTypeAllowed(this.config, type);
  }

  /**
   * Browser-side import/load guard.  Server validation remains authoritative.
   *
   * Namespace URI + local name are used instead of XML prefixes because a caller can rename a
   * prefix without changing the XML element's meaning.
   *
   * For MEDIATION owner, allows legacy CDR and CSV elements if they existed in the persisted XML.
   */
  findDisallowedXmlElements(xml: string): BpmnXmlElementIdentity[] {
    const parser = new DOMParser();
    const document = parser.parseFromString(xml, 'application/xml');

    if (document.querySelector('parsererror') || document.doctype) {
      throw new Error('Invalid or unsafe BPMN XML');
    }

    const allowed = new Set(this.config.allowedXmlElements.map(element => this.xmlKey(element.namespaceUri, element.localName)));
    const ids = new Set<string>();
    for (const element of Array.from(document.getElementsByTagName('*'))) {
      const id = element.getAttribute('id');
      if (id && ids.has(id)) throw new Error('Duplicate BPMN id');
      if (id) ids.add(id);
    }
    const disallowed = new Map<string, BpmnXmlElementIdentity>();

    const visit = (parent: Element): void => {
      for (const child of Array.from(parent.children)) {
        if (this.isAccessControlledChild(parent, child)) {
          const identity = { namespaceUri: child.namespaceURI ?? '', localName: child.localName };
          const key = this.xmlKey(identity.namespaceUri, identity.localName);
          const id = child.getAttribute('id');
          const legacy = this.access?.ownerCode === 'MEDIATION' &&
            ((identity.namespaceUri === 'CdrParser' && identity.localName === 'cdrParser') ||
             (identity.namespaceUri === 'CsvTransformer' && identity.localName === 'csvTransformer')) &&
            !!id && this.persistedInstances.get(id) === key;
          if (!allowed.has(key) && !legacy) {
            disallowed.set(key, identity);
          }
        }
        visit(child);
      }
    };

    if (document.documentElement) {
      visit(document.documentElement);
    }
    return [...disallowed.values()];
  }

  private isAccessControlledChild(parent: Element, child: Element): boolean {
    if (parent.namespaceURI === BPMN_MODEL_NS && (parent.localName === 'process' || parent.localName === 'subProcess')) {
      return !(child.namespaceURI === BPMN_MODEL_NS && STRUCTURAL_FLOW_CHILDREN.has(child.localName));
    }

    return (
      parent.namespaceURI === BPMN_MODEL_NS &&
      parent.localName === 'collaboration' &&
      child.namespaceURI === BPMN_MODEL_NS &&
      child.localName === 'participant'
    );
  }

  private xmlKey(namespaceUri: string, localName: string): string {
    return `${namespaceUri}\u0000${localName}`;
  }
}
