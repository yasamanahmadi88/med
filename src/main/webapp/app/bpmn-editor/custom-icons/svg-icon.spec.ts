import {
  MAX_ICON_BYTES,
  formatKb,
  fromIconDataUri,
  isIconDataUri,
  isStoredIconSafe,
  toIconDataUri,
  utf8ByteLength,
  validateSvgIcon,
} from './svg-icon';

/**
 * The upload boundary. Everything else in the feature trusts what comes out of here, so these are
 * the tests that decide whether a hostile SVG can reach a diagram at all.
 */
describe('custom icon SVG validation', () => {
  const CLEAN = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7" /></svg>';

  const message = (source: string): string => {
    const result = validateSvgIcon(source);
    return result.ok ? '' : result.message;
  };

  describe('what it accepts', () => {
    it('accepts a plain SVG and hands back a data URI that decodes to the same bytes', () => {
      const result = validateSvgIcon(CLEAN);

      expect(result.ok).toBe(true);
      expect(result.ok && result.dataUri).toMatch(/^data:image\/svg\+xml;base64,/);
      expect(result.ok && fromIconDataUri(result.dataUri)).toBe(CLEAN);
    });

    it('accepts an inline <style> and a fragment-free data: href', () => {
      // Both are ordinary in exported icons, and neither reaches outside the file.
      const styled = `<svg xmlns="http://www.w3.org/2000/svg"><style>.a{fill:red}</style><rect class="a" /></svg>`;
      const embedded = `<svg xmlns="http://www.w3.org/2000/svg"><image href="data:image/png;base64,AAA=" /></svg>`;

      expect(validateSvgIcon(styled).ok).toBe(true);
      expect(validateSvgIcon(embedded).ok).toBe(true);
    });

    it('encodes an SVG with non-Latin1 text, which btoa alone cannot', () => {
      // The Vue registry called `btoa(svgContent)` directly
      // (`utils/customIconRegistry-fixed.ts:83`), so any icon carrying Persian or Chinese text
      // threw InvalidCharacterError and the upload died. This asserts both halves: that the raw
      // call still throws, and that this one does not.
      const persian = '<svg xmlns="http://www.w3.org/2000/svg"><text>پرداخت</text></svg>';

      expect(() => btoa(persian)).toThrow();

      const result = validateSvgIcon(persian);
      expect(result.ok).toBe(true);
      expect(result.ok && fromIconDataUri(result.dataUri)).toBe(persian);
    });

    it('counts bytes rather than characters when measuring an icon', () => {
      // A Persian character is two UTF-8 bytes, so `.length` would under-count the size limit by
      // half on exactly the icons most likely to be near it.
      const persian = 'پ';

      expect(persian.length).toBe(1);
      expect(utf8ByteLength(persian)).toBe(2);
    });
  });

  describe('what it refuses', () => {
    it('refuses a <script> element', () => {
      const hostile = `<svg xmlns="http://www.w3.org/2000/svg"><script>fetch('https://example.test')</script></svg>`;

      expect(validateSvgIcon(hostile).ok).toBe(false);
      expect(message(hostile)).toContain('<script>');
    });

    it('refuses an event handler attribute, whatever its case', () => {
      const onload = `<svg xmlns="http://www.w3.org/2000/svg" onload="alert(1)"><rect /></svg>`;
      const nested = `<svg xmlns="http://www.w3.org/2000/svg"><rect onClick="alert(1)" /></svg>`;

      expect(validateSvgIcon(onload).ok).toBe(false);
      expect(message(onload)).toContain('onload');
      expect(validateSvgIcon(nested).ok).toBe(false);
    });

    it('refuses a <foreignObject>, where arbitrary HTML would live', () => {
      const hostile = `<svg xmlns="http://www.w3.org/2000/svg"><foreignObject><body xmlns="http://www.w3.org/1999/xhtml"/></foreignObject></svg>`;

      expect(validateSvgIcon(hostile).ok).toBe(false);
      expect(message(hostile)).toContain('foreignobject');
    });

    it('refuses a link to anywhere but a data: URI', () => {
      const remote = `<svg xmlns="http://www.w3.org/2000/svg"><image href="https://example.test/pixel.png" /></svg>`;
      const xlink = `<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><use xlink:href="http://example.test/x.svg#a" /></svg>`;

      expect(validateSvgIcon(remote).ok).toBe(false);
      expect(message(remote)).toContain('outside itself');
      expect(validateSvgIcon(xlink).ok).toBe(false);
    });

    it('refuses a javascript: URL in any attribute', () => {
      const hostile = `<svg xmlns="http://www.w3.org/2000/svg"><a href="java script:alert(1)"><rect /></a></svg>`;

      expect(validateSvgIcon(hostile).ok).toBe(false);
    });

    it('refuses a file whose root element is not an SVG', () => {
      // What `accept=".svg"` does not stop: the filter is on the file dialog, and a renamed file
      // arrives with the extension the user gave it.
      const html = '<html xmlns="http://www.w3.org/1999/xhtml"><body>not an icon</body></html>';

      expect(validateSvgIcon(html).ok).toBe(false);
      expect(message(html)).toContain('not an SVG');
    });

    it('refuses a file that does not parse', () => {
      expect(validateSvgIcon('<svg><rect></svg>').ok).toBe(false);
      expect(message('<svg><rect></svg>')).toContain('not valid XML');
    });

    it('refuses an empty file', () => {
      expect(validateSvgIcon('').ok).toBe(false);
    });

    it('refuses an icon over the per-icon limit, and says how big it was', () => {
      const padding = 'x'.repeat(MAX_ICON_BYTES);
      const huge = `<svg xmlns="http://www.w3.org/2000/svg"><desc>${padding}</desc></svg>`;

      expect(utf8ByteLength(huge)).toBeGreaterThan(MAX_ICON_BYTES);
      expect(validateSvgIcon(huge).ok).toBe(false);
      expect(message(huge)).toContain(formatKb(MAX_ICON_BYTES));
    });
  });

  describe('what it accepts back out of a diagram', () => {
    it('recognises only the encoding it produces', () => {
      expect(isIconDataUri(toIconDataUri(CLEAN))).toBe(true);

      // A `.bpmn` file is user input: `contents` is whatever somebody wrote in it.
      expect(isIconDataUri('javascript:alert(1)')).toBe(false);
      expect(isIconDataUri('data:text/html;base64,PHN2Zz48L3N2Zz4=')).toBe(false);
      expect(isIconDataUri('data:image/svg+xml,<svg onload="alert(1)"/>')).toBe(false);
      expect(isIconDataUri(undefined)).toBe(false);
    });

    it('re-checks the SVG behind a stored URI, not only its prefix', () => {
      // The same hostile file, correctly base64-encoded under the right media type. Only decoding
      // and re-validating catches this one — a prefix check passes it.
      const hostile = toIconDataUri(`<svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script></svg>`);

      expect(isIconDataUri(hostile)).toBe(true);
      expect(isStoredIconSafe(hostile)).toBe(false);
      expect(isStoredIconSafe(toIconDataUri(CLEAN))).toBe(true);
    });

    it('returns nothing for base64 that is not UTF-8', () => {
      expect(fromIconDataUri(`data:image/svg+xml;base64,${btoa('\xff\xfe')}`)).toBeUndefined();
    });
  });
});
