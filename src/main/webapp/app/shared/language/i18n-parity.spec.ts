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

  /**
   * U+200C ZERO WIDTH NON-JOINER — the Persian half-space, نیم\u200Cفاصله.
   *
   * Note the deliberate asymmetry between the data and this spec. The `i18n/fa` JSON stores the
   * joiner as a literal character, and a test below fails if it is ever written as a `\u200C`
   * escape there, because ngx-translate would render such an escape as visible text. This file is
   * the opposite: it writes the escape and never the literal, because an invisible character in an
   * assertion cannot be read or reviewed, and `scripts/unicode-security-scan.sh` rejects literal
   * zero-width characters outside `i18n/fa/` as a Trojan Source risk.
   *
   * The two forms are the same string at runtime (`'\u200C'.length === 1`), so no assertion below
   * compares anything different from what it compared when it was written as a literal.
   */
  const ZWNJ = '\u200C';
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
      // The host may end in a Persian letter or in the `}}` of an interpolation placeholder — no
      // shipped value does the latter today, but the rule would still catch a space there. The
      // trailing guard keeps it off words that merely begin with ها.
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
      // The indefinite -i after a silent ه, as in وقایع ثبت شده\u200Cای.
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
            // print advice that is subtly wrong (`\u200Cمی ب` rather than `می\u200Cب`).
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
      // the joiner `\u200C`. ngx-translate would then render the escape as text.
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
   * Where a ZWNJ may sit relative to a placeholder. No shipped value binds a suffix to a `{{…}}`
   * today — `entity.action.show` deliberately does not, see below — but Persian gives every reason
   * to write one (`{{count}}\u200Cتایی`), so the boundary is recorded rather than rediscovered. The
   * asymmetry is entirely in `templateMatcher`'s character classes: `\s?` cannot consume a ZWNJ
   * *outside* the braces, and `[^{}\s]` happily swallows one *inside* them.
   */
  describe('ZWNJ against an interpolation placeholder', () => {
    it('is not whitespace, so a ZWNJ after the braces cannot disturb the match', () => {
      expect(/\s/.test(ZWNJ)).toBe(false);
      const bound = `نمایش {{otherEntity}}${ZWNJ}ها`;
      const matches = [...bound.matchAll(new RegExp(NGX_TEMPLATE_MATCHER.source, 'g'))];
      expect(matches.map(match => match[1])).toEqual(['otherEntity']);
      // The same replace() call TranslateDefaultParser.interpolateString makes.
      const rendered = bound.replace(new RegExp(NGX_TEMPLATE_MATCHER.source, 'g'), (substring, key) =>
        key === 'otherEntity' ? 'ماژول' : substring,
      );
      expect(rendered).toBe(`نمایش ماژول${ZWNJ}ها`);
      expect(rendered).not.toContain('{{');
    });

    it('is matched by [^{}\\s], so a ZWNJ inside the braces corrupts the key', () => {
      // The counter-example, which is what makes the case above a measurement rather than a hope.
      const corrupted = `نمایش {{otherEntity${ZWNJ}}}ها`;
      const matches = [...corrupted.matchAll(new RegExp(NGX_TEMPLATE_MATCHER.source, 'g'))];
      expect(matches.map(match => match[1])).toEqual([`otherEntity${ZWNJ}`]);
    });

    it('never appears inside a placeholder in any shipped value', () => {
      // The live guard: a ZWNJ typed between the braces renders the raw `{{…}}` on screen.
      const offenders: { lang: string; file: string; key: string; occurrence: string }[] = [];
      for (const lang of ['en', 'fa']) {
        for (const file of files) {
          for (const [key, value] of Object.entries(read(lang, file))) {
            for (const occurrence of looksLikePlaceholder(value)) {
              if (occurrence.includes(ZWNJ)) offenders.push({ lang, file, key, occurrence });
            }
          }
        }
      }
      expect(offenders).toEqual([]);
    });
  });

  /**
   * `entity.action.show` is a template shared by four entity lists, and plurality has to live in
   * exactly one of the two halves. English puts it in the label — `Configs`, `Flows`,
   * `Resource Authorities` — behind a bare `Show {{otherEntity}}`. Persian had put it in *both*:
   * the template carried a trailing `ها` and three of the four labels were already plural, so
   * `/module` rendered `نمایش تنظیم ها ها`, with the suffix twice. The remaining label,
   * `resourceAuthorities`, was singular (`مجوز منبع`) and leaned on the template's suffix for its
   * plurality, so the two faults were load-bearing on each other and had to be fixed together.
   *
   * The call sites are read out of the component templates rather than listed here, so a fifth one
   * is covered the day it is added.
   */
  describe('entity.action.show — plurality lives in the label, once', () => {
    const APP = join(process.cwd(), 'src/main/webapp/app');
    const templates = readdirSync(APP, { recursive: true })
      .map(entry => String(entry))
      .filter(entry => entry.endsWith('.component.html'))
      .map(entry => join(APP, entry));

    /** `otherEntity: ('medPortalApp.module.configs' | translate)` next to the jhiTranslate key. */
    const CALL_SITE = /jhiTranslate="entity\.action\.show"[\s\S]{0,300}?otherEntity:\s*\('([^']+)'\s*\|\s*translate\)/g;

    const sites: { template: string; key: string }[] = [];
    let usages = 0;
    for (const template of templates) {
      const html = readFileSync(template, 'utf8');
      usages += (html.match(/jhiTranslate="entity\.action\.show"/g) ?? []).length;
      for (const match of html.matchAll(CALL_SITE)) {
        sites.push({ template: template.slice(APP.length + 1), key: match[1] });
      }
    }

    /** Flat lookup across every bundle in a language, since the labels live in per-entity files. */
    const bundle = (lang: string): Record<string, string> =>
      files.reduce<Record<string, string>>((into, file) => Object.assign(into, read(lang, file)), {});

    const render = (lang: string, key: string): string =>
      bundle(lang)['entity.action.show'].replace(new RegExp(NGX_TEMPLATE_MATCHER.source, 'g'), (substring, name) =>
        name === 'otherEntity' ? bundle(lang)[key] : substring,
      );

    it('finds every call site, so the assertions below cannot pass on an empty list', () => {
      // If the templates are reformatted so the regex stops matching, this fails rather than
      // quietly reducing the suite to nothing.
      expect({ extracted: sites.length, usages }).toEqual({ extracted: usages, usages });
      expect(sites.length).toBeGreaterThanOrEqual(4);
    });

    it('keeps the Persian template free of a plural suffix of its own', () => {
      // The regression this guards: re-adding `ها` after the placeholder. English is the shape to
      // match — the template contributes the verb and nothing else.
      expect(read('fa', 'global.json')['entity.action.show']).toBe('نمایش {{otherEntity}}');
      expect(read('en', 'global.json')['entity.action.show']).toBe('Show {{otherEntity}}');
    });

    it.each([
      ['medPortalApp.module.configs', 'نمایش تنظیم\u200Cها'],
      ['medPortalApp.product.flows', 'نمایش فلوها'],
      ['medPortalApp.resource.resourceAuthorities', 'نمایش مجوز\u200Cهای منبع'],
      ['medPortalApp.medAuthority.resourceAuthorities', 'نمایش مجوز\u200Cهای منبع'],
    ])('renders %s as the Persian text a user actually reads', (key, expected) => {
      expect(sites.map(site => site.key)).toContain(key);
      expect(render('fa', key)).toBe(expected);
    });

    it.each(sites)('$template renders $key with the plural marked exactly once', ({ key }) => {
      const rendered = render('fa', key);
      const label = bundle('fa')[key];
      // 1. No doubled suffix, joined or spaced. This is the exact string the old template produced.
      expect({ key, doubled: new RegExp(`ها[${ZWNJ} ]?ها`).test(rendered) }).toEqual({ key, doubled: false });
      // 2. The label still carries the plural itself, bound to its host. Without this, dropping the
      //    template's suffix would silently de-pluralise `resourceAuthorities` back to `مجوز منبع`.
      expect({ key, label, plural: new RegExp(`[؀-ۿ${ZWNJ}]ها`).test(label) }).toEqual({ key, label, plural: true });
      // 3. The slot filled: no braces survive into the rendered text.
      expect(rendered).not.toContain('{{');
    });
  });
});
