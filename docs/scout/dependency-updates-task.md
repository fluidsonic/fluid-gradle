# `./gradlew dependencyUpdates` needs the configuration cache and parallel execution disabled (on plugin 0.54.0)

The dependency-check command documented in `CLAUDE.md` fails out of the box on the bundled plugin version and must be run with extra flags.

`gradle.properties` enables `org.gradle.configuration-cache=true` and `org.gradle.parallel=true` project-wide. The gradle-versions plugin (`com.github.ben-manes:gradle-versions-plugin`, version `gradle-versions` in `gradle/libs.versions.toml`) that backs the `dependencyUpdates` task, on 0.54.0, resolves and aggregates *all* subproject configurations from the *root* task. Gradle 9 forbids resolving another project's configurations under parallel project execution, so the root task produces no usable report — a bare `./gradlew dependencyUpdates` fails.

There is no flag-free workaround on 0.54.0: `--no-parallel` cannot be set from a build script or plugin, because parallelism is fixed at build initialization, before any build script evaluates. The flags must be passed on the command line:

`./gradlew dependencyUpdates --no-configuration-cache --no-parallel`

The task itself is configured in `build.gradle.kts` (the `dependencyUpdates { }` block) — see `versions.md` for the `rejectVersionIf` unstable-candidate filter it uses.

The flag-free fix is upgrading the plugin to 0.55.0 — see `dependency-updates-plugin-0.55-upgrade.md` (currently blocked).

gradle-versions-plugin version: 0.54.0 (as of 2026-07, `gradle/libs.versions.toml`).
