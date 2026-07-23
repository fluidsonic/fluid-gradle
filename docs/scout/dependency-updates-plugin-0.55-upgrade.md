# Upgrading gradle-versions-plugin to 0.55.0 (flag-free `dependencyUpdates`) — blocked, do not bump yet

The plan that removes the `--no-configuration-cache --no-parallel` flags documented in `dependency-updates-task.md`, plus why it is not yet safe to apply.

The current released version is 0.54.0 (see `gradle/libs.versions.toml`); 0.55.0 is not yet published (see the Blocked section below), so the following describes its planned behavior, not something available today. Once released, 0.55.0 will rewrite the `dependencyUpdates` task to aggregate per-project instead of from the root, and will fully support parallel execution and the configuration cache on Gradle 9 (its minimum Gradle becomes 8.4). Once on 0.55.0, a bare `./gradlew dependencyUpdates` works and the extra flags are no longer needed.

Upgrade steps, all in `gradle/libs.versions.toml`:
- Bump the `gradle-versions` version to `0.55.0`.
- Rename the plugin id in the `gradle-versions` plugin entry: `com.github.ben-manes.versions` → `io.github.ben-manes.versions`.
- Rename the module in the `gradle-versions-plugin` library entry: `com.github.ben-manes:gradle-versions-plugin` → `io.github.ben-manes:gradle-versions-plugin`.
- Leave the imports alone: the internal Kotlin package `com.github.benmanes.gradle.versions.*` is unchanged in 0.55.0, so `sources/LibraryConfigurator.kt`, `sources/utility/Project.kt`, and `build.gradle.kts` stay as-is.
- The change reaches consumer libraries automatically via `apply<VersionsPlugin>()` in `sources/LibraryConfigurator.kt`.

**Blocked (as of 2026-07-23).** 0.55.0's publication is pending manual Gradle Plugin Portal approval (upstream issue #997). The Plugin Portal marker and the Maven Central artifact both 404, so bumping today breaks the build. Wait for both to resolve, then verify with a dependency resolve/build before adopting.
