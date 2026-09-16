import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

import { BpmnElementAccessService } from './bpmn-element-access.service';
import { BpmnProductElementAccess } from './bpmn-element-access.types';

describe('BpmnElementAccessService', () => {
  const resourceUrl = 'api/bpmn-element-access/products';
  const bpmnModelNs = 'http://www.omg.org/spec/BPMN/20100524/MODEL';

  const mciAccess: BpmnProductElementAccess = {
    productId: 7,
    productName: 'MCI',
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

  function loadMciAccess(): void {
    service.loadForProduct(7).subscribe();

    const request = httpTestingController.expectOne(`${resourceUrl}/7`);
    expect(request.request.method).toBe('GET');

    request.flush(mciAccess);
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

  it('loads product access and derives bpmn-js access configuration', () => {
    let emittedAccess: BpmnProductElementAccess | undefined;

    service.loadForProduct(7).subscribe(access => {
      emittedAccess = access;
    });

    const request = httpTestingController.expectOne(`${resourceUrl}/7`);

    expect(request.request.method).toBe('GET');

    request.flush(mciAccess);

    expect(emittedAccess).toEqual(mciAccess);
    expect(service.currentAccess()).toEqual(mciAccess);

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

  it('clears stale permissions before another product lookup and stays fail closed when it fails', () => {
    service.loadForProduct(7).subscribe();

    const firstRequest = httpTestingController.expectOne(`${resourceUrl}/7`);
    firstRequest.flush(mciAccess);

    expect(service.isTypeAllowed('Merger:Merger')).toBe(true);

    let receivedError = false;

    service.loadForProduct(8).subscribe({
      error: () => {
        receivedError = true;
      },
    });

    expect(service.currentAccess()).toBeNull();

    expect(service.currentConfig()).toEqual({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    expect(service.isTypeAllowed('Merger:Merger')).toBe(false);

    const secondRequest = httpTestingController.expectOne(`${resourceUrl}/8`);

    secondRequest.flush(
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

    expect(service.isTypeAllowed('Merger:Merger')).toBe(false);
  });

  it('normalizes missing groups and elements to empty arrays', () => {
    const response = {
      productId: 7,
      productName: 'MCI',
      groups: null,
      elements: null,
    } as unknown as BpmnProductElementAccess;

    service.loadForProduct(7).subscribe();

    const request = httpTestingController.expectOne(`${resourceUrl}/7`);
    request.flush(response);

    expect(service.currentAccess()).toEqual({
      productId: 7,
      productName: 'MCI',
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
    service.loadForProduct(7).subscribe();

    const request = httpTestingController.expectOne(`${resourceUrl}/7`);
    request.flush(mciAccess);

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
    loadMciAccess();

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
    loadMciAccess();

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
    loadMciAccess();

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

    expect(() => service.findDisallowedXmlElements(xml)).toThrow(
      'Invalid or unsafe BPMN XML',
    );
  });

  it('rejects XML containing a DOCTYPE declaration', () => {
    const xml = `
      <!DOCTYPE definitions>
      <definitions xmlns="${bpmnModelNs}" />
    `;

    expect(() => service.findDisallowedXmlElements(xml)).toThrow(
      'Invalid or unsafe BPMN XML',
    );
  });

  it('fails closed for a BPMN element that is not allowed for the product', () => {
    loadMciAccess();

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
