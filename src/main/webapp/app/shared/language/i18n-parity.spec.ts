import { readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';

/**
 * Structural parity between the shipped translation bundles.
 *
 * Both languages are merged into `i18n/<lang>.json` by `MergeJsonWebpackPlugin`
 * (`webpack/webpack.custom.js`) and read at runtime by ngx-translate, which resolves a key against
 * the active language only — there is no fallback to `en`. So a key present in `en` and absent
 * from `fa` renders as the raw key ("medPortalApp.flow.flowName") on screen, and a placeholder
 * dropped or renamed during translation renders as literal text or as nothing at all. Neither
 * failure is visible to the type checker or to any component test, because translation values are
 * data rather than code.
 *
 * These checks read the 28 source files per language off disk rather than the built bundle, so
 * they fail on the file that is wrong and name the key.
 */
describe('i18n bundle parity', () => {
  const I18N = join(process.cwd(), 'src/main/webapp/i18n');
  const files = readdirSync(join(I18N, 'en'))
    .filter(file => file.endsWith('.json'))
    .sort();

  /** `{ 'a.b.c': 'value' }` — the shape the runtime actually looks keys up in. */
  const flatten = (value: unknown, prefix: string, into: Record<string, string>): Record<string, string> => {
    for (const [key, child] of Object.entries(value as Record<string, unknown>)) {
      const path = prefix ? `${prefix}.${key}` : key;
      if (child !== null && typeof child === 'object' && !Array.isArray(child)) {
        flatten(child, path, into);
      } else {
        into[path] = String(child);
      }
    }
    return into;
  };

  const read = (lang: string, file: string): Record<string, string> =>
    flatten(JSON.parse(readFileSync(join(I18N, lang, file), 'utf8')), '', {});

  /** Placeholder *names*, spacing-insensitive: `{{ param }}` and `{{param}}` are the same slot. */
  const placeholders = (value: string): string[] => (value.match(/\{\{[^}]*\}\}/g) ?? []).map(token => token.replace(/\s+/g, '')).sort();
  const tags = (value: string): string[] => (value.match(/<[^>]+>/g) ?? []).sort();

  /**
   * The matcher ngx-translate actually interpolates with, copied verbatim from
   * `TranslateDefaultParser.templateMatcher` in
   * `@ngx-translate/core` (`fesm2022/ngx-translate-core.mjs`), where `interpolateString` runs
   * `expr.replace(this.templateMatcher, …)`. Note `\s?` — zero or *one* space on each side. A
   * second space is not a formatting nit: the occurrence stops matching, no substitution happens,
   * and the raw `{{ login  }}` is what the user reads.
   */
  const NGX_TEMPLATE_MATCHER = /{{\s?([^{}\s]*)\s?}}/g;
  /** Anything a translator would take for a placeholder, whether or not the runtime agrees. */
  const looksLikePlaceholder = (value: string): string[] => value.match(/\{\{[^{}]*\}\}/g) ?? [];

  it('ships the same 28 files in every language', () => {
    expect(
      readdirSync(join(I18N, 'fa'))
        .filter(file => file.endsWith('.json'))
        .sort(),
    ).toEqual(files);
  });

  describe.each(files)('%s', file => {
    it('translates every English key, and invents none', () => {
      const en = Object.keys(read('en', file)).sort();
      const fa = Object.keys(read('fa', file)).sort();
      expect(fa).toEqual(en);
    });

    it('keeps every interpolation placeholder the English string defines', () => {
      const en = read('en', file);
      const fa = read('fa', file);
      for (const [key, source] of Object.entries(en)) {
        // Superset, not equality: three pre-existing Persian strings (medAuthority, resource,
        // resourceAuthority) interpolate an id their English original spells out in words. That
        // is a richer translation, not a broken one. Losing a slot English defines is the break.
        expect({ key, placeholders: placeholders(fa[key] ?? '') }).toEqual({
          key,
          placeholders: expect.arrayContaining(placeholders(source)),
        });
      }
    });

    it.each(['en', 'fa'])('writes every %s placeholder in a form ngx-translate can interpolate', lang => {
      for (const [key, value] of Object.entries(read(lang, file))) {
        for (const occurrence of looksLikePlaceholder(value)) {
          // Tested per occurrence rather than over the whole value, so one unmatched placeholder
          // cannot hide behind a well-formed one earlier in the same string.
          const matcher = new RegExp(NGX_TEMPLATE_MATCHER.source, 'g');
          expect({ key, occurrence, interpolated: matcher.test(occurrence) }).toEqual({ key, occurrence, interpolated: true });
        }
      }
    });

    it('keeps the HTML markup of the English string intact', () => {
      const en = read('en', file);
      const fa = read('fa', file);
      for (const [key, source] of Object.entries(en)) {
        // These values reach the DOM through `[innerHTML]`, so a mangled tag is a rendering bug.
        expect({ key, tags: tags(fa[key] ?? '') }).toEqual({ key, tags: tags(source) });
      }
    });
  });
});
