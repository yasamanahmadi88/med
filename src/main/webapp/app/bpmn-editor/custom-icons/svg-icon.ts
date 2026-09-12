/**
 * Validation and encoding for a user-uploaded SVG icon.
 *
 * An uploaded icon is hostile input that is then stored in the diagram and shown to everybody who
 * opens that flow, so the rules here are the boundary the rest of the feature is built on:
 *
 * - the only shape that ever leaves this file is a `data:image/svg+xml;base64,…` URI, and the only
 *   places that URI is used are an `<img src>` (the palette entry) and an SVG `<image href>` (the
 *   canvas). Both are image contexts: a browser neither runs script nor resolves external
 *   references in an SVG loaded that way. Nothing here is ever assigned as markup.
 * - `contents` read back out of a diagram goes through {@link isIconDataUri} and then through the
 *   same validator, because a `.bpmn` file is user input too and nothing stops someone writing
 *   `contents="javascript:…"` into one by hand.
 *
 * The limits are stated on the constants; the checks are stated on {@link validateSvgIcon}.
 */

/**
 * The largest SVG source accepted for one icon, in UTF-8 bytes.
 *
 * The number a user can check against their own file. 32 KB is about eight times a typical
 * hand-drawn or Material-style icon and still leaves room for an editor-exported one with its
 * metadata left in; past that the file is a picture rather than an icon.
 */
export const MAX_ICON_BYTES = 32 * 1024;

/**
 * The largest icon library a diagram may carry, in stored characters.
 *
 * Measured on the encoded form because that is what lands in `FlowEntity.flow` — a CLOB, so the
 * column takes it, but every save, every load and every `/api/flows` row carries it. base64 is
 * ASCII, so characters are bytes here. 192 KB is roughly 35 typical icons, or six at the per-icon
 * limit, against a diagram that is otherwise a few kilobytes.
 */
export const MAX_LIBRARY_BYTES = 192 * 1024;

const SVG_NAMESPACE = 'http://www.w3.org/2000/svg';

const DATA_URI_PREFIX = 'data:image/svg+xml;base64,';

/** Exactly the form {@link toIconDataUri} produces, and nothing else. */
const DATA_URI_PATTERN = /^data:image\/svg\+xml;base64,[A-Za-z0-9+/]+={0,2}$/;

/**
 * Elements that either run code or pull in a document of their own.
 *
 * Animation elements are deliberately absent: they cannot script, and the one thing they could
 * rewrite — an `href` — has already been restricted to a `data:` URI by the attribute rules below.
 */
const FORBIDDEN_ELEMENTS = ['script', 'foreignobject', 'iframe', 'embed', 'object', 'audio', 'video', 'handler'];

const LINK_ATTRIBUTES = ['href', 'xlink:href'];

/**
 * A link an icon may carry: an embedded `data:` URI, or a reference to an element in the icon
 * itself. Everything else — `http:`, a protocol-relative `//host`, a bare path — would have the
 * browser fetch something at render time, which is what these rules exist to prevent.
 */
function isSelfContainedLink(value: string): boolean {
  const link = value.toLowerCase();
  return link.startsWith('data:') || link.startsWith('#');
}

export type SvgIconValidation = { ok: true; dataUri: string } | { ok: false; message: string };

/** How many bytes this string occupies as UTF-8 — not `.length`, which counts UTF-16 units. */
export function utf8ByteLength(source: string): number {
  return new TextEncoder().encode(source).length;
}

/** Whole kilobytes, rounded up, for a message a user is meant to compare with a file size. */
export function formatKb(bytes: number): string {
  return `${Math.ceil(bytes / 1024)} KB`;
}

/**
 * Encode an SVG source as a base64 `data:` URI.
 *
 * `btoa` throws `InvalidCharacterError` on any code point above U+00FF, so an icon with Persian or
 * Chinese text in it kills the upload — which is exactly what the Vue registry did
 * (`utils/customIconRegistry-fixed.ts:83`, `btoa(svgContent)`). Encoding to UTF-8 first and
 * mapping each byte to a Latin-1 character is what makes the input safe for it.
 */
export function toIconDataUri(source: string): string {
  const bytes = new TextEncoder().encode(source);
  let latin1 = '';
  for (const byte of bytes) {
    latin1 += String.fromCharCode(byte);
  }
  return DATA_URI_PREFIX + btoa(latin1);
}

