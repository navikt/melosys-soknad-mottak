# Suggested Commands

- Start local Postgres (needed for app + integration tests): `docker run -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=su -d --rm postgres` (or adjust creds to match `application-local.yml`).
- Full test suite (CI equivalent): `mvn --settings .github/maven-settings.xml verify`.
- Build jar (used by CI before Docker): `mvn --settings .github/maven-settings.xml clean package`.
- Run service locally with local profile: `mvn -pl mottak -am spring-boot:run -Dspring-boot.run.profiles=local` (ensure Postgres running; configure Kafka/Altinn/test URLs via env or `application-local.yml`).
- Package & run directly: `mvn -pl mottak package && java -jar mottak/target/melosys-soknad-mottak.jar --spring.profiles.active=local`.
- Docker build (uses Dockerfile at repo root): `docker build -t melosys-soknad-mottak .` (base image currently temurin 17).
- Common tooling: use `rg` for searching, `mvn -pl <module> test` for scoped tests, `mvn dependency:tree` for dependency debugging.