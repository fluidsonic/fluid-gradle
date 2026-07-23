# Internal Versions object and the unstable-version filter

Two version-handling details that affect consumers, not just this project's own build.

**Versions object propagates to consumers.** `sources/Versions.kt` (`Versions`) hardcodes `gradle` and `junit`. `LibraryConfigurationBuilder.build()` (`sources/LibraryConfigurationBuilder.kt`) defaults its `gradleVersion` to `Versions.gradle` unless a consumer overrides it via `gradleVersion(...)`. So bumping the `gradle` constant changes the Gradle **wrapper** version enforced on every library that applies this plugin (the `wrapper {}` block in `sources/LibraryConfigurator.kt`), not merely this repo's own build. Current: `gradle = "9.6.1"`, `junit = "6.1.2"` (as of 2026-07).

**Unstable-version filter has a single-letter `m` footgun.** `isUnstableVersion` in `sources/utility/Project.kt` uses the regex `\b(alpha|beta|dev|eap|m|rc|snapshot)\d*\b` (case-insensitive). The bare `m` alternative intentionally catches Kotlin milestone tags like `-m1`/`-M1`, but will also match any standalone `m`/`mN` token in another version scheme. The doc comment only says "alpha, beta, RC, etc." — it does not mention `m`. Pinned by `tests/io/fluidsonic/gradle/IsUnstableVersionTest.kt` (`mVersions_returnTrue`).

The `dependencyUpdates` `rejectVersionIf` (in `sources/LibraryConfigurator.kt`) rejects a candidate only when it is unstable AND the current version is not — a deliberate asymmetry. Consumers opt into unstable candidates via `dependencyUpdatesIncludeUnstableVersions()`. The identical regex is duplicated for the plugin's own build in `buildSrc` (`VersionStability.isUnstableVersion`, package `io.fluidsonic.gradle.buildsupport`); this duplication is structural, not accidental — see `buildsrc-duplication.md`.
