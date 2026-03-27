# Changelog

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
