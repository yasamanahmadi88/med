import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import {
  BpmnElementAccessConfig,
  BpmnProductElementAccess,
  BpmnXmlElementIdentity,
  EMPTY_BPMN_ELEMENT_ACCESS_CONFIG,
  isBpmnTypeAllowed,
  toBpmnElementAccessConfig,
} from './bpmn-element-access.types';

const BPMN_MODEL_NS = 'http://www.omg.org/spec/BPMN/20100524/MODEL';
const STRUCTURAL_FLOW_CHILDREN = new Set(['sequenceFlow', 'laneSet', 'documentation', 'extensionElements']);

/**
 * Product-scoped BPMN element policy.
 *
 * Access is loaded once before the editor is constructed.  The snapshot then feeds both Angular
 * components and bpmn-js modules.  There is intentionally no "show all on error" fallback: a
 * failed/missing permission lookup must not expose elements the product was never assigned.
 */
@Injectable({ providedIn: 'root' })
export class BpmnElementAccessService {
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/bpmn-element-access/products');
  private access: BpmnProductElementAccess | null = null;
  private config: BpmnElementAccessConfig = EMPTY_BPMN_ELEMENT_ACCESS_CONFIG;

  constructor(
    private readonly http: HttpClient,
    private readonly applicationConfigService: ApplicationConfigService,
  ) {}

  loadForProduct(productId: number): Observable<BpmnProductElementAccess> {
    // Fail closed while permissions for another product are being resolved.
    // A failed lookup must never leave the previous product's permissions active.
    this.clear();

    return this.http.get<BpmnProductElementAccess>(`${this.resourceUrl}/${productId}`).pipe(
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
    this.access = null;
    this.config = EMPTY_BPMN_ELEMENT_ACCESS_CONFIG;
  }

  currentAccess(): BpmnProductElementAccess | null {
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
   */
  findDisallowedXmlElements(xml: string): BpmnXmlElementIdentity[] {
    const parser = new DOMParser();
    const document = parser.parseFromString(xml, 'application/xml');

    if (document.querySelector('parsererror') || document.doctype) {
      throw new Error('Invalid or unsafe BPMN XML');
    }

    const allowed = new Set(this.config.allowedXmlElements.map(element => this.xmlKey(element.namespaceUri, element.localName)));
    const disallowed = new Map<string, BpmnXmlElementIdentity>();

    const visit = (parent: Element): void => {
      for (const child of Array.from(parent.children)) {
        if (this.isAccessControlledChild(parent, child)) {
          const identity = { namespaceUri: child.namespaceURI ?? '', localName: child.localName };
          const key = this.xmlKey(identity.namespaceUri, identity.localName);
          if (!allowed.has(key)) {
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
    if (
      parent.namespaceURI === BPMN_MODEL_NS &&
      (parent.localName === 'process' || parent.localName === 'subProcess')
    ) {
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
