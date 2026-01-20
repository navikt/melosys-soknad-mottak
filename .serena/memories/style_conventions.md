# Style & Conventions

- Language: Kotlin (official code style). Source roots are `src/main/kotlin` and `src/test/kotlin` per module.
- Spring Boot idioms: Kotlin `runApplication`, `main` in `Application.kt`; Kotlin classes are opened for Spring/JPA via kotlin-maven allopen/noarg plugins. `-Xjsr305=strict` for nullability.
- Configuration: default settings in `application.yml` (prod profile). Local overrides in `application-local.yml`; use env vars for secrets and URLs. Keep secrets out of repo.
- Testing: Spring Boot starter test (JUnit 5) plus Testcontainers Postgres, Kotest, MockK. Use the local profile or Testcontainers for integration tests.
- Logging/config files: `logback-spring.xml` in resources; keep log config there. Flyway migrations live under `src/main/resources/db`.
- Build tooling: Maven with kotlin-maven-plugin jvmTarget `${java.version}` (21). Spring Boot maven plugin builds fat jar; finalName comes from parent artifact id (`melosys-soknad-mottak`).