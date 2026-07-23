# Changelog

## [Unreleased]

### Added
- Dependabot configuration for automated dependency updates (Gradle and GitHub Actions ecosystems, weekly)
- Scout knowledge base under `docs/scout/` documenting non-obvious, repo-specific conventions and traps
- Per-module JVM target selection via `jvm { jdk(version) }` in the target DSL (JDK 21–24; defaults to 21)

### Changed
- Kotlin updated to 2.4.10 (KSP to 2.3.10); language and API baseline raised to 2.4
- Gradle wrapper updated to 9.6.1
- Dokka updated to 2.2.0
- JUnit updated to 6.1.2
- Gradle Plugin Publish updated to 2.1.1
- Gradle Versions plugin updated to 0.54.0
- GitHub Actions pinned to full commit SHAs (`actions/checkout` v7.0.1, `actions/setup-java` v5.6.0)
- JUnit test dependencies consolidated onto the Gradle version catalog
- Renamed the opt-out DSL methods to `disableDokka()`, `disablePublishing()`, `disableExplicitApi()`, and `disableKotlinDependencyVersionAlignment()` for naming consistency
- CI now runs on pull requests, caches Gradle, and tests against JDK 21 and 25; the Gradle wrapper distribution is pinned by SHA-256 and validated in CI

### Deprecated
- `noDokka()`, `withoutPublishing()`, `withoutExplicitApi()`, and `withoutEnforcingSameVersionForAllKotlinDependencies()` — use the corresponding `disable…` methods (aliases retained for one release)

### Fixed
- Accessing `project.kotlin` before the Kotlin multiplatform plugin is applied now fails with a clear message instead of a bare `NullPointerException`
- Unsupported dependency notations now fail fast with a clear error instead of being silently passed through

### Removed
- `withLanguageFeature(...)` from the language DSL — the underlying Kotlin compiler API was deprecated to error level and removed in Kotlin 2.4 with no replacement

## [3.0.0]

### Added
- KSP annotation processor support via `ksp()` DSL methods in `JvmDependenciesDsl`
- JDK 23 and 24 support in `JdkVersion` enum
- Gradle version catalog (`gradle/libs.versions.toml`) for centralized dependency management
- KDoc documentation on all public API surfaces
- Comprehensive unit test suite (57 tests)
- `CLAUDE.md` project documentation

### Changed
- Minimum JDK target raised from 8 to 21
- Kotlin updated to 2.3.20
- Gradle wrapper updated to 9.4.1
- Dokka updated to 2.1.0 (migrated from v1 to v2 API)
- Gradle Plugin Publish updated to 2.1.0
- JUnit updated to 6.0.3 (unified versioning)
- Common test dependencies simplified to `kotlin("test")`
- Dokka plugin applied by ID instead of class reference

### Deprecated
- `kapt()` methods in `JvmDependenciesDsl` — use `ksp()` instead
- `JsTargetDsl` and `JsDependenciesDsl` — JS target support will be removed in a future version

### Removed
- Darwin/Native target support (iOS, macOS, tvOS, watchOS)
- `KotlinJsCompilerType` parameter from `TargetsDsl.js()` — IR is now the only compiler
- JDK 8-20 from `JdkVersion` enum
- JetBrains Space Maven repository URLs (Space was sunset)
- `junit-platform-runner` dependency (removed in JUnit 6)
- Stale `kotlin.ExperimentalUnsignedTypes` and `kotlin.RequiresOptIn` default opt-ins
- Manual MPP hierarchy template suppression and commented-out Commonizer workarounds
