# Deferred remediations: security review

Four findings from the full-project security review are **not** being fixed in code right now.
Each of them needs a decision that an engineer cannot make alone — new infrastructure, a
product trade-off, or a change to how the app is deployed. Everything else the review turned
up (unbounded in-memory maps, raw bearer tokens in the session cache, double signature
verification, `permitAll` gaps, Docker port exposure, remember-me window mismatch, and the
rest) was fixed on this branch.

This document exists so the owner can decide. Read the section, pick an option, file the work.

Line numbers were taken from branch `claude/full-project-review-zx15jv` while other fixes were
landing concurrently. Symbol names are stable; re-check the exact line before quoting it
elsewhere.

## Summary

| # | Item | Severity | Blocked on | Recommendation |
| - | ---- | -------- | ---------- | -------------- |
| 1 | Session, rate-limit and captcha state is per-JVM | High for HA, none for single node | Whether the app will ever run on more than one node | Sticky sessions now; Redis when a second node is actually planned |
| 2 | Every authenticated request runs a burst of DB queries | Medium (performance), low (security) | Accepting a bounded staleness window on permission changes | Fix the N+1 first (no decision needed); then cache authorities behind an `auth_version` claim |
| 3 | The XSS filter covers a path this API never uses | Medium | Which string fields are allowed to be HTML | Do not sanitize DTOs. Sanitize in `TranslateDirective`, the one unescaped sink |
| 4 | CSP allows `script-src 'unsafe-inline'` | Medium | Losing Angular's critical-CSS inlining, or building an HTML-rewriting filter | Disable `inlineCritical`, externalise the two inline scripts, then drop `'unsafe-inline'` |

---

## 1. Session, rate-limit and captcha state lives in one JVM

### What the code does today

`SecurityCache` keeps both the server-side session table and the per-login rate limiters in
plain `ConcurrentHashMap`s:

- `src/main/java/com/behsa/medportal/security/SecurityCache.java:99` — `Map<String, SessionInfo> sessionInfos`
- `src/main/java/com/behsa/medportal/security/SecurityCache.java:100` — `Map<String, LoginBucketEntry> loginBuckets`

`JWTFilter` treats the presence of a session in that map as a precondition for
authentication. A valid, correctly signed, unexpired token with no matching session entry is
rejected and the request continues unauthenticated
(`src/main/java/com/behsa/medportal/security/jwt/JWTFilter.java:63-70`). Rate limiting is
per-session and therefore also per-node
(`src/main/java/com/behsa/medportal/security/jwt/JWTFilter.java:109-130`).

Captcha state is the same shape:
`src/main/java/com/behsa/medportal/security/captcha/LocalCaptchaService.java:32` holds
`Map<String, Entry> store`, written by `issue()` (line 42), read by `renderPng()` (line 55)
and consumed by `verifyAndConsume()` (line 78).

### Why it matters

The token is verifiable on any node, but the session is not. Consequences, in order of how
soon you hit them:

- **Restart logs everyone out.** A rolling deploy invalidates every live session, including
  30-day remember-me sessions. This is true today, on one node.
- **Two nodes without sticky sessions is unusable.** A user authenticated on node A gets a
  401 from node B and the front end drops them at the login screen.
- **The captcha flow spans three requests.** `POST /api/captcha-endpoint`,
  `GET /api/captcha.png` and `POST /api/authenticate` are three round trips
  (`src/main/java/com/behsa/medportal/config/SecurityConfiguration.java:181-182`). Land them
  on different nodes and the captcha id is unknown on the second and third. Login fails, and
  it fails in a way that looks intermittent.
- **Login rate limits divide by the node count.** Ten attempts per minute per node, so N
  nodes means 10N attempts per minute against a single account.

A concurrent fix bounded both maps and added a scheduled sweep, so unbounded heap growth is no
longer a concern. The clustering limitation is untouched and deliberate; the class javadoc at
`SecurityCache.java:25-34` says so.

### Options

**(a) Sticky sessions at the load balancer.** Configure source-IP or cookie affinity.

- Buys: multi-node works for the common case. No code change, no new dependency.
- Costs: losing a node logs out everyone it was serving. Restarts still log everyone out.
  Rate limits are still per-node. Uneven load — a busy client pins to one node. Cookie
  affinity needs a cookie the API client actually sends; the front end authenticates with a
  bearer header, so source-IP affinity is the likely mechanism, which breaks behind a
  corporate NAT that collapses many users to one address.
- Effort: hours, in load-balancer config. No repo change.

**(b) Shared store (Redis) for sessions, buckets and captcha.** Move all three maps behind an
interface with a Redis implementation. Bucket4j already ships a Redis-backed distributed
proxy, so the rate limiters port fairly directly. Sessions and captcha entries are small
key/value records with a natural TTL, so they map cleanly.

- Buys: any node serves any request. Sessions survive node loss. Global rate limits.
  Server-side revocation stays exactly as it is today.
- Costs: a new production dependency that must be HA itself, or its outage becomes a total
  auth outage — every request needs a session lookup. Someone has to own it: provisioning,
  TLS, credentials, backup policy, monitoring. Adds a network hop to the hot path of every
  authenticated request.
- Effort: roughly 3-5 days of code (extract interfaces, Redis implementations, config,
  tests) plus whatever the infrastructure team needs to stand up Redis.

**(c) Genuinely stateless JWT.** Delete the server-side session entirely; trust the signed
token.

- Buys: no shared store, no per-request lookup, trivially horizontal.
- Costs: this deletes the revocation story. Today logout, an admin kick, and account
  deactivation all take effect immediately because the session row is removed. Stateless
  tokens are valid until they expire — 24 hours, or 30 days with remember-me. Recovering
  revocation means short access tokens plus refresh tokens plus a revocation list, which is
  a shared store again, just a smaller one. The concurrent-session check
  (`SecurityCache.hasConcurrentSession`) and the admin session list also disappear.
- Effort: 1-2 weeks including front-end refresh handling. Do not do this to solve clustering.

### Recommendation

Do **(a)** if and only if a second node is imminent, and treat it as a stopgap. Plan **(b)**
as the real fix, and do it at the moment the second node is actually funded — not before, and
not by half. Reject **(c)** as a clustering solution; it is an authentication redesign wearing
a scalability costume, and item 2 is a better reason to revisit the token design than this
one.

**Open question:** nobody has stated whether this app is meant to be clustered at all. If the
answer is "one node forever, restarts are announced", all three options are wasted work and
the correct action is to write that down and close the finding.

---

## 2. Every authenticated request runs several database queries

### What the code does today

`TokenProvider.getAuthentication` rebuilds the principal from the database on every request
(`src/main/java/com/behsa/medportal/security/jwt/TokenProvider.java:166`), called from
`JWTFilter` at `src/main/java/com/behsa/medportal/security/jwt/JWTFilter.java:78`:

1. `userRepository.findOneWithAuthoritiesByLoginAndActivatedTrue(login)` —
   `TokenProvider.java:169-171`. This method has an `@EntityGraph` but, unlike its sibling
   `findOneWithAuthoritiesByLogin`, **no `@Cacheable`**
   (`src/main/java/com/behsa/medportal/repository/UserRepository.java:31` vs `:34`). One
   query, uncached, per request.
2. `fetchResourceAuthorities(authorities)` — `TokenProvider.java:180`, delegating to
   `ResourceAuthorityQueryService.findByAuthorities`
   (`src/main/java/com/behsa/medportal/service/ResourceAuthorityQueryService.java:85`).

The framing that started this review said "a DB round-trip plus an authority-resolution
query". It is worse than that. `findByAuthorities` does:

- `medAuthorityRepository.findByNameIn(...)` — one query (line 91)
- `resourceAuthorityRepository.findByMedAuthority_IdIn(...)` — one query (line 96)
- then, **per returned row**, `medAuthorityService.findOne(...)` and
  `resourceService.findOne(...)` (lines 99-108), each a `findById` against the database
  (`src/main/java/com/behsa/medportal/service/impl/MedAuthorityServiceImpl.java:78-81`,
  `src/main/java/com/behsa/medportal/service/impl/ResourceServiceImpl.java:74-77`).

That is a textbook N+1, and it re-fetches entities that the page query already loaded —
`ResourceAuthorityEntity.medAuthority` and `.resource` are `@ManyToOne` with the default
`EAGER` fetch (`src/main/java/com/behsa/medportal/domain/ResourceAuthorityEntity.java:30-40`),
so they are in the persistence context before the loop runs. The loop's only real effect is
to replace the mapped DTO with an identically-mapped DTO.

Hibernate's first-level cache absorbs repeated ids within the one read-only transaction, so
the cost scales with *distinct* ids rather than row count. With the seed data
(`src/main/resources/config/liquibase/data/resource_authority_seed.csv`: 48 rows and 12
distinct resources per role) a single-role user costs roughly **15-16 queries per
authenticated request**. It grows linearly with the size of the resource catalogue, not with
anything the user did.

### Why it matters

- Throughput: an idle-looking page that fires six parallel API calls costs ~90 queries.
- The performance argument for JWT is gone. The app pays the database cost of a session
  lookup *and* carries the revocation weakness of a bearer token.
- It is a cheap amplification target. Any authenticated caller turns one HTTP request into a
  dozen-plus queries; the per-session bucket4j limits bound it, but the multiplier is real.

Correctness-wise the current design is the *good* behaviour: authorities are read fresh, so a
permission change or a deactivation takes effect on the next request. Any caching gives that
up, which is exactly the trade the team has to choose.

### Options

**(0) Fix the N+1 first. This needs no decision.** Delete the `findOne` loop at
`ResourceAuthorityQueryService.java:99-108` — it re-fetches already-loaded entities — and make
the mapper populate the DTO from the eagerly-loaded associations, or use an explicit
`JOIN FETCH`. That takes the per-request cost from ~15 queries to 2 with no behaviour change
and no staleness. It was left out of this branch only because it touches a shared query
service other work is in flight on. **Half a day. Do this regardless of what is decided
below.**

**(a) Leave it at 2 queries per request.** After (0), the remaining cost is one user lookup
and one authority lookup. For a portal with tens to low hundreds of concurrent users this is
probably fine, and permission changes stay instant.

- Buys: zero staleness, no new mechanism.
- Costs: 2 queries per request forever; the database is on the critical path of every call.
- Effort: none beyond (0).

**(b) Cache authorities per user, with an `auth_version` claim.** Mint tokens with an
`auth_version` claim; keep a per-user counter that any permission or activation change
increments; on each request compare the claim against the cached counter and rebuild
authorities only on mismatch or cache miss.

- Buys: authenticated requests become memory-only in the common case. Permission changes
  still take effect immediately for the affected user, because their counter moved.
- Costs: the counter lookup must itself be cheap and correct. In a single JVM that is a
  Caffeine cache; across nodes it is the same shared-store problem as item 1, so this
  couples to that decision. Also more moving parts to get wrong: miss the increment on one
  admin code path and a revoked permission silently persists until the token expires.
- Effort: 2-3 days single-node. Materially more if it has to be cluster-correct.

**(c) Put authorities in the token and stop resolving them.** Simplest and fastest; the
`auth` claim is already there.

- Buys: no queries at all.
- Costs: a permission change does nothing until the token expires — up to 30 days with
  remember-me. Deactivating a user stops meaning anything, which is a real regression: the
  `AndActivatedTrue` in the current query is the mechanism that locks out a disabled account.
  Not acceptable without short token lifetimes and refresh.
- Effort: 1 day to do, months to regret.

### Recommendation

Do **(0)** now. Then measure. Two queries per authenticated request is a defensible resting
place, and unless load testing says otherwise, **(a)** is the answer — carrying the
complexity of **(b)** for a saving that has not been shown to matter is a bad trade. Revisit
**(b)** if and when the Redis decision in item 1 is made, since it needs the same store.

Reject **(c)**.

---

## 3. The XSS input filter does not cover the path this application uses

### What the code does today

`XssSanitizingFilter` wraps `POST`/`PUT`/`PATCH` requests in an `XssRequestWrapper`
(`src/main/java/com/behsa/medportal/filter/XssSanitizingFilter.java:22-29`) with an OWASP
policy of `Sanitizers.FORMATTING.and(Sanitizers.LINKS)` (line 14). The wrapper overrides
exactly three methods — `getParameter`, `getParameterValues`, `getParameterMap`
(`src/main/java/com/behsa/medportal/filter/XssRequestWrapper.java:18-47`).

This is a REST API. Every write endpoint binds a JSON body with `@RequestBody`, which Jackson
deserialises straight from the request input stream and which never touches `getParameter*`:
`src/main/java/com/behsa/medportal/web/rest/FlowResource.java:87,126,167`,
`src/main/java/com/behsa/medportal/web/rest/ProductResource.java:71,98,135`, and 13 other
controllers under `src/main/java/com/behsa/medportal/web/rest/`.

**No DTO field is sanitized by this filter.** The two things it does reach are:

- Form-encoded bodies, of which this application has none.
- Query parameters — but only on `POST`/`PUT`/`PATCH`, because the filter passes `GET`
  through unwrapped (line 27). Query parameters are overwhelmingly a `GET` concern
  (JHipster criteria filters, pagination), so the one input class the wrapper *can* handle is
  the one it skips.

The filter is therefore approximately a no-op that reads as a control. Two smaller notes on
the implementation, for whoever eventually touches it:

- `getParameterMap` mutates the `String[]` values of the container's own parameter map in
  place (`XssRequestWrapper.java:41-45`) rather than returning a copy. That writes through to
  Tomcat's cached parameters.
- `Sanitizers.FORMATTING` is an HTML policy. Applied to a non-HTML string it does not escape,
  it *rewrites* — `a < b` comes back altered. Using it on arbitrary scalar input is wrong
  even where it is reached.

`owasp-java-html-sanitizer` 20240325.1 is already a dependency (`pom.xml:355-359`), so no new
dependency is needed for any option below.

### Where output actually escapes, and where it does not

This is the part that decides the fix. Angular escapes `{{ }}` interpolation, and every entity
field in this app renders through interpolation — `flow-detail.component.html:23`,
`resource.component.html:76-77`, `product-detail.component.html:23`, and so on. Those are
safe and need nothing.

There are exactly three constructs in the front end that write HTML:

| Sink | Sanitized? |
| ---- | ---------- |
| `src/main/webapp/app/shared/alert/alert.component.html:4` — `[innerHTML]="alert.message"` | Yes — `alert.service.ts:68` runs `DomSanitizer.sanitize(SecurityContext.HTML, …)` before the message is stored |
| `src/main/webapp/app/shared/alert/alert-error.component.html:4` — same binding | Yes — same path, via `AlertErrorComponent.addErrorAlert` → `AlertService.addAlert` |
| `src/main/webapp/app/shared/language/translate.directive.ts:50` — `this.el.nativeElement.innerHTML = value` | **No** |

`TranslateDirective` assigns the ngx-translate output directly to `innerHTML`, and
`[translateValues]` interpolates runtime data into that string before assignment. Templates
that feed server data into it:

- `entities/module/delete/module-delete-dialog.component.html:10` — `{ name: module.moduleName }`
- `entities/flow/delete/flow-delete-dialog.component.html:10` — `{ name: flow.flowName }`
- `entities/product/delete/product-delete-dialog.component.html:13` — `{ name: product.productName }`
- `admin/user-management/delete/user-management-delete-dialog.component.html:11`,
  `admin/user-management/change-password-dialog/…:7`, `account/settings/settings.component.html:4`,
  `account/password/password.component.html:4`, `home/home.component.html:6` — all `login`

Of these, most are not exploitable by accident of validation: `flowName` is `@Size(max = 6)`
and `productName` is `@Size(max = 3)` (`FlowDTO.java:18`, `ProductDTO.java:18`), too short to
carry a payload; `login` is constrained by `Constants.LOGIN_REGEX`
(`src/main/java/com/behsa/medportal/config/Constants.java:9`), which admits no `<`.

`ModuleDTO.moduleName` is `@Size(max = 50)` with no character restriction
(`src/main/java/com/behsa/medportal/service/dto/ModuleDTO.java:15-17`), and `/api/modules`
requires only `authenticated()` — it is not in the admin-only matcher list, so it falls
through to `src/main/java/com/behsa/medportal/config/SecurityConfiguration.java:202`. Fifty
characters is comfortably enough for `<img src=x onerror=…>`. **Any authenticated user can
store a module name that executes as script in the browser of whoever opens the module delete
dialog.** That is a genuine stored-XSS path, and it is the single concrete finding behind this
item. It has not been confirmed end-to-end in a running browser — treat it as a strong
hypothesis pending a manual check, not a proven exploit.

Note where the defect lives: it is an **output-encoding** bug in one directive, not a missing
input filter. Sanitizing DTOs would have papered over it while leaving the sink open to every
other data source that reaches a translation parameter.

### DTO field inventory

Which string fields could plausibly reach an HTML rendering context, and which must never be
touched. All under `src/main/java/com/behsa/medportal/service/dto/`.

**Reaches an unescaped sink today (via `TranslateDirective`):**

| DTO | Field | Constraint | Notes |
| --- | ----- | ---------- | ----- |
| `ModuleDTO` | `moduleName` | `@Size(max = 50)` | The live risk. Writable by any authenticated user |
| `FlowDTO` | `flowName` | `@Size(max = 6)` | Length makes a payload impractical |
| `ProductDTO` | `productName` | `@Size(max = 3)` | Same |
| `AdminUserDTO` / `UserDTO` | `login` | `LOGIN_REGEX` | Regex excludes `<`; safe as long as the regex holds |

**Free-text, currently interpolation-only, would become a sink if anyone adds an
`[innerHTML]` or a `translateValues` binding:** `FlowDTO.flowDesc` (300),
`ProductDTO.productDesc` (300), `ResourceDTO.name` / `displayName` (200/300),
`MedAuthorityDTO.displayName` (500), `ModuleDTO.dnsName` (100), `InstanceDTO.hostName` (50) /
`moduleStatus` (50), `ConfigDTO.commentDesc` (200), `CustomAuditEventDTO.eventType` (255).

**Structured or machine data — must NOT be sanitized:**

| DTO | Field | Why |
| --- | ----- | --- |
| `FlowDTO` | `flow` | BPMN XML, `@NotNull` with no size cap. Fed to `bpmnModeler.importXML` at `src/main/webapp/app/bpmn-editor/components/designer/designer.component.ts:104`. An HTML sanitizer would strip or rewrite the XML and break the editor outright |
| `LogDTO` / `LogListRowDTO` | `reqMessage`, `resMessage`, `properties`, `errorDetails` | Captured wire payloads and stack traces. Mangling them destroys the only evidence an operator has |
| `InstanceDTO` | `processedStatistics` | Serialised metrics |
| `ResourceDTO` | `apiUri` | A URI. Sanitizing rewrites it and breaks authorization matching |
| `ModuleDTO` | `loggingFilter`, `redisKeyPrefix`, `defaultPort` | Configuration values with their own grammar |
| `PasswordChangeDTO` | `currentPassword`, `newPassword` | Passwords. Never transform |
| `VersionDTO` | `tableName`, `moduleName` | Identifiers |

`ConfigDTO` already shows the pattern worth following: `property` and `pValue`/`commentDesc`
carry explicit `@Pattern` constraints, with `PLAIN_TEXT_PATTERN = "^[^<>]*$"` rejecting angle
brackets outright (`src/main/java/com/behsa/medportal/service/dto/ConfigDTO.java:23,30-41`).
Rejection at validation is better than silent rewriting: the user finds out, and stored data
stays byte-identical to what was submitted.

### Options

**(a) Fix the sink. Sanitize inside `TranslateDirective`.** Run
`DomSanitizer.sanitize(SecurityContext.HTML, value)` before assigning to `innerHTML`, exactly
as `AlertService` already does.

- Buys: closes every path through that directive at once, including ones nobody has found
  yet. Cannot corrupt stored data, because nothing is rewritten on the way in.
- Costs: any translation string that legitimately contains markup loses it. Worth checking
  the i18n bundles, though JHipster's stock strings do not rely on inline HTML for anything
  load-bearing.
- Effort: a few hours including a check of the translation bundles.

**(b) Add `@Pattern` rejection to the handful of free-text name fields**, following the
`ConfigDTO` precedent. Start with `ModuleDTO.moduleName`.

- Buys: defence in depth, clear error to the user, no data mutation.
- Costs: a per-field judgement call; too aggressive a pattern breaks legitimate input.
- Effort: an hour per field, plus deciding whether existing rows would now fail validation.

**(c) Delete `XssSanitizingFilter` and `XssRequestWrapper`.** They protect nothing this
application does and imply a control that is not there.

- Buys: removes a false sense of coverage and the in-place map mutation.
- Costs: an auditor asks why the XSS filter was removed. Answer with this document.
- Effort: an hour.

**(d) Blanket-sanitize all JSON string fields** via a Jackson deserializer.

- Costs: corrupts `FlowDTO.flow` and every field in the "must not be sanitized" table above.
  Do not do this.

### Recommendation

**(a) + (b) + (c)**, in that order, as one small piece of work — roughly one day. Fix the
sink, add rejection patterns to the free-text name fields starting with `moduleName`, then
remove the filter that never covered anything, and record why.

Separately and independently of this decision, `/api/modules` write access should probably not
be `authenticated()`. That is a plain authorization gap, not a deferred one — worth raising as
its own item.

---

## 4. CSP still allows `script-src 'unsafe-inline'`

### What the code does today

`src/main/resources/config/application-prod.yml:115`:

```
content-security-policy: "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:"
```

Applied at `src/main/java/com/behsa/medportal/config/SecurityConfiguration.java:156`. Only the
prod profile overrides the value; other profiles get the JHipster library default, which is
where the `https://storage.googleapis.com` entry originally came from.

### Why `'unsafe-inline'` is there

Four distinct causes, and they matter separately because they have different fixes. From the
built output at `target/classes/static/index.html`:

1. **Two hand-written inline `<script>` blocks in the source index.** A theme bootstrap that
   reads `localStorage` before first paint (`src/main/webapp/index.html:15-28`) and a
   4-second load-failure timeout (`src/main/webapp/index.html:52-64`). Both survive into the
   build.
2. **Two `onload="this.media='all'"` attributes** on the deferred stylesheet links, emitted by
   Angular's critical-CSS inliner. Inline **event handler attributes** are the awkward case:
   a nonce cannot authorise them and a hash cannot either without also adding
   `'unsafe-hashes'`. As long as these exist, `script-src 'unsafe-inline'` is required. This
   is the detail that makes `inlineCritical` a `script-src` problem and not just a `style-src`
   one.
3. **Two inline `<style>` blocks**, also from critical-CSS inlining. The built file carries
   `data-beasties-container` on `<html>`, confirming Angular's beasties/critters step ran.
   `angular.json:43` sets `"optimization": true` for the production configuration, and
   `styles.inlineCritical` defaults to `true` under that.
4. **One inline `style="display: none"` attribute** (`src/main/webapp/index.html:43`). Same
   `'unsafe-hashes'` problem as (2), on the `style-src` side.

**`https://storage.googleapis.com` appears to be unused.** A repository-wide search finds it
only in `application-prod.yml:115` — not in `package.json`, `angular.json`, any source file,
or the built output. It is inherited from the JHipster template and can be dropped. Confirm
against any runtime config or CDN rewrite that lives outside this repository before removing
it.

### Why it matters

`script-src 'unsafe-inline'` permits any inline `<script>` and any `on*` attribute the page
ends up containing, which is precisely the payload class that XSS produces. With it present,
CSP is not an XSS control; it is a directory of allowed *external* origins. Given that item 3
identifies a live path from stored data to `innerHTML`, this is the mitigation that would
have contained it, and it is disabled.

`style-src 'unsafe-inline'` is a much smaller deal — style injection is an exfiltration and
UI-redress concern, not code execution. If only one is fixed, fix `script-src`.

### What a nonce would actually require

Worth being blunt about, because "just use a nonce" is the reflexive answer and it does not
fit this deployment.

`index.html` is a static file on the classpath, served by Spring's resource handler.
`ClientForwardController.forward` returns `"forward:/"`
(`src/main/java/com/behsa/medportal/web/rest/ClientForwardController.java:30`) — a forward to
a static resource, not a rendered template. Nothing in the request path substitutes anything
into the HTML.

A nonce is per-response by definition. To use one you would need to:

1. Add a filter that generates a nonce per request, reads `index.html`, rewrites every
   `<script>` tag to carry it, and writes the result — for `/` and every SPA route.
2. Set the same nonce in the CSP header for that response.
3. Serve `index.html` with `Cache-Control: no-store`, since a cached page carries a stale
   nonce and the browser then blocks its own scripts. Static assets are currently served with
   `cachePublic` (`src/main/java/com/behsa/medportal/config/StaticResourcesWebConfiguration.java:48-54`),
   so the caching posture has to change deliberately.
4. Still keep `'unsafe-hashes'` or eliminate the `onload` attributes, because a nonce does
   nothing for an event handler attribute.

That is real work — call it 3-4 days with the caching regression risk — for an outcome that
step 4 alone would largely achieve.

### Options

**(a) Remove the inline content, then remove `'unsafe-inline'`.**

- Set `"inlineCritical": false` under `optimization.styles` in `angular.json`'s production
  configuration. That removes the inline `<style>` blocks *and* the `onload` attributes in
  one change.
- Move the two inline scripts into a small `.js` asset loaded with `<script src>`. The theme
  bootstrap must stay render-blocking in `<head>` to avoid a flash of the wrong theme, so it
  needs to be a separate synchronous file rather than folded into the bundle.
- Replace `style="display: none"` with a class from `loading.css`.
- Then set `script-src 'self'; style-src 'self'` and drop `storage.googleapis.com`.
- Buys: a CSP that actually constrains injected script. No per-request machinery, no caching
  change, nothing to maintain.
- Costs: losing critical-CSS inlining costs one render-blocking stylesheet round trip on
  first paint. On an intranet portal that is small; measure it rather than assuming. The
  extracted theme script adds one more request, cacheable forever.
- Effort: 1-2 days including verifying nothing else in the build emits inline script, and a
  first-paint measurement before and after.

**(b) Nonce-based CSP.** As described above.

- Buys: keeps `inlineCritical`.
- Costs: a rewriting filter to own, `no-store` on the SPA entry point, and it still does not
  cover the `onload` attributes without `'unsafe-hashes'`.
- Effort: 3-4 days plus ongoing maintenance. Not worth it here.

**(c) Hash-based CSP.** Hash the inline blocks and list them in the policy.

- Costs: the hashes change on every build that touches the inlined CSS, so the policy has to
  be generated by the build and kept in lockstep with it. A mismatch is a white screen in
  production. And, again, hashes do not cover `on*` attributes without `'unsafe-hashes'`.
- Effort: 2-3 days, plus a permanent build coupling. Worse than (a).

**(d) Do nothing.** Defensible only if items 3 and its neighbours are genuinely closed, since
CSP is the second layer, not the first.

### Recommendation

**(a).** It is the cheapest option, the only one that does not add machinery to maintain, and
the only one that gets to a clean `script-src 'self'`. Removing `storage.googleapis.com` is
free and should happen in the same change.

Two things worth adding to the policy while it is being edited, neither of which is affected
by the decision above:

- `base-uri 'self'` — `base-uri` does **not** fall back to `default-src`, so it is currently
  unset, and an injected `<base>` tag can redirect every relative script URL.
- `object-src 'none'` — covered by `default-src 'self'` today, but stating it explicitly is
  conventional and costs nothing.
