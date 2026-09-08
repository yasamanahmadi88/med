import eslint from '@eslint/js';
import angular from 'angular-eslint';
import { defineConfig } from 'eslint/config';
import prettier from 'eslint-plugin-prettier/recommended';
import globals from 'globals';
import tseslint from 'typescript-eslint';
// For a detailed explanation, visit: https://github.com/angular-eslint/angular-eslint/blob/main/docs/CONFIGURING_FLAT_CONFIG.md
// jhipster-needle-eslint-add-import - JHipster will add additional import here

export default defineConfig(
  {
    languageOptions: {
      globals: {
        ...globals.node,
      },
    },
  },
  { ignores: ['src/main/docker/', 'src/main/webapp/404.html', 'src/main/webapp/index.html', 'src/main/webapp/swagger-ui/**', 'src/main/webapp/content/**'] },
  { ignores: ['target/classes/static/', 'target/', 'dist/'] },
  // Nested checkouts. `npm run lint` is `eslint .`, so anything the linter can reach it will try
  // to parse against `parserOptions.project` — and a worktree or scratch clone under the repo is
  // a second copy of every source file, none of which belongs to this checkout's tsconfigs. A
  // clean CI checkout has no such directory, so this only ever matters on a contributor machine:
  // measured here, `eslint .` is clean without one and reports 1057 errors with one present.
  { ignores: ['.claude/'] },
  eslint.configs.recommended,
  {
    files: ['**/*.{js,cjs,mjs}'],
    rules: {
      'no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    },
  },
  {
    files: ['src/main/webapp/**/*.ts'],
    extends: [...tseslint.configs.strictTypeChecked, ...tseslint.configs.stylistic, ...angular.configs.tsRecommended],
    languageOptions: {
      globals: {
        ...globals.browser,
      },
      parserOptions: {
        project: ['./tsconfig.app.json', './tsconfig.spec.json'],
      },
    },
    processor: angular.processInlineTemplates,
    rules: {
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'jhi',
          style: 'kebab-case',
        },
      ],
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'jhi',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/relative-url-prefix': 'error',
      '@typescript-eslint/consistent-type-definitions': 'off',
      '@typescript-eslint/explicit-module-boundary-types': 'off',
      '@typescript-eslint/no-confusing-void-expression': 'off',
      '@typescript-eslint/no-empty-object-type': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-extraneous-class': 'off',
      '@typescript-eslint/no-misused-spread': 'off',
      '@typescript-eslint/no-floating-promises': 'off',
      '@typescript-eslint/no-non-null-assertion': 'off',
      '@typescript-eslint/no-shadow': ['error'],
      '@typescript-eslint/no-unnecessary-type-arguments': 'off',
      '@typescript-eslint/no-unsafe-argument': 'off',
      '@typescript-eslint/no-unsafe-assignment': 'off',
      '@typescript-eslint/no-unsafe-call': 'off',
      '@typescript-eslint/no-unsafe-member-access': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      '@typescript-eslint/restrict-template-expressions': ['error', { allowNumber: true }],
      '@typescript-eslint/unbound-method': 'off',
      '@angular-eslint/prefer-standalone': 'off',
      '@angular-eslint/prefer-inject': 'off',
      '@typescript-eslint/no-deprecated': 'off',
      '@typescript-eslint/consistent-indexed-object-style': 'off',
      '@typescript-eslint/consistent-generic-constructors': 'off',
      '@typescript-eslint/unified-signatures': 'off',
      '@typescript-eslint/no-unnecessary-condition': 'off',
      '@typescript-eslint/prefer-nullish-coalescing': 'off',
      '@typescript-eslint/prefer-optional-chain': 'off',
      '@typescript-eslint/explicit-function-return-type': 'off',
      '@typescript-eslint/array-type': 'off',
      '@typescript-eslint/no-invalid-void-type': 'off',
      '@typescript-eslint/no-unsafe-return': 'off',
      'eqeqeq': 'off',
      'no-useless-escape': 'off',
      '@typescript-eslint/no-redundant-type-constituents': 'off',
      '@typescript-eslint/no-unnecessary-type-conversion': 'off',
      '@typescript-eslint/member-ordering': 'off',
      '@typescript-eslint/await-thenable': 'off',
      '@typescript-eslint/use-unknown-in-catch-callback-variable': 'off',
      // Both of these are switched back off by the `prettier` block at the end of this file
      // (`eslint-plugin-prettier/recommended` sets `curly: 0` and `arrow-body-style: 0`), so they
      // do not currently fire. They are kept as the declared intent: `arrow-body-style` has no
      // violations, and `curly` has 21 across 8 files, all auto-fixable. Re-asserting either one
      // after the prettier block is what would turn it on.
      'arrow-body-style': 'error',
      curly: 'error',
      'guard-for-in': 'error',
      'no-bitwise': 'error',
      'no-caller': 'error',
      'no-console': ['error', { allow: ['warn', 'error'] }],
      'no-eval': 'error',
      'no-labels': 'error',
      'no-new': 'error',
      'no-new-wrappers': 'error',
      'object-shorthand': ['error', 'always', { avoidExplicitReturnArrows: true }],
      radix: 'error',
      'spaced-comment': ['warn', 'always'],
    },
  },
  {
    files: ['src/main/webapp/**/*.spec.ts'],
    rules: {
      '@typescript-eslint/no-empty-function': 'off',
    },
  },
  {
    // Hand-written ambient shims describing bpmn-js/diagram-js internals, which ship no types of
    // their own. They model untyped JS callbacks as `Function` and mirror the libraries' generic
    // signatures, so these two rules fire throughout without pointing at anything fixable.
    // Scoped to the declaration files: the editor's own code is linted under the full rule set.
    files: ['src/main/webapp/app/bpmn-editor/types/**/*.d.ts'],
    rules: {
      '@typescript-eslint/no-unsafe-function-type': 'off',
      '@typescript-eslint/no-unnecessary-type-parameters': 'off',
    },
  },
  {
    // The palette providers, element factory and renderers are carried over from the upstream
    // open-source editor and drive bpmn-js/diagram-js internals that ship no types. These rules
    // fire on that house style — `a && b()` guards, labels built by concatenating `any` values,
    // a `@ts-ignore` over the didi `$inject` statics — rather than on defects. Correctness rules
    // stay on, and the editor's Angular components are linted under the full rule set.
    files: ['src/main/webapp/app/bpmn-editor/additional-modules/**/*.ts'],
    rules: {
      '@typescript-eslint/ban-ts-comment': 'off',
      '@typescript-eslint/no-shadow': 'off',
      '@typescript-eslint/no-unnecessary-type-parameters': 'off',
      '@typescript-eslint/no-unused-expressions': 'off',
      '@typescript-eslint/restrict-plus-operands': 'off',
    },
  },
  {
    files: ['src/main/webapp/**/*.html'],
    extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
    rules: {
      '@angular-eslint/template/click-events-have-key-events': 'off',
      '@angular-eslint/template/interactive-supports-focus': 'off',
      '@angular-eslint/template/prefer-control-flow': 'off',
      '@angular-eslint/template/eqeqeq': 'off',
      '@angular-eslint/template/label-has-associated-control': 'off',
      '@angular-eslint/template/alt-text': 'off',
      '@angular-eslint/no-empty-lifecycle-method': 'off',
    },
  },
  {
    // The Playwright suite. It runs in Node, not the browser, and is type-aware-linted against
    // `e2e/tsconfig.json` — the same tsconfig `npm run typecheck:e2e` uses, so lint and typecheck
    // see the same program.
    //
    // The rule set is the app's (`strictTypeChecked` + `stylistic`) with far fewer exemptions than
    // `src/main/webapp` takes: the whole `no-unsafe-*` family, `no-explicit-any`,
    // `no-unnecessary-condition`, `prefer-nullish-coalescing`, `prefer-optional-chain`,
    // `no-confusing-void-expression` and `no-misused-spread` are all left on here, and each
    // finding they produced was fixed in the specs rather than switched off.
    //
    // `no-floating-promises` in particular stays on and is the reason this block earns its keep:
    // an un-awaited `expect(...)` in a Playwright spec is an assertion that never runs, and the
    // suite passes anyway. The webapp block turns that rule off; this one must not.
    files: ['e2e/**/*.ts'],
    extends: [...tseslint.configs.strictTypeChecked, ...tseslint.configs.stylistic],
    languageOptions: {
      globals: {
        ...globals.node,
      },
      parserOptions: {
        project: ['./e2e/tsconfig.json'],
      },
    },
    rules: {
      // Playwright specs reach for `!` where a null means the test should already have failed:
      // `(await locator.boundingBox())!` on an element a preceding assertion proved visible, and
      // `document.querySelector(...)!` inside `page.evaluate`. Guarding each one turns a loud
      // TypeError into a longer spec that is no safer. 61 sites; also off for the webapp.
      '@typescript-eslint/no-non-null-assertion': 'off',
      // Matches the webapp: this codebase writes object shapes as `type` aliases throughout, and
      // the e2e fixtures follow it. Purely stylistic, no coverage lost.
      '@typescript-eslint/consistent-type-definitions': 'off',
      // Same option the webapp block uses. `${index + 1}` in a test title is the intended use.
      '@typescript-eslint/restrict-template-expressions': ['error', { allowNumber: true }],
      // Debug logging must not survive into a committed spec; `warn`/`error` stay available.
      'no-console': ['error', { allow: ['warn', 'error'] }],
      // Stated explicitly rather than inherited, so that a later edit to the shared rule set
      // cannot switch it off here without someone deleting this line on purpose.
      '@typescript-eslint/no-floating-promises': 'error',
    },
  },
  // ---------------------------------------------------------------------------------------------
  // What the deleted `.eslintrc.json` enabled that this config does not. ESLint 10 could not read
  // that file, so none of these were being enforced — deleting it changed no behaviour. Recorded
  // so the next reader does not have to re-derive it. Violation counts measured over
  // `src/main/webapp` and `e2e` by enabling each rule temporarily:
  //
  //   eqeqeq                                          4    best follow-up candidate
  //   @typescript-eslint/prefer-optional-chain         5
  //   @typescript-eslint/prefer-nullish-coalescing    29
  //   @typescript-eslint/no-unnecessary-condition     30
  //   @typescript-eslint/explicit-function-return-type 60
  //   @typescript-eslint/member-ordering               — declared twice in this file and already
  //                                                      overridden to 'off' by the later entry
  //   @typescript-eslint/ban-types, no-parameter-properties  removed from typescript-eslint v8
  //   curly, arrow-body-style                          switched off by the `prettier` block below
  //
  // `eqeqeq` is not enabled here on purpose. Two of its four sites are behavioural rather than
  // cosmetic — `Renderer/utils.ts` compares a moddle property against a value of unknown type,
  // where a loose comparison may be deliberate, and `flow-update.component.ts` compares two
  // possibly-undefined names. Tightening those is a change with its own risk and deserves its own
  // commit, not a rider on a lint-config cleanup.
  // ---------------------------------------------------------------------------------------------
  // jhipster-needle-eslint-add-config - JHipster will add additional config here
  {
    extends: [prettier],
  },
);
