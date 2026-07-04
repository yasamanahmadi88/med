# MedPortal

JHipster-based medical portal application.

## Stack

| Component | Version |
|-----------|---------|
| Java (JDK) | 25 |
| Spring Boot | 4.0.6 |
| JHipster | 9.1.0 |
| Angular | 21 |
| Node.js | 24+ |

## Prerequisites

- JDK 25
- Node.js 24.16.0 or later
- Maven 3.9+ (or use `./mvnw`)
- Oracle Database (production/dev profiles)

## Development

```bash
# Backend only
./mvnw -Dskip.installnodenpm -Dskip.npm

# Frontend dev server
npm install
npm start

# Full build
./mvnw -Pprod verify
```

## Upgrade notes (JHipster 7.9 / JDK 11 / Angular 14 → JHipster 9 / JDK 25 / Angular 21)

- Build files (`pom.xml`, `package.json`, `angular.json`, webpack, etc.) were added based on JHipster 9.1.
- Java `javax.*` APIs migrated to `jakarta.*`.
- Spring Security updated to lambda DSL (`authorizeHttpRequests`, `requestMatchers`).
- Angular dependencies upgraded to v21; NgModule-based structure retained with `zone.js` for compatibility.
- Oracle JDBC via `ojdbc11` from Maven Central.
