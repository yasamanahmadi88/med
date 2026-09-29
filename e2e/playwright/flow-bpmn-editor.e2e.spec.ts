import { expect, test } from './support/medportal-fixtures';
import type { Page } from '@playwright/test';

/**
 * The Flow form drives the editor through one URL — `/bpmn-editor?flowId=n` — and expects the
 * diagram to come back as a PUT on that flow. That round trip crosses the router, the lazy chunk,
 * bpmn-js's own XML serialisation and FlowService, so only a real browser can prove it; the unit
 * specs have to stub the modeler out.
 *
 * The specs enter that URL directly rather than clicking the button on the flow list, because a
 * rendered list row currently throws in FaIconComponent (`jhiSortBy` assigns over the icon signal
 * input) and the shared `page` fixture rightly fails on console errors. That the list navigates
 * here with the right query param is covered in flow.component.spec.ts instead.
 *
 * The same fixture fails the test on any unexpected console error, page error or failed request,
 * so a diagram that does not import surfaces here rather than passing silently.
 */

const seededDiagram = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  id="Definitions_seeded" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="SeededProcess" isExecutable="true">
    <bpmn:task id="SeededTask" name="Seeded task" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="SeededProcess">
      <bpmndi:BPMNShape id="SeededTask_di" bpmnElement="SeededTask">
        <dc:Bounds x="160" y="80" width="100" height="80" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

const flow = { id: 7, flowName: 'seeded flow', flowDesc: 'seeded', flow: seededDiagram, product: null };

/**
 * A flow from before the palette was narrowed to the file modules. Kafka, HTTP and DB modules can
 * no longer be added, but flows that already carry one have to keep working — which is what the
 * tests below check end to end: it imports, it draws, its properties show, and an edit saves.
 */
const legacyDiagram = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                  xmlns:KafkaReceiver="KafkaReceiver"
                  xmlns:DbTransmitter="DbTransmitter"
                  id="Definitions_legacy" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="LegacyProcess" isExecutable="true">
    <KafkaReceiver:kafkaReceiver id="LegacyKafka" name="orders in" camunda:commentDesc="from the old flow" />
    <DbTransmitter:dbTransmitter id="LegacyDb" name="rows out" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_2">
    <bpmndi:BPMNPlane id="BPMNPlane_2" bpmnElement="LegacyProcess">
      <bpmndi:BPMNShape id="LegacyKafka_di" bpmnElement="LegacyKafka">
        <dc:Bounds x="160" y="80" width="120" height="120" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="LegacyDb_di" bpmnElement="LegacyDb">
        <dc:Bounds x="360" y="80" width="120" height="120" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

const legacyFlow = { id: 8, flowName: 'legacy flow', flowDesc: 'legacy', flow: legacyDiagram, product: null };

/** One flow behind its id. The shared mock answers /api/flows itself, with an empty list. */
async function mockFlow(page: Page): Promise<void> {
  await page.route('**/api/flows/7', route =>
    route.fulfill({ status: 200, headers: { 'content-type': 'application/json' }, body: JSON.stringify(flow) }),
  );
  await page.route('**/api/flows/8', route =>
    route.fulfill({ status: 200, headers: { 'content-type': 'application/json' }, body: JSON.stringify(legacyFlow) }),
  );
}

/**
 * Reach the editor the way the flow list does. The stop on /flow first is what save and cancel
 * come back to, so it also stands in for the page the user left.
 */
async function openEditorOnFlow(page: Page, flowId = 7): Promise<void> {
  await page.goto('/flow');
  await page.goto(`/bpmn-editor?flowId=${flowId}`);
  await expect(page.locator('#designer-container')).toBeVisible();
}

test.describe('Flow BPMN editor', () => {
  test('seeds the diagram from the flow it was opened on', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await mockFlow(page);

    await openEditorOnFlow(page);

    // bpmn-js labels each rendered shape with the id from the source XML, so finding this one
    // means the flow's own diagram was imported rather than a blank canvas created.
    await expect(page.locator('.bpmn-canvas .djs-element[data-element-id="SeededTask"]')).toBeVisible();
  });

  test('saves the diagram back onto the flow and returns to where it came from', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await mockFlow(page);

    await openEditorOnFlow(page);
    await expect(page.locator('.bpmn-canvas .djs-element[data-element-id="SeededTask"]')).toBeVisible();

    const update = page.waitForRequest(request => request.method() === 'PUT' && request.url().includes('/api/flows/7'));
    await page.getByTestId('bpmnSave').click();

    // The whole flow goes back, with what bpmn-js serialised in place of the diagram it was given.
    const body = (await update).postDataJSON() as { id: number; flowName: string; flow: string };
    expect(body.id).toBe(7);
    expect(body.flowName).toBe('seeded flow');
    expect(body.flow).toContain('SeededTask');

    await expect(page).toHaveURL(/\/flow$/);
  });

  test('cancel returns without touching the flow', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await mockFlow(page);

    const writes: string[] = [];
    page.on('request', request => {
      if (request.method() !== 'GET' && request.url().includes('/api/flows')) {
        writes.push(`${request.method()} ${request.url()}`);
      }
    });

    await openEditorOnFlow(page);
    await expect(page.locator('.bpmn-canvas .djs-element[data-element-id="SeededTask"]')).toBeVisible();

    await page.getByTestId('bpmnCancel').click();

    await expect(page).toHaveURL(/\/flow$/);
    expect(writes, 'cancel must not persist anything').toEqual([]);
  });

  test('opens a flow built from modules the palette no longer offers', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await mockFlow(page);

    await openEditorOnFlow(page, 8);

    // Dropping a moddle extension or a renderer along with the palette entry would show up here:
    // the import would fail — which the fixture turns into a console error and a failed test —
    // or the shapes would simply not be drawn.
    await expect(page.locator('.bpmn-canvas .djs-element[data-element-id="LegacyKafka"]')).toBeVisible();
    await expect(page.locator('.bpmn-canvas .djs-element[data-element-id="LegacyDb"]')).toBeVisible();

    // And the palette really is the narrowed one, so this is a diagram carrying modules that
    // cannot be added to it.
    await expect(page.locator('.djs-palette [data-action="create.KafkaReceiver-module"]')).toHaveCount(0);
    await expect(page.locator('.djs-palette [data-action="create.fileReceiver-module"]')).toHaveCount(1);
  });

  test('shows and edits the properties of a module the palette no longer offers', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await mockFlow(page);

    await openEditorOnFlow(page, 8);
    await page.locator('.bpmn-canvas .djs-element[data-element-id="LegacyKafka"]').click();

    const panel = page.getByTestId('bpmnModulePanel');
    await expect(panel).toBeVisible();
    await expect(panel.locator('.panel-header p')).toHaveText('KafkaReceiver');

    // The stored value is read back, which is what "can be viewed" means for a module whose
    // schema had to survive the palette change.
    const comment = panel.locator('.selection-area[data-field="commentDesc"] input');
    await expect(comment).toHaveValue('from the old flow');

    await comment.fill('edited after the palette change');

    const update = page.waitForRequest(request => request.method() === 'PUT' && request.url().includes('/api/flows/8'));
    await page.getByTestId('bpmnSave').click();

    const body = (await update).postDataJSON() as { flow: string };
    expect(body.flow).toContain('edited after the palette change');
    // The module itself has to come back out of the serialiser too, not just the edited property.
    expect(body.flow).toContain('LegacyKafka');
    expect(body.flow).toContain('LegacyDb');
  });
});
