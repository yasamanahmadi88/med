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

  /** U+200C ZERO WIDTH NON-JOINER — نیم‌فاصله. Stored literally in these files, never as `‌`. */
  const ZWNJ = '‌';
  /** The Arabic/Persian block, used to tell a Persian letter from a space, digit, brace or Latin letter. */
  const PERSIAN = '؀-ۿ';

  /**
   * Persian binds some morphemes to their host word with a ZWNJ rather than a space. The rule these
   * cases share is that the bound element is *not a free word*: it cannot appear on its own. An
   * ordinary boundary between two free words stays a plain space, so this is deliberately a short
   * list of specific morphemes rather than a blanket "no spaces near Persian letters" check.
   *
   * Each pattern captures the host tail, the space, and the bound morpheme, so a failure can quote
   * the exact offending substring rather than the whole sentence.
   */
  const ZWNJ_RULES: { id: string; morpheme: string; re: RegExp }[] = [
    {
      id: 'plural suffix',
      morpheme: 'ها / های',
      // The host may end in a Persian letter or in the `}}` of an interpolation placeholder
      // (`entity.action.show` is "نمایش {{otherEntity}}‌ها"). The trailing guard keeps the rule off
      // words that merely begin with ها.
      re: new RegExp(`[${PERSIAN}}] (?:های|ها)(?![${PERSIAN}])`, 'g'),
    },
    {
      id: 'verb prefix',
      morpheme: 'می / نمی',
      // Leading guard makes می a standalone token, so the rule cannot fire inside a word that merely
      // ends in ...می, such as عمومی ("general").
      re: new RegExp(`(?:^|[^${PERSIAN}])ن?می [${PERSIAN}]`, 'g'),
    },
    {
      id: 'SI combining form',
      morpheme: 'میلی',
      // Same standalone-token guard, and here it is load-bearing: without it the rule would fire on
      // ایمیلی که ("an email that"), where ...میلی is simply the tail of ایمیلی.
      re: new RegExp(`(?:^|[^${PERSIAN}])میلی [${PERSIAN}]`, 'g'),
    },
    {
      id: 'privative prefix',
      morpheme: 'بی',
      re: new RegExp(`(?:^|[^${PERSIAN}])بی [${PERSIAN}]`, 'g'),
    },
    {
      id: 'indefinite enclitic',
      morpheme: 'ای',
      // The indefinite -i after a silent ه, as in وقایع ثبت شده‌ای.
      re: new RegExp(`ه (?:ای)(?![${PERSIAN}])`, 'g'),
    },
  ];

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

    it('binds Persian suffixes and prefixes with a ZWNJ rather than a space', () => {
      const offenders: { key: string; rule: string; morpheme: string; found: string; expected: string }[] = [];
      for (const [key, value] of Object.entries(read('fa', file))) {
        for (const { id, morpheme, re } of ZWNJ_RULES) {
          for (const found of value.match(new RegExp(re.source, 'g')) ?? []) {
            // The binding space is always the last one in the match: the prefix rules open with a
            // guard character that is itself often a space, and replacing that one instead would
            // print advice that is subtly wrong (`‌می ب` rather than `می‌ب`).
            offenders.push({ key, rule: id, morpheme, found, expected: found.replace(/ (?=[^ ]*$)/, ZWNJ) });
          }
        }
      }
      // Reported as a list so one run names every offending key rather than only the first, and the
      // quoted substring is short enough to spot the space in.
      expect({ file, offenders }).toEqual({ file, offenders: [] });
    });

    it('writes ZWNJ as a literal U+200C, never as an escape sequence', () => {
      // The check above compares literal characters, so it would silently pass a file that spelled
      // the joiner `‌`. ngx-translate would then render the escape as text.
      const raw = readFileSync(join(I18N, 'fa', file), 'utf8');
      expect({ file, escapes: raw.match(/\\u200[cC]/g) ?? [] }).toEqual({ file, escapes: [] });
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

  /**
   * `entity.action.show` is the one value where the plural suffix lands directly against an
   * interpolation placeholder: "نمایش {{otherEntity}}‌ها". The ZWNJ sits immediately after the
   * closing braces, which is exactly where `templateMatcher` stops looking, so it is worth proving
   * rather than assuming that the slot still fills.
   */
  describe('entity.action.show — ZWNJ against a placeholder', () => {
    const value = read('fa', 'global.json')['entity.action.show'];

    it('is stored with the ZWNJ bound to the placeholder', () => {
      expect(value).toBe(`نمایش {{otherEntity}}${ZWNJ}ها`);
    });

    it('does not let the ZWNJ enter the captured placeholder name', () => {
      // U+200C is not in JavaScript's \s class, so `\s?}}` cannot consume it; the ZWNJ stays
      // outside the match entirely. The key must therefore be exactly "otherEntity".
      expect(/\s/.test(ZWNJ)).toBe(false);
      const matches = [...value.matchAll(new RegExp(NGX_TEMPLATE_MATCHER.source, 'g'))];
      expect(matches.map(match => match[1])).toEqual(['otherEntity']);
    });

    it('still substitutes, and keeps the ZWNJ in the rendered string', () => {
      // The same replace() call ngx-translate's TranslateDefaultParser.interpolateString makes.
      const rendered = value.replace(new RegExp(NGX_TEMPLATE_MATCHER.source, 'g'), (substring, key) =>
        key === 'otherEntity' ? 'ماژول' : substring,
      );
      expect(rendered).toBe(`نمایش ماژول${ZWNJ}ها`);
      expect(rendered).not.toContain('{{');
    });

    it('would fail if the ZWNJ were inside the braces, so the check above is not vacuous', () => {
      const corrupted = `نمایش {{otherEntity${ZWNJ}}}${ZWNJ}ها`;
      const matches = [...corrupted.matchAll(new RegExp(NGX_TEMPLATE_MATCHER.source, 'g'))];
      // ZWNJ *is* matched by [^{}\s], so inside the braces it becomes part of the key and the
      // lookup misses. That is the failure mode this placement avoids.
      expect(matches.map(match => match[1])).toEqual([`otherEntity${ZWNJ}`]);
    });
  });
});
