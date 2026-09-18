# Validating a Plugin Portal publish still needs Portal credentials, and trips signing

Operational gotcha for verifying a `com.gradle.plugin-publish` bump (the `gradle-plugin-publish` entry in `gradle/libs.versions.toml`) without actually publishing.

`./gradlew publishPlugins --validate-only` still requires Gradle Plugin Portal credentials (`gradle.publish.key`/`gradle.publish.secret`, or the matching environment variables) to run at all — `--validate-only` only skips the upload, not the authenticated Portal call that precedes it. Without them configured locally, the task fails before validating anything.

Separately, whenever `GITHUB_PACKAGES_AUTH_TOKEN` is set (see `publishing.md`), `build.gradle.kts` wires up `signing { sign(publishing.publications) }` unconditionally over every `MavenPublication` — including the two that `java-gradle-plugin` registers for this plugin (the main `pluginMaven` publication and the `io.fluidsonic.gradlePluginMarkerMavenPublication` marker). Locally, with no signatory configured, `publishPlugins` then fails on the resulting `signPluginMavenPublication` and `signIo.fluidsonic.gradlePluginMarkerMavenPublication` tasks before it can reach the Portal call at all. Skip both explicitly to get past signing:

`-x signPluginMavenPublication -x signIo.fluidsonic.gradlePluginMarkerMavenPublication`
