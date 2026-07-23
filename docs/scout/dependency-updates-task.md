# `./gradlew dependencyUpdates` needs the configuration cache and parallel execution disabled

The dependency-check command documented in `CLAUDE.md` fails out of the box and must be run with extra flags.

`gradle.properties` enables `org.gradle.configuration-cache=true` and `org.gradle.parallel=true` project-wide. The gradle-versions plugin (`com.github.ben-manes:gradle-versions-plugin`, version `gradle-versions` in `gradle/libs.versions.toml`) that backs the `dependencyUpdates` task is not configuration-cache compatible and errors under parallel project execution, so a bare `./gradlew dependencyUpdates` fails.

Run it as:

`./gradlew dependencyUpdates --no-configuration-cache --no-parallel`

The task itself is configured in `build.gradle.kts` (the `dependencyUpdates { }` block) — see `versions.md` for the `rejectVersionIf` unstable-candidate filter it uses.

gradle-versions-plugin version: 0.54.0 (as of 2026-07, `gradle/libs.versions.toml`).
