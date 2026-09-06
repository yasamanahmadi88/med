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

    // DesignerComponent seeds an empty diagram when no xml is supplied, and that diagram opens
    // with a start event — a process without one is not executable, and a blank canvas gives
    // the user nothing to drag from.
    await expect(page.locator('.bpmn-canvas .djs-element')).not.toHaveCount(0);
  });

  test('right-clicking an element offers the types it can become', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // The seeded start event is a replaceable element, so ContextMenuProvider hands it to the
    // stock bpmn-replace popup rather than to our create menu.
    const startEvent = page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first();
    await startEvent.click({ button: 'right' });

    await expect(page.locator('.djs-popup [data-id="replace-with-message-start"]')).toBeVisible();
    await expect(page.locator('.bpmn-context-menu')).toHaveCount(0);
  });

  test('right-clicking bare canvas offers elements to create', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // The canvas resolves to the Process, which has no type to swap — bpmn-replace is empty for
    // it, so this is the case our own menu exists to cover.
    await page
      .locator('.bpmn-canvas .djs-container svg')
      .first()
      .click({ button: 'right', position: { x: 420, y: 320 } });

    const menu = page.locator('.bpmn-context-menu');
    await expect(menu).toBeVisible();
    await expect(menu.locator('.context-menu_header')).toHaveText('Create Element');
    await expect(menu.locator('.context-menu_item')).not.toHaveCount(0);
  });

  test('picking from the create menu places that element', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');
    const before = await page.locator('.bpmn-canvas .djs-element').count();

    await page
      .locator('.bpmn-canvas .djs-container svg')
      .first()
      .click({ button: 'right', position: { x: 420, y: 320 } });

    // The chosen shape attaches to the cursor, exactly as dragging from the palette does, so a
    // second click is what commits it to the canvas.
    await page.locator('.bpmn-context-menu .context-menu_item', { hasText: 'Task' }).first().click();
    await page.locator('.bpmn-canvas .djs-container').click({ position: { x: 500, y: 380 } });

    await expect(page.locator('.bpmn-canvas .djs-element')).not.toHaveCount(before);
  });

  test('the create menu closes on a click elsewhere', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    await page
      .locator('.bpmn-canvas .djs-container svg')
      .first()
      .click({ button: 'right', position: { x: 420, y: 320 } });
    await expect(page.locator('.bpmn-context-menu')).toBeVisible();

    await page.locator('.bpmn-canvas').click({ position: { x: 200, y: 200 } });

    await expect(page.locator('.bpmn-context-menu')).toHaveCount(0);
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
