# fluid-gradle

## Commands

- Build: `./gradlew build`
- Test: `./gradlew test`
- Dependency check: `./gradlew dependencyUpdates`

## Source layout

- Sources: `sources/`
- Tests: `tests/`

## Architecture

Gradle plugin using a DSL -> Builder -> Configuration -> Configurator pattern.

- Package: `io.fluidsonic.gradle`
- Key entry points: `fluidLibrary()` (root project) and `fluidLibraryModule()` (subprojects)
- DSL interfaces define the API surface
- Builder classes collect configuration
- Configuration data classes hold the resolved settings
- Configurator classes apply the configuration to Gradle projects

## Conventions

- Tab indentation (literal tab characters)
- Explicit API mode is enabled
- Kotlin with multiplatform support
- Version tags have no `v` prefix (e.g. `3.0.0`, not `v3.0.0`)