/** True only for the exact encoding this module produces. */
export function isIconDataUri(contents: unknown): contents is string {
  return typeof contents === 'string' && DATA_URI_PATTERN.test(contents);
}

/** The SVG source behind a stored `data:` URI, or undefined if it is not one or does not decode. */
export function fromIconDataUri(contents: unknown): string | undefined {
  if (!isIconDataUri(contents)) {
    return undefined;
  }
  try {
    const latin1 = atob(contents.slice(DATA_URI_PREFIX.length));
    const bytes = Uint8Array.from(latin1, character => character.charCodeAt(0));
    return new TextDecoder('utf-8', { fatal: true }).decode(bytes);
  } catch {
    return undefined;
  }
}

/**
 * Accept an SVG source, or say why not.
 *
 * The file picker's `accept` filter is a hint to the file dialog and nothing more — a renamed
 * `.html` arrives here just as easily — so every check runs on the bytes:
 *
 * - the source is at most {@link MAX_ICON_BYTES};
 * - `DOMParser` parses it without a parser error, and the root element is an SVG `<svg>`;
 * - no element is one of {@link FORBIDDEN_ELEMENTS};
 * - no attribute name begins with `on`, in any case — that is the whole event-handler surface;
 * - every `href` / `xlink:href` is a `data:` URI or a `#fragment`, so nothing reaches out of the
 *   document;
 * - no attribute value carries a `javascript:` URI.
 *
 * What it does not do: it does not rewrite the SVG, so an icon that passes is stored byte for byte
 * as it arrived. That is safe only because of where the bytes end up — see the file comment.
 */
export function validateSvgIcon(source: string): SvgIconValidation {
  const bytes = utf8ByteLength(source);
  if (bytes === 0) {
    return { ok: false, message: 'That file is empty.' };
  }
  if (bytes > MAX_ICON_BYTES) {
    return {
      ok: false,
      message: `That icon is ${formatKb(bytes)}. An icon may be at most ${formatKb(MAX_ICON_BYTES)}.`,
    };
  }

  const parsed = new DOMParser().parseFromString(source, 'image/svg+xml');

  // A parse failure is reported as a document *containing* a <parsererror>, not as a throw, and
  // where that element sits differs between browsers — so the whole tree is searched.
  if (parsed.getElementsByTagName('parsererror').length > 0) {
    return { ok: false, message: 'That file is not valid XML, so it cannot be read as an SVG.' };
  }

  const root = parsed.documentElement;
  if (!root || root.localName.toLowerCase() !== 'svg' || root.namespaceURI !== SVG_NAMESPACE) {
    return { ok: false, message: 'That file is not an SVG: its root element has to be <svg>.' };
  }

  for (const element of [root, ...Array.from(root.querySelectorAll('*'))]) {
    const name = element.localName.toLowerCase();
    if (FORBIDDEN_ELEMENTS.includes(name)) {
      return { ok: false, message: `That SVG contains a <${name}> element, which is not allowed in an icon.` };
    }

    for (const attribute of Array.from(element.attributes)) {
      const attributeName = attribute.name.toLowerCase();

      if (attributeName.startsWith('on')) {
        return { ok: false, message: `That SVG carries an event handler (${attribute.name}), which is not allowed in an icon.` };
      }

      const value = attribute.value.trim();

      // A `#fragment` resolves inside the icon's own document and cannot reach out of it, and it
      // is how every real drawing tool wires a gradient, a clip path or a `<use>`: rejecting it
      // would refuse most icons a user actually has, with a message telling them the file is not
      // self-contained when it plainly is.
      if (LINK_ATTRIBUTES.includes(attributeName) && !isSelfContainedLink(value)) {
        return {
          ok: false,
          message: `That SVG links to something outside itself (${attribute.name}="${attribute.value}"). An icon has to be self-contained.`,
        };
      }

      if (value.toLowerCase().replace(/\s/g, '').includes('javascript:')) {
        return { ok: false, message: `That SVG carries a javascript: URL in ${attribute.name}, which is not allowed in an icon.` };
      }
    }
  }

  return { ok: true, dataUri: toIconDataUri(source) };
}

/** The same rules, applied to an icon that arrived inside a diagram rather than from a file. */
export function isStoredIconSafe(contents: unknown): contents is string {
  const source = fromIconDataUri(contents);
  return source !== undefined && validateSvgIcon(source).ok;
}
