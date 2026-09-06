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

  test('the panel header is not laid out around an icon it never draws', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const header = page.locator('.panel > .panel-header');
    await expect(header).toHaveText('Properties');

    // `styles/panel.scss` carried the Vue `.panel-header` rule over whole: a grid whose first
    // 40px column held `common/BpmnIcon.vue`, with two rows beside it for the element name and
    // type. This header renders one word and no icon, so the title was squeezed into that column
    // and wrapped — "Proper" over "ties". Counting line boxes is what catches it; the text reads
    // the same either way, and a text assertion would pass over the top of it.
    const lineBoxes = await header.evaluate(el => {
      const range = document.createRange();
      range.selectNodeContents(el);
      return range.getClientRects().length;
    });
    expect(lineBoxes).toBe(1);
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

  test('blocks saving while a module property is invalid, and says why', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // A FileTransmitter carries the IP field the Vue editor validated.
    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .fileTransmitter-module').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);
    await page.keyboard.press('Escape');

    const save = page.getByTestId('bpmnSave');
    await expect(save).toBeEnabled();

    // The panel renders each group collapsed; the fields exist but are not reachable until the
    // header is opened.
    await page.locator('.panel-content .bio-properties-panel-group-header', { hasText: 'FileTransmitter' }).click();

    const ip = page.locator('.panel-content [data-entry-id="ip"] input');
    await expect(ip).toBeVisible();
    await ip.fill('999.1.1.1');
    await ip.blur();

    // The panel says so on the field itself...
    await expect(page.locator('.panel-content [data-entry-id="ip"] .bio-properties-panel-error')).toHaveText(
      'Invalid IPv4 format (e.g., 192.168.1.1)',
    );
    // ...and the toolbar refuses to let it leave, naming the element and the reason. The Vue
    // Save button disabled itself with no explanation anywhere on the page.
    await expect(save).toBeDisabled();
    const problems = page.getByTestId('bpmnProblems');
    await expect(problems).toBeVisible();
    await expect(problems).toContainText('IP Address');
    await expect(problems).toContainText('Invalid IPv4 format');

    await ip.fill('10.0.0.1');
    await ip.blur();

    await expect(save).toBeEnabled();
    await expect(problems).toHaveCount(0);
  });

  test('leaves an unfilled property alone', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // None of the validated properties is required. A module dropped on the canvas and not yet
    // configured must not block the save.
    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .fileTransmitter-module').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);
    await page.keyboard.press('Escape');

    await page.locator('.panel-content .bio-properties-panel-group-header', { hasText: 'FileTransmitter' }).click();

    await expect(page.locator('.panel-content [data-entry-id="ip"] input')).toHaveValue('');
    await expect(page.getByTestId('bpmnSave')).toBeEnabled();
    await expect(page.getByTestId('bpmnProblems')).toHaveCount(0);
  });

  test('names the selected element by its concrete type', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // `bpmn-icons/getIconType.ts` in the Vue editor derived this name so the panel header could
    // show it. bpmn-js-properties-panel's `PanelHeaderProvider` computes the same name from the
    // same function, so what the port needed here was nothing — and this is the test that says
    // so, rather than an assumption in a comment.
    const header = page.locator('.bio-properties-panel-header');

    await page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first().click();
    await expect(header).toContainText(/start event/i);

    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .bpmn-icon-gateway-none').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);
    await page.keyboard.press('Escape');

    // Not "Gateway": the concrete type, which is what tells an exclusive gateway from a parallel
    // one in a diagram where both are drawn as diamonds.
    await expect(header).toContainText(/exclusive gateway/i);
  });

  test('draws a different header icon per element type', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // The other half of what `bpmn-icons/` existed for: 123 lines mapping a concrete type to an
    // icon. The panel ships the same map. Comparing the two rendered icons is what distinguishes
    // "the right icon" from "an icon" — a fallback would pass a mere presence check.
    const icon = page.locator('.bio-properties-panel-header-icon svg');

    await page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first().click();
    await expect(icon).toBeVisible();
    const startEventIcon = await icon.innerHTML();

    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .bpmn-icon-task').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);
    await page.keyboard.press('Escape');

    await expect(icon).toBeVisible();
    expect(await icon.innerHTML()).not.toBe(startEventIcon);
  });

  test('offers execution listeners on the element types that accept them', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // `config/bpmnEnums.ts` was one constant, `LISTENER_ALLOWED_TYPES`, with one consumer: the
    // Vue execution-listener editor. bpmn-js-properties-panel declares a constant of the same
    // name with the same six entries in the same order and gates its own Execution listeners
    // group on it, so this group appearing is that constant doing its job.
    const groups = page.locator('.bio-properties-panel-group-header-title');

    await page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first().click();
    await expect(groups.filter({ hasText: 'Execution listeners' })).toHaveCount(1);

    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .bpmn-icon-gateway-none').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);
    await page.keyboard.press('Escape');
    await expect(groups.filter({ hasText: 'Execution listeners' })).toHaveCount(1);

    // And on the process itself, reached by clicking bare canvas.
    await page
      .locator('.bpmn-canvas .djs-container svg')
      .first()
      .click({ position: { x: 700, y: 420 } });
    await expect(groups.filter({ hasText: 'Execution listeners' })).toHaveCount(1);
  });

  test('the panel groups open and close from their header', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // `common/CollapseTitle.vue` was the title row the Vue panel put inside a naive-ui collapse
    // item — the hand-rolled half of a collapsible section. @bpmn-io/properties-panel's own
    // `Group` is the whole thing: header, title, arrow and the open/closed state
    // (`dist/index.esm.js:923`). Asserting the entries appear and disappear is what separates
    // that from a header that only looks the part.
    await page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first().click();

    const general = page
      .locator('.bio-properties-panel-group')
      .filter({ has: page.locator('.bio-properties-panel-group-header-title', { hasText: 'General' }) })
      .first();
    const header = general.locator('.bio-properties-panel-group-header');
    const entries = general.locator('.bio-properties-panel-group-entries');

    // Groups open closed, so the first click has to be the one that reveals the fields.
    await expect(entries).toBeHidden();

    await header.click();
    await expect(entries).toBeVisible();
    await expect(entries.locator('#bio-properties-panel-id')).toBeVisible();

    await header.click();
    await expect(entries).toBeHidden();
  });

  test('each property row labels its own control', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // `common/EditItem.vue` was the Vue panel's label-and-slot row, and its label was a plain
    // `<div>` at a fixed pixel width — text near a control, associated with nothing. The panel's
    // entries render a real `<label for>` bound to the input, so clicking the label focuses the
    // field. That association is the behaviour worth holding: a `<div>` beside an input satisfies
    // any assertion about the label's text.
    await page.locator('.bpmn-canvas .djs-element[data-element-id^="StartEvent"]').first().click();

    const general = page
      .locator('.bio-properties-panel-group')
      .filter({ has: page.locator('.bio-properties-panel-group-header-title', { hasText: 'General' }) })
      .first();
    await general.locator('.bio-properties-panel-group-header').click();

    const idLabel = general.locator('label.bio-properties-panel-label[for="bio-properties-panel-id"]');
    await expect(idLabel).toHaveText('ID');

    await idLabel.click();
    await expect(page.locator('#bio-properties-panel-id')).toBeFocused();
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

  test('the replace menu is a list, not a row that runs off the screen', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    const canvas = page.locator('.bpmn-canvas .djs-container');
    await page.locator('.djs-palette .bpmn-icon-task').click();
    const box = await canvas.boundingBox();
    await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 3);
    await page.keyboard.press('Escape');
    await page.locator('.bpmn-canvas .djs-element[data-element-id^="Activity"]').first().click();
    await page.locator('.djs-context-pad .bpmn-icon-screw-wrench').click();
    await expect(page.locator('.djs-popup')).toBeVisible();

    // `styles/palette.scss` carried a `.djs-popup-group { display: flex !important }` over from
    // the Vue editor. diagram-js draws this menu as a `<ul>` inside a 300px popup and scrolls it
    // vertically (`.djs-popup-results { max-height: 280px; overflow: auto }`); forcing the list
    // into a row laid the ten "Change element" entries out 1341px wide, so eight of them fell
    // outside the popup — three under `.djs-popup-backdrop`, where a click dismisses the menu
    // rather than choosing a type, and four off the side of a 1280px viewport.
    //
    // Counting columns is what catches it. Every entry is present and carries the right label in
    // either layout, so a presence or text assertion walks straight past a menu the user cannot
    // reach the bottom half of.
    const geometry = await page.evaluate(() => {
      const popup = document.querySelector('.djs-popup')!.getBoundingClientRect();
      const entries = Array.from(document.querySelectorAll('.djs-popup-body .entry'));
      return {
        entryCount: entries.length,
        columns: new Set(entries.map(entry => Math.round(entry.getBoundingClientRect().left))).size,
        spillingRight: entries.filter(entry => entry.getBoundingClientRect().right > popup.right + 1).length,
      };
    });

    expect(geometry.entryCount).toBeGreaterThan(1);
    expect(geometry.columns).toBe(1);
    expect(geometry.spillingRight).toBe(0);
  });

  test('the grid background is painted on the element this editor renders', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/bpmn-editor');

    // `bg: 'grid-image'` is the shipped default (`config/index.ts:13`), and the container carries
    // the class for it. The rule that draws the grid was written for Vue's `<div class="designer">`
    // and this editor renders `<jhi-designer>`, so — like the `.panel-header` grid before it — it
    // matched nothing and the default background drew nothing at all.
    await expect(page.locator('#designer-container')).toHaveClass(/\bdesigner-with-bg\b/);

    const background = await page.evaluate(() => {
      const style = getComputedStyle(document.querySelector('jhi-designer')!);
      return { image: style.backgroundImage, repeat: style.backgroundRepeat, size: style.backgroundSize };
    });

    expect(background.image).toMatch(/^url\("data:image\/svg\+xml;base64,/);
    // The tile is 40px and has to repeat. `bpmn-editor.component.scss` asks for `contain` on the
    // same element, which would stretch one tile over the whole canvas; the `background`
    // shorthand is `!important`, so it resets the size and this pins that it still does.
    expect(background.repeat).toBe('repeat');
    expect(background.size).toBe('auto');
  });
});
