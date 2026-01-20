# Task Completion Checklist

- Run tests: `mvn --settings .github/maven-settings.xml verify` (or narrower `mvn -pl mottak test` if scoped). Ensure Postgres/Testcontainers prerequisites are met.
- For code changes affecting build/deploy: `mvn clean package` to confirm jar builds; optionally `docker build` if touching Dockerfile/runtime.
- Update configuration/env docs if new secrets or app properties are introduced (keep prod values out of repo). Adjust `application-local.yml` when adding new config knobs.
- Validate Spring profiles used by change (default prod vs local). Confirm new Flyway migrations apply cleanly.
- If touching GitHub workflows/NAIS manifests, note expected image tag and cluster vars; ensure version bumps align with Java 21 baseline.
- Summarize changes and any manual steps (DB migrations, feature flags, external deps) in PR description.