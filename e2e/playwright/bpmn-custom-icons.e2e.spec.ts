import { expect, test } from './support/medportal-fixtures';
import type { Page } from '@playwright/test';

/**
 * Custom icons, end to end.
 *
 * The claim this suite exists for is that an icon travels in the diagram: upload it, place it,
 * export the XML, import that XML back and the shape is still drawn from it. Nothing short of a
 * real browser can hold that — bpmn-js renders through the SVG DOM, which jsdom does not
 * implement, so the unit specs stub the modeler and can only cover the model and the wiring.
 *
 * The shared `page` fixture fails the test on any unexpected console error, page error or failed
 * request, so an icon whose data URI the browser refuses, or a renderer that throws on a shape
 * whose icon is missing, surfaces here rather than passing quietly.
 */

/**
 * The uploaded icon. Deliberately not ASCII: `btoa` throws on any code point above U+00FF, which
 * is exactly what the Vue registry did to an icon with Persian or Chinese text in it
 * (`utils/customIconRegistry-fixed.ts:83`). If the encoding regressed, the upload fails here.
 */
const ICON_SVG =
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">' +
  '<title>نماد پرداخت</title><rect x="1" y="1" width="22" height="22" rx="4" fill="#2b6cb0" /></svg>';

/** The same, carrying a string that must never appear in the page as text. */
const SENTINEL_SVG =
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><text x="2" y="12">ICON-SENTINEL</text><rect width="24" height="24" fill="#eee" /></svg>';

const svgFile = (source: string, name = 'payment.svg') => ({
  name,
  mimeType: 'image/svg+xml',
  buffer: Buffer.from(source, 'utf8'),
});

const canvasImages = (page: Page) => page.locator('.bpmn-canvas .djs-element image');

const paletteEntries = (page: Page) => page.locator('.djs-palette .entry[data-action^="create.custom-icon-"]');

/** Upload one icon through the toolbar dialog and close it. */
async function uploadIcon(page: Page, source: string, name: string): Promise<void> {
  await page.getByTestId('bpmnCustomIcons').click();
  await page.getByTestId('bpmnIconFile').setInputFiles(svgFile(source));
  await expect(page.getByTestId('bpmnIconPreview')).toHaveAttribute('src', /^data:image\/svg\+xml;base64,/);

  await page.getByTestId('bpmnIconName').fill(name);
  await page.getByTestId('bpmnIconAdd').click();

  await expect(page.locator('[data-cy="bpmnIconList"] li')).toHaveCount(1);
  await page.locator('.modal-footer .btn-secondary').click();
  await expect(page.locator('[data-cy="bpmnIconList"]')).toHaveCount(0);
}

/** Place the first custom-icon palette entry in the middle of the canvas. */
async function placeIcon(page: Page): Promise<void> {
  const canvas = page.locator('.bpmn-canvas .djs-container');
  await paletteEntries(page).first().click();
  const box = await canvas.boundingBox();
  await page.mouse.click(box!.x + box!.width / 2, box!.y + box!.height / 2);
  // Placing opens the label editor with the caret in it; the rest of the test uses the keyboard.
  await page.keyboard.press('Escape');
}

/** The XML the editor would save, read out of the preview dialog. */
async function currentXml(page: Page): Promise<string> {
  await page.getByTestId('bpmnPreviewXml').click();
  const xml = (await page.getByTestId('bpmnXmlPreview').textContent()) ?? '';
  await page.locator('.modal-footer .btn-secondary').click();
  await expect(page.getByTestId('bpmnXmlPreview')).toHaveCount(0);
  return xml;
}

/** Load a diagram through the toolbar's Import button, which opens a real file chooser. */
async function importXml(page: Page, xml: string): Promise<void> {
  const chooser = page.waitForEvent('filechooser');
  await page.locator('.toolbar button[title="Import"]').click();
  await (await chooser).setFiles({ name: 'diagram.bpmn', mimeType: 'application/xml', buffer: Buffer.from(xml, 'utf8') });
}

test.describe('BPMN editor custom icons', () => {
  test('an uploaded icon survives export and import, and is drawn both times', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await page.goto('/bpmn-editor');
    await expect(page.locator('.bpmn-canvas .djs-container')).toBeVisible();

    await uploadIcon(page, ICON_SVG, 'Payment');

    // The palette entry is the icon itself, drawn by diagram-js as an <img src> from the stored
    // data URI (`diagram-js/lib/features/palette/Palette.js:275-280`).
    await expect(paletteEntries(page)).toHaveCount(1);
    await expect(paletteEntries(page).locator('img')).toHaveAttribute('src', /^data:image\/svg\+xml;base64,/);

    await placeIcon(page);

    await expect(canvasImages(page)).toHaveCount(1);
    const placed = await measurePlacedIcon(page);
    // Not a presence check: a renderer that drew a zero-sized image, or one that laid the label
    // over the icon, passes "the element is there" and fails all three of these.
    expect(placed.imageWidth).toBeGreaterThan(20);
    expect(placed.imageHeight).toBeGreaterThan(20);
    expect(placed.imageInsideShape).toBe(true);
    expect(placed.labelBelowImage).toBe(true);

    const exported = await currentXml(page);
    // The library, in the diagram's own definitions — not in the process, and not in the browser.
    expect(exported).toContain('<customIcon:iconLibrary>');
    expect(exported).toContain('iconId="Icon_1"');
    expect(exported).toContain('<customIcon:customTask');
    expect(exported.indexOf('<customIcon:iconLibrary>')).toBeLessThan(exported.indexOf('<bpmn:process'));

    // Start over with a diagram that has neither the icon nor the shape, so what comes back after
    // the import can only have come out of the XML.
    await page.getByTestId('bpmnRestart').click();
    await expect(paletteEntries(page)).toHaveCount(0);
    await expect(canvasImages(page)).toHaveCount(0);

    await importXml(page, exported);

    await expect(canvasImages(page)).toHaveCount(1);
    await expect(paletteEntries(page)).toHaveCount(1);
    const reimported = await measurePlacedIcon(page);
    expect(reimported.href).toBe(placed.href);
    expect(reimported.imageWidth).toBe(placed.imageWidth);
  });

  test('a diagram whose icon is missing still opens, drawn as a placeholder', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await page.goto('/bpmn-editor');
    await expect(page.locator('.bpmn-canvas .djs-container')).toBeVisible();

    // A flow saved by somebody who then removed the icon, or hand-edited XML: the shape points at
    // an id the library does not have. The page fixture fails on a thrown renderer, so opening at
    // all is half the assertion.
    await importXml(page, diagramWithoutLibrary());

    const shape = page.locator('.bpmn-canvas .djs-element[data-element-id="Task_1"]');
    await expect(shape).toBeVisible();
    await expect(shape.locator('.custom-icon-missing')).toHaveCount(1);
    await expect(shape.locator('image')).toHaveCount(0);
    // The name is still on it, so the user can see which task lost its icon.
    await expect(shape.locator('.djs-label')).toContainText('Nameless');
  });

  test('removing an icon that is in use leaves the shape, drawn as a placeholder', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await page.goto('/bpmn-editor');
    await expect(page.locator('.bpmn-canvas .djs-container')).toBeVisible();

    await uploadIcon(page, ICON_SVG, 'Payment');
    await placeIcon(page);
    await expect(canvasImages(page)).toHaveCount(1);
    const before = await page.locator('.bpmn-canvas .djs-element').count();

    await page.getByTestId('bpmnCustomIcons').click();
    await page.getByTestId('bpmnIconRemove-Icon_1').click();
    await expect(page.locator('[data-cy="bpmnIconList"] li')).toHaveCount(0);
    await page.locator('.modal-footer .btn-secondary').click();

    // The shape is not deleted out from under the user, and it is not left drawing an icon the
    // diagram no longer carries: it redraws as the placeholder.
    await expect(page.locator('.bpmn-canvas .djs-element')).toHaveCount(before);
    await expect(canvasImages(page)).toHaveCount(0);
    // Scoped to `.djs-element`: diagram-js-minimap draws its own clone of every shape inside
    // `.bpmn-canvas`, so an unscoped selector counts each of them twice.
    await expect(page.locator('.bpmn-canvas .djs-element .custom-icon-missing')).toHaveCount(1);
    await expect(paletteEntries(page)).toHaveCount(0);
  });

  test('a hostile SVG is refused, and a stored one never reaches the page as markup', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await page.goto('/bpmn-editor');
    await expect(page.locator('.bpmn-canvas .djs-container')).toBeVisible();

    await page.getByTestId('bpmnCustomIcons').click();

    // `accept=".svg"` is a filter on the file dialog and nothing more, so this arrives named as an
    // SVG and has to be refused on its contents.
    await page
      .getByTestId('bpmnIconFile')
      .setInputFiles(svgFile('<svg xmlns="http://www.w3.org/2000/svg" onload="window.__pwned = true"><script>window.__pwned = true</script></svg>'));

    await expect(page.getByTestId('bpmnIconError')).toBeVisible();
    await expect(page.getByTestId('bpmnIconPreview')).toHaveCount(0);
    await expect(page.getByTestId('bpmnIconAdd')).toBeDisabled();
    expect(await page.evaluate(() => (window as unknown as { __pwned?: boolean }).__pwned)).toBeUndefined();

    // And the accepted path: an icon whose own text must stay inside the image.
    await page.getByTestId('bpmnIconFile').setInputFiles(svgFile(SENTINEL_SVG, 'sentinel.svg'));
    await page.getByTestId('bpmnIconName').fill('Sentinel');
    await page.getByTestId('bpmnIconAdd').click();
    await page.locator('.modal-footer .btn-secondary').click();
    await placeIcon(page);
    await expect(canvasImages(page)).toHaveCount(1);

    const leaked = await page.evaluate(() => ({
      // `<img src>` and `<image href>` load an SVG as an image: its elements never enter this
      // document, so neither its text nor a <script> in it can be found here. Anything that
      // inlined the stored SVG — innerHTML, a bypassSecurityTrustHtml — puts both in reach.
      //
      // The document is full of `<text>` elements either way: every bpmn-js label is one, and the
      // shape placed here is labelled "Sentinel". What must not exist is one carrying the string
      // from inside the icon.
      sentinelInDocument: (document.documentElement.textContent ?? '').includes('ICON-SENTINEL'),
      sentinelNodes: Array.from(document.querySelectorAll('*')).filter(node =>
        Array.from(node.childNodes).some(child => child.nodeType === Node.TEXT_NODE && child.textContent?.includes('ICON-SENTINEL')),
      ).length,
      dataUriImages: document.querySelectorAll('image[href^="data:image/svg+xml;base64,"], img[src^="data:image/svg+xml;base64,"]').length,
    }));

    expect(leaked.sentinelInDocument).toBe(false);
    expect(leaked.sentinelNodes).toBe(0);
    // The canvas shape and the palette entry.
    expect(leaked.dataUriImages).toBeGreaterThanOrEqual(2);
  });
});

/** Geometry of the one placed custom-icon shape, so the assertions can be about what is drawn. */
async function measurePlacedIcon(page: Page): Promise<{
  href: string;
  imageWidth: number;
  imageHeight: number;
  imageInsideShape: boolean;
  labelBelowImage: boolean;
}> {
  return page.evaluate(() => {
    const image = document.querySelector('.bpmn-canvas .djs-element image')!;
    const shape = image.closest('.djs-element')!;
    const label = shape.querySelector('.djs-label')!;

    const imageBox = image.getBoundingClientRect();
    const shapeBox = shape.getBoundingClientRect();
    const labelBox = label.getBoundingClientRect();

    return {
      href: image.getAttribute('href') ?? '',
      imageWidth: Math.round(imageBox.width),
      imageHeight: Math.round(imageBox.height),
      imageInsideShape:
        imageBox.left >= shapeBox.left - 1 &&
        imageBox.right <= shapeBox.right + 1 &&
        imageBox.top >= shapeBox.top - 1 &&
        imageBox.bottom <= shapeBox.bottom + 1,
      labelBelowImage: labelBox.top >= imageBox.bottom - 1,
    };
  });
}

/** A diagram carrying a shape from an icon library it does not have. */
function diagramWithoutLibrary(): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:customIcon="http://medportal.behsa.com/schema/bpmn/custom-icons"
                  id="Definitions_missing" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_missing" isExecutable="true">
    <customIcon:customTask id="Task_1" name="Nameless" iconId="Icon_404" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_missing">
      <bpmndi:BPMNShape id="Task_1_di" bpmnElement="Task_1">
        <dc:Bounds x="160" y="80" width="120" height="120" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;
}
