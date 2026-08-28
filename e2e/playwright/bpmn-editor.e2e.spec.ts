import { expect, test } from './support/medportal-fixtures';

/**
 * The BPMN editor is the one feature whose behaviour unit tests cannot reach: bpmn-js renders
 * through the SVG DOM, which jsdom does not implement, so its specs stub the modeler and cover
 * wiring only. These run against a real browser.
 *
 * The shared `page` fixture fails the test on any unexpected console error, page error or failed
 * request, so a moddle namespace that does not resolve or a renderer that throws surfaces here
 * rather than passing silently.
 */
test.describe('BPMN editor', () => {
  test('is behind the auth guard', async ({ page, mockApi }) => {
    await mockApi({ account: 'anonymous' });

    await page.goto('/bpmn-editor');

    await expect(page).toHaveURL(/\/login$/);
  });

  test('loads its lazy chunk and renders the bpmn-js canvas', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    await expect(page).toHaveURL(/\/bpmn-editor$/);
    await expect(page.locator('#designer-container')).toBeVisible();
    // bpmn-js builds this container and the SVG root itself; their presence means the modeler
    // constructed and attached without throwing.
    await expect(page.locator('.bpmn-canvas .bjs-container')).toBeVisible();
    await expect(page.locator('.bpmn-canvas .djs-container svg').first()).toBeVisible();
  });

  test('starts an empty diagram so the canvas has a root element', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // DesignerComponent calls createDiagram() when no xml is supplied; bpmn-js draws a start
    // event into the root layer for it.
    await expect(page.locator('.bpmn-canvas .djs-element')).not.toHaveCount(0);
  });

  test('renders the properties panel into the panel component', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // bpmn-js-properties-panel renders into the element PanelComponent registers.
    await expect(page.locator('.panel-content .bio-properties-panel')).toBeVisible();
  });

  test('shows the custom integration modules in the palette', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const palette = page.locator('.bpmn-canvas .djs-palette');
    await expect(palette).toBeVisible();

    // Entries EnhancementPaletteProvider adds on top of the stock bpmn-js palette.
    for (const className of [
      'merger-module',
      'fragmenter-module',
      'KafkaReceiver-module',
      'KafkaTransmitter-module',
      'HttpReceiver-module',
      'HttpTransmitter-module',
      'fileReceiver-module',
      'fileTransmitter-module',
      'dbReceiver-module',
      'dbTransmitter-module',
      'csvTransformer-module',
    ]) {
      await expect(palette.locator(`.${className}`), `palette entry .${className}`).toBeVisible();
    }

    // The stock entries must survive: enhancement mode adds to the palette rather than replacing it.
    await expect(palette.locator('.bpmn-icon-start-event-none')).toBeVisible();
  });

  test('places a custom module on the canvas', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const canvas = page.locator('.bpmn-canvas .djs-container');
    await expect(canvas).toBeVisible();
    const before = await page.locator('.bpmn-canvas .djs-element').count();

    // This is the end-to-end proof that the palette, the KafkaReceiver moddle extension, the
    // custom element factory and the renderer all agree: without any one of them this click
    // either throws on an unknown namespace or draws nothing.
    await page.locator('.djs-palette .KafkaReceiver-module').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);

    await expect(page.locator('.bpmn-canvas .djs-element')).not.toHaveCount(before);
  });

  test('exposes the toolbar actions', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    for (const title of ['Save', 'Export', 'Import', 'Undo', 'Redo']) {
      await expect(page.locator(`.toolbar button[title="${title}"]`), `toolbar ${title}`).toBeVisible();
    }
  });
});
