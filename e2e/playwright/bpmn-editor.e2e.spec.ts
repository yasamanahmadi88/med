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

  test('creates an element from the custom canvas context menu', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const canvas = page.locator('.bpmn-canvas .djs-container');
    await expect(canvas).toBeVisible();
    const canvasBox = await canvas.boundingBox();
    expect(canvasBox).not.toBeNull();

    await page.mouse.click(canvasBox!.x + canvasBox!.width * 0.7, canvasBox!.y + canvasBox!.height * 0.7, { button: 'right' });

    const menu = page.locator('[data-cy="bpmnContextMenu"]');
    await expect(menu).toBeVisible();
    await expect(menu).toContainText('Create Element');
    await expect(menu.getByRole('menuitem', { name: 'Exclusive Gateway', exact: true })).toBeVisible();
    await expect(menu.getByRole('menuitem', { name: 'Message Boundary Event', exact: true })).toBeVisible();
    await expect(menu.getByRole('menuitem', { name: 'Task', exact: true })).toHaveCount(0);
    const before = await page.locator('.bpmn-canvas .djs-element').count();

    await menu.getByRole('menuitem', { name: 'Start Event', exact: true }).click();

    // ContextMenuComponent schedules create.start(...) after 30 ms.
    await page.waitForTimeout(100);
    await page.mouse.click(canvasBox!.x + canvasBox!.width * 0.6, canvasBox!.y + canvasBox!.height * 0.6);

    await expect(page.locator('.bpmn-canvas .djs-element')).not.toHaveCount(before);
    const createdStartEvent = page.locator('.bpmn-canvas .djs-shape[data-element-id^="StartEvent_"]').last();
    await createdStartEvent.click();
    await expect(page.locator('.bpmn-canvas .djs-context-pad [data-action="set-color"]')).toBeVisible();
  });

  test('does not suppress the browser context menu outside the canvas', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    for (const selector of ['jhi-toolbar', 'jhi-panel']) {
      const defaultPrevented = await page.locator(selector).evaluate(element => {
        const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
        element.dispatchEvent(event);
        return event.defaultPrevented;
      });

      expect(defaultPrevented, `${selector} should allow the browser context menu`).toBe(false);
    }
  });

  test('renders the properties panel into the panel component', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // bpmn-js-properties-panel renders into the element PanelComponent registers.
    await expect(page.locator('.editor-properties-panel__content .bio-properties-panel')).toBeVisible();
  });

  test('stacks the canvas above a collapsible properties panel on a narrow viewport', async ({ page, mockApi }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const designer = page.locator('jhi-designer');
    const propertiesPanel = page.locator('jhi-panel');

    await expect(designer).toBeVisible();
    await expect(propertiesPanel).toBeVisible();
    await expect(page.locator('.editor-properties-panel__title')).toHaveText('Properties');

    const designerBox = await designer.boundingBox();
    const panelBox = await propertiesPanel.boundingBox();

    expect(designerBox).not.toBeNull();
    expect(panelBox).not.toBeNull();
    expect(designerBox!.width).toBeGreaterThan(300);
    expect(panelBox!.width).toBeGreaterThan(300);
    expect(panelBox!.y).toBeGreaterThanOrEqual(designerBox!.y + designerBox!.height - 1);

    const toggle = page.getByTestId('bpmnPropertiesToggle');
    await toggle.click();
    await expect(toggle).toHaveAttribute('aria-expanded', 'false');
    await expect(page.locator('.editor-properties-panel__content')).toBeHidden();
    await expect(toggle).toBeInViewport();

    const expandedDesignerBox = await designer.boundingBox();
    const collapsedPanelBox = await propertiesPanel.boundingBox();
    const viewport = page.viewportSize();

    expect(expandedDesignerBox).not.toBeNull();
    expect(collapsedPanelBox).not.toBeNull();
    expect(viewport).not.toBeNull();
    expect(expandedDesignerBox!.height).toBeGreaterThan(designerBox!.height);
    expect(collapsedPanelBox!.y + collapsedPanelBox!.height).toBeLessThanOrEqual(viewport!.height);

    await toggle.click();
    await expect(toggle).toHaveAttribute('aria-expanded', 'true');
    await expect(page.locator('.editor-properties-panel__content')).toBeVisible();
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

    for (const title of ['Save', 'Preview as XML', 'Cancel', 'Zoom Out', 'Zoom Reset', 'Zoom In', 'Undo', 'Redo', 'Restart']) {
      await expect(page.locator(`.toolbar button[title="${title}"]`), `toolbar ${title}`).toBeVisible();
    }
  });
  test('keeps the BPMN full-screen layout inside the viewport', async ({ page, mockApi }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const appRoot = page.locator('.app-root');
    const main = page.locator('.app-root > main');
    const editor = page.locator('#designer-container');

    await expect(appRoot).toHaveClass(/full-screen-mode/);
    await expect(main).toBeVisible();
    await expect(editor).toBeVisible();

    const viewport = page.viewportSize();
    const appBox = await appRoot.boundingBox();
    const mainBox = await main.boundingBox();
    const editorBox = await editor.boundingBox();

    expect(viewport).not.toBeNull();
    expect(appBox).not.toBeNull();
    expect(mainBox).not.toBeNull();
    expect(editorBox).not.toBeNull();

    expect(appBox!.y + appBox!.height).toBeLessThanOrEqual(viewport!.height + 1);
    expect(mainBox!.y + mainBox!.height).toBeLessThanOrEqual(viewport!.height + 1);
    expect(editorBox!.y + editorBox!.height).toBeLessThanOrEqual(viewport!.height + 1);
  });

  test('maps active BPMN logical dividers correctly in LTR and RTL', async ({ page, mockApi }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const panelHost = page.locator('#designer-container .main-content > jhi-panel');
    const panelBody = panelHost.locator('.editor-properties-panel');

    await expect(panelHost).toBeVisible();
    await expect(panelBody).toBeVisible();

    const readBorders = async (locator: typeof panelHost) =>
      locator.evaluate(element => {
        const style = window.getComputedStyle(element);

        return {
          direction: style.direction,
          left: Number.parseFloat(style.borderLeftWidth) || 0,
          right: Number.parseFloat(style.borderRightWidth) || 0,
        };
      });

    await page.locator('html').evaluate(element => element.setAttribute('dir', 'ltr'));

    await page.waitForFunction(() => document.documentElement.dir === 'ltr');

    const ltrHost = await readBorders(panelHost);
    const ltrBody = await readBorders(panelBody);

    expect(ltrHost.direction).toBe('ltr');
    expect(ltrBody.direction).toBe('ltr');

    expect(ltrHost.left).toBeGreaterThan(0);
    expect(ltrHost.right).toBe(0);

    expect(ltrBody.left).toBeGreaterThan(0);
    expect(ltrBody.right).toBe(0);

    await page.locator('html').evaluate(element => element.setAttribute('dir', 'rtl'));

    await page.waitForFunction(() => document.documentElement.dir === 'rtl');

    const rtlHost = await readBorders(panelHost);
    const rtlBody = await readBorders(panelBody);

    expect(rtlHost.direction).toBe('rtl');
    expect(rtlBody.direction).toBe('rtl');

    expect(rtlHost.left).toBe(0);
    expect(rtlHost.right).toBeGreaterThan(0);

    expect(rtlBody.left).toBe(0);
    expect(rtlBody.right).toBeGreaterThan(0);

    // jhi-palette is conditional (*ngIf="customPalette").
    // Validate it too when this configuration actually renders it.
    const customPalette = page.locator('#designer-container .main-content > jhi-palette');

    if ((await customPalette.count()) > 0) {
      await expect(customPalette).toBeVisible();

      await page.locator('html').evaluate(element => element.setAttribute('dir', 'ltr'));
      await page.waitForFunction(() => document.documentElement.dir === 'ltr');

      const ltrPalette = await readBorders(customPalette);

      expect(ltrPalette.left).toBe(0);
      expect(ltrPalette.right).toBeGreaterThan(0);

      await page.locator('html').evaluate(element => element.setAttribute('dir', 'rtl'));
      await page.waitForFunction(() => document.documentElement.dir === 'rtl');

      const rtlPalette = await readBorders(customPalette);

      expect(rtlPalette.left).toBeGreaterThan(0);
      expect(rtlPalette.right).toBe(0);
    }
  });
});
