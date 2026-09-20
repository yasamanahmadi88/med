import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

import { BpmnElementAccessService } from './bpmn-element-access.service';
import { BpmnOwnerElementAccess } from './bpmn-element-access.types';

describe('BpmnElementAccessService', () => {
  const resourceUrl = 'api/bpmn-element-access/current';
  const bpmnModelNs = 'http://www.omg.org/spec/BPMN/20100524/MODEL';

  const mediationAccess: BpmnOwnerElementAccess = {
    ownerCode: 'MEDIATION',
    ownerDisplayName: 'Mediation Portal',
    groups: [
      {
        id: 1000,
        code: 'FILE_PROCESSING',
        name: 'File Processing Elements',
      },
    ],
    elements: [
      {
        id: 1000,
        code: 'MERGER',
        bpmnType: 'Merger:Merger',
        namespaceUri: 'Merger',
        localName: 'merger',
        paletteAction: 'create.merger-module',
        displayName: 'Merger Module',
        sortOrder: 100,
      },
    ],
  };

  let service: BpmnElementAccessService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        BpmnElementAccessService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ApplicationConfigService,
          useValue: {
            getEndpointFor: () => resourceUrl,
          },
        },
      ],
    });

    service = TestBed.inject(BpmnElementAccessService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  function loadMediationAccess(): void {
    service.loadCurrent().subscribe();

    const request = httpTestingController.expectOne(resourceUrl);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys()).toEqual([]);

    request.flush(mediationAccess);
  }

  it('starts fail closed', () => {
    expect(service.currentAccess()).toBeNull();

    expect(service.currentConfig()).toEqual({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    expect(service.isTypeAllowed('Merger:Merger')).toBe(false);
  });

  it('loads active Owner access and derives bpmn-js access configuration', () => {
    let emittedAccess: BpmnOwnerElementAccess | undefined;

    service.loadCurrent().subscribe(access => {
      emittedAccess = access;
    });

    const request = httpTestingController.expectOne(resourceUrl);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys()).toEqual([]);

    request.flush(mediationAccess);

    expect(emittedAccess).toEqual(mediationAccess);
    expect(service.currentAccess()).toEqual(mediationAccess);

    expect(service.currentConfig()).toEqual({
      allowedTypes: ['Merger:Merger'],
      allowedPaletteActions: ['create.merger-module'],
      allowedXmlElements: [
        {
          namespaceUri: 'Merger',
          localName: 'merger',
        },
      ],
    });

    expect(service.isTypeAllowed('Merger:Merger')).toBe(true);
    expect(service.isTypeAllowed('KafkaReceiver:KafkaReceiver')).toBe(false);
  });

  it('clears stale Owner permissions before another lookup and stays fail closed when it fails', () => {
    loadMediationAccess();

    expect(service.isTypeAllowed('Merger:Merger')).toBe(true);

    let receivedError = false;

    service.loadCurrent().subscribe({
      error() {
        receivedError = true;
      },
    });

    /*
     * loadCurrent() clears the previous snapshot before starting
     * the request. A failed refresh must never retain stale access.
     */
    expect(service.currentAccess()).toBeNull();

    expect(service.currentConfig()).toEqual({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    expect(service.isTypeAllowed('Merger:Merger')).toBe(false);

    const request = httpTestingController.expectOne(resourceUrl);

    request.flush(
      { message: 'permission lookup failed' },
      {
        status: 500,
        statusText: 'Server Error',
      },
    );

    expect(receivedError).toBe(true);
    expect(service.currentAccess()).toBeNull();

    expect(service.currentConfig()).toEqual({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });
  });

  it('normalizes missing groups and elements to empty arrays', () => {
    const response = {
      ownerCode: 'MEDIATION',
      ownerDisplayName: 'Mediation Portal',
      groups: null,
      elements: null,
    } as unknown as BpmnOwnerElementAccess;

    service.loadCurrent().subscribe();

    const request = httpTestingController.expectOne(resourceUrl);
    request.flush(response);

    expect(service.currentAccess()).toEqual({
      ownerCode: 'MEDIATION',
      ownerDisplayName: 'Mediation Portal',
      groups: [],
      elements: [],
    });

    expect(service.currentConfig()).toEqual({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });
  });

  it('clear removes the current access snapshot', () => {
    loadMediationAccess();

    expect(service.currentAccess()).not.toBeNull();

    service.clear();

    expect(service.currentAccess()).toBeNull();

    expect(service.currentConfig()).toEqual({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    expect(service.isTypeAllowed('Merger:Merger')).toBe(false);
  });

  it('accepts an allowed XML element regardless of its namespace prefix', () => {
    loadMediationAccess();

    const xml = `
      <bpmn:definitions
        xmlns:bpmn="${bpmnModelNs}"
        xmlns:customMerger="Merger">
        <bpmn:process id="Process_1">
          <customMerger:merger id="Merger_1" />
        </bpmn:process>
      </bpmn:definitions>
    `;

    expect(service.findDisallowedXmlElements(xml)).toEqual([]);
  });

  it('ignores structural BPMN children and descendants inside extensionElements', () => {
    loadMediationAccess();

    const xml = `
      <bpmn:definitions
        xmlns:bpmn="${bpmnModelNs}"
        xmlns:ext="urn:test-extension">
        <bpmn:process id="Process_1">
          <bpmn:documentation>documentation</bpmn:documentation>
          <bpmn:laneSet id="LaneSet_1" />
          <bpmn:sequenceFlow
            id="Flow_1"
            sourceRef="Source_1"
            targetRef="Target_1" />
          <bpmn:extensionElements>
            <ext:metadata />
          </bpmn:extensionElements>
        </bpmn:process>
      </bpmn:definitions>
    `;

    expect(service.findDisallowedXmlElements(xml)).toEqual([]);
  });

  it('treats a collaboration participant as an access-controlled BPMN element', () => {
    loadMediationAccess();

    const xml = `
      <bpmn:definitions xmlns:bpmn="${bpmnModelNs}">
        <bpmn:collaboration id="Collaboration_1">
          <bpmn:participant id="Participant_1" />
        </bpmn:collaboration>
      </bpmn:definitions>
    `;

    expect(service.findDisallowedXmlElements(xml)).toEqual([
      {
        namespaceUri: bpmnModelNs,
        localName: 'participant',
      },
    ]);
  });

  it('rejects malformed XML', () => {
    const xml = `
      <bpmn:definitions xmlns:bpmn="${bpmnModelNs}">
        <bpmn:process id="Process_1">
      </bpmn:definitions>
    `;

    expect(() => service.findDisallowedXmlElements(xml)).toThrow('Invalid or unsafe BPMN XML');
  });

  it('rejects XML containing a DOCTYPE declaration', () => {
    const xml = `
      <!DOCTYPE definitions>
      <definitions xmlns="${bpmnModelNs}" />
    `;

    expect(() => service.findDisallowedXmlElements(xml)).toThrow('Invalid or unsafe BPMN XML');
  });

  it('fails closed for a BPMN element that is not allowed for the active Owner', () => {
    loadMediationAccess();

    const xml = `
      <bpmn:definitions xmlns:bpmn="${bpmnModelNs}">
        <bpmn:process id="Process_1">
          <bpmn:startEvent id="StartEvent_1" />
        </bpmn:process>
      </bpmn:definitions>
    `;

    expect(service.findDisallowedXmlElements(xml)).toEqual([
      {
        namespaceUri: bpmnModelNs,
        localName: 'startEvent',
      },
    ]);
  });
});
