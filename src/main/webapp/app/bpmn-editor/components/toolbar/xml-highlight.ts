import hljs from 'highlight.js/lib/core';
import xmlLanguage from 'highlight.js/lib/languages/xml';

let xmlLanguageRegistered = false;

/**
 * Highlights BPMN XML using highlight.js, escaping all text nodes safely.
 * Runs once per unique XML string (via setter memoization), not per change-detection cycle.
 *
 * @param xml The raw BPMN XML document
 * @returns HTML string with token classes (.hljs-tag, .hljs-attr, etc.) ready for [innerHTML]
 */
export function highlightXml(xml: string): string {
  if (!xmlLanguageRegistered) {
    hljs.registerLanguage('xml', xmlLanguage);
    xmlLanguageRegistered = true;
  }
  return hljs.highlight(xml, { language: 'xml' }).value;
}
