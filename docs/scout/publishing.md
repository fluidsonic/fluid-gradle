# Publishing: two credential paths and the JVM-only javadoc jar

Operational wiring for publishing — where each repository is configured, which environment variables/properties gate it, and a hard assumption in javadoc attachment.

Two independent credential paths:
- **Sonatype/Nexus** is configured at the **root** project (`configurePublishing` in `sources/LibraryConfigurator.kt`) and activates only if **both** `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` environment variables are set. Endpoints are the migrated central ones: `nexusUrl` = `https://ossrh-staging-api.central.sonatype.com/service/local/`, snapshots = `https://central.sonatype.com/repository/maven-snapshots/`.
- **GitHub Packages** is configured **per module** (`configurePublishing` in `sources/LibraryModuleConfigurator.kt`) and activates if `GITHUB_PACKAGES_AUTH_TOKEN` is set as an environment variable **or** a Gradle property. The Maven `username` is the literal dummy string `"unused"`; the token is the password.

The plugin's own `build.gradle.kts` duplicates this exact dual-credential logic for self-publishing.

Javadoc trap: when a JVM target exists, `configurePublishing` (module) registers a `javadocJar` sourced from the Dokka v2 task `dokkaGeneratePublicationHtml` (skipped if `noDokka`) and attaches it via `publications.filterIsInstance<MavenPublication>().single { it.name == "jvm" }`. The `.single { }` **throws** if there is not exactly one publication named `jvm`. Only the JVM publication receives a javadoc artifact.

dokka version: 2.2.0 (as of 2026-07, `gradle/libs.versions.toml`).
