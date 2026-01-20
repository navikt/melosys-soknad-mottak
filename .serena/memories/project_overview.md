# melosys-soknad-mottak

- Purpose: Spring Boot Kotlin service that ingests Altinn queue messages for applications about social security affiliation (utsendte arbeidstakere), stores XML/attachments, orders a PDF representation, and publishes a Kafka event when ready.
- Layout: Maven multi-module project.
  - Root `pom.xml` (packaging `pom`) sets Java 21/Kotlin 2.2, Spring Boot 3.5.7, dependency mgmt.
  - Modules: `mottak` (main service), `altinn-download-queue-external`, `altinn-correspondence-agency-external-basic`, `altinn-soknad-skjema` (Altinn SOAP/schema client libs bundled in repo).
- Main app: `mottak/src/main/kotlin/no/nav/melosys/soknadmottak/Application.kt` with `runApplication` entrypoint.
- Config: default `application.yml` uses prod profile; `application-local.yml` wires local Postgres + dummy creds, custom Kafka topic/ports. Secrets provided via env vars (see `application.yml`).
- Runtime: Dockerfile currently based on `ghcr.io/navikt/baseimages/temurin:17` and copies `mottak/target/melosys-soknad-mottak.jar`.
- Deployment: NAIS manifests (`nais.yaml`, `nais-dev.json`, `nais-prod.json`). GH Actions build with JDK 21, Maven, and publish Docker image to NAIS registry before deploy.
- Notable dependencies: Spring Boot (web/webflux/data-jpa/actuator), Spring Kafka, Jackson XML, coroutines, JPA, Flyway, Testcontainers (Postgres), Kotest/MockK, token support, Altinn client libs.