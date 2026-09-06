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

  test('the Delete key removes an element once its label editor is closed', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // Placing an element opens its label editor and puts the caret inside it, so Delete belongs
    // to the text until Escape closes it. Worth pinning, because the sequence looks like the key
    // being unbound — it is not — and the shortcut dialog tells users about the Escape.
    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .bpmn-icon-task').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);

    const withTask = await page.locator('.bpmn-canvas .djs-element').count();
    await page.keyboard.press('Delete');
    await expect(page.locator('.bpmn-canvas .djs-element')).toHaveCount(withTask);

    await page.keyboard.press('Escape');
    await page.keyboard.press('Delete');

    await expect(page.locator('.bpmn-canvas .djs-element')).toHaveCount(withTask - 1);
  });

  test('the Delete key cannot remove the start event either', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // The context pad hides its trash button, but the keyboard goes straight to the editor
    // action — so this is the path that would bypass CustomRules if the rule were only cosmetic.
    const before = await page.locator('.bpmn-canvas .djs-element').count();
    await page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first().click();
    await page.keyboard.press('Delete');

    await expect(page.locator('.bpmn-canvas .djs-element')).toHaveCount(before);
  });

  test('offers no delete on the start event', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // bpmn-js builds the context pad by asking the rules what is allowed, so the missing trash
    // button *is* CustomRules refusing. A process without a start event is one no engine will
    // run, and nothing in the editor says it has gone.
    await page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first().click();

    await expect(page.locator('.djs-context-pad .entry')).not.toHaveCount(0);
    await expect(page.locator('.djs-context-pad .bpmn-icon-trash')).toHaveCount(0);
  });

  test('still deletes an ordinary element', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // The rule must protect two element types, not make the canvas read-only.
    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .bpmn-icon-task').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);

    const withTask = await page.locator('.bpmn-canvas .djs-element').count();
    await page.locator('.djs-context-pad .bpmn-icon-trash').click();

    await expect(page.locator('.bpmn-canvas .djs-element')).toHaveCount(withTask - 1);
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

    for (const title of ['Save', 'Export', 'Import', 'Cancel', 'Undo', 'Redo', 'Restart', 'Zoom out', 'Zoom in', 'Preview as XML']) {
      await expect(page.locator(`.toolbar button[title="${title}"]`), `toolbar ${title}`).toBeVisible();
    }
  });

  test('draws an icon in every toolbar button', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // This project ships FontAwesome as SVG components and loads no webfont stylesheet, so the
    // `<i class="fas fa-save">` markup the toolbar used before drew nothing at all. Counting the
    // rendered SVGs is what tells the two apart — the buttons look fine either way in a DOM dump.
    const buttons = page.locator('.toolbar button');
    const iconButtons = page.locator('.toolbar button svg.svg-inline--fa');

    // Every button but the zoom percentage carries an icon.
    await expect(iconButtons).toHaveCount((await buttons.count()) - 1);
  });

  test('zooms the canvas and reports the scale', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const level = page.getByTestId('bpmnZoomFit');
    await expect(level).toHaveText('100%');

    await page.getByTestId('bpmnZoomIn').click();
    await expect(level).toHaveText('110%');

    // One assertion per click: two in a row are delivered faster than the canvas redraws, and
    // the second then lands on a button Angular is mid-render on and is lost.
    await page.getByTestId('bpmnZoomOut').click();
    await expect(level).toHaveText('100%');
    await page.getByTestId('bpmnZoomOut').click();
    await expect(level).toHaveText('90%');

    // Fit-to-viewport lands on an arbitrary scale, and the label has to follow a zoom the
    // buttons did not cause.
    await level.click();
    await expect(level).not.toHaveText('90%');
  });

  test('previews the XML the editor would save', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    await page.getByTestId('bpmnPreviewXml').click();

    const preview = page.getByTestId('bpmnXmlPreview');
    await expect(preview).toBeVisible();
    // The real serialisation, not a placeholder: the seeded start event has to be in it.
    await expect(preview).toContainText('<bpmn:startEvent');
    await expect(preview).toContainText('bpmn:definitions');

    await page.locator('.modal-footer .btn-secondary').click();
    await expect(preview).toHaveCount(0);
  });

  test('restarts onto a fresh diagram', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .bpmn-icon-task').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);
    await page.keyboard.press('Escape');
    const withTask = await page.locator('.bpmn-canvas .djs-element').count();

    await page.getByTestId('bpmnRestart').click();

    // Back to the seeded diagram: the start event and nothing else.
    await expect(page.locator('.bpmn-canvas .djs-element')).toHaveCount(withTask - 1);
    await expect(page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]')).toHaveCount(1);
  });

  test('lists the keyboard shortcuts', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    await page.getByTestId('bpmnShortcuts').click();

    const dialog = page.locator('.modal-body.shortcut-keys');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('h5')).toHaveText(['Editing', 'Tools', 'View']);
    await expect(dialog.getByText('Delete selection')).toBeVisible();
  });

  test('opens and closes the minimap', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // The module is registered by the `miniMap` setting, and the stylesheet hides the minimap's
    // own toggle — so if this button did nothing there would be no way to see the minimap at all.
    // diagram-js-minimap marks the open state with `open` on its own container and starts closed.
    const minimap = page.locator('.djs-minimap');
    await expect(minimap).toHaveCount(1);
    await expect(minimap).not.toHaveClass(/\bopen\b/);

    await page.getByTestId('bpmnToggleMinimap').click();
    await expect(minimap).toHaveClass(/\bopen\b/);

    await page.getByTestId('bpmnToggleMinimap').click();
    await expect(minimap).not.toHaveClass(/\bopen\b/);
  });
});
