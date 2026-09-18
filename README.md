fluid-gradle
============

Gradle plugin for simplifying the configuration of multiplatform `io.fluidsonic.*` Kotlin libraries.
It provides a concise DSL for declaring targets, dependencies, language settings, and publishing.


Usage
-----

### 1. Apply the plugin

In your root `build.gradle.kts`:

```kotlin
plugins {
    id("io.fluidsonic.gradle") version "4.2.0"
}
```

### 2. Configure the library (root project)

```kotlin
fluidLibrary(name = "country", version = "0.15.0") {
    allModules {
        targets {
            jvm()
        }
    }
}
```

### 3. Configure a module (subproject)

```kotlin
fluidLibraryModule(description = "Kotlin multiplatform country library") {
    targets {
        common {
            dependencies {
                api(kotlinx("serialization-core", "1.9.0"))
            }
        }
        jvm {
            jdk(21)
            dependencies {
                api(fluid("stdlib", "0.15.0"))
            }
            testDependencies {
                implementation(kotlin("test"))
            }
        }
    }
    language {
        withExperimentalApi("kotlin.contracts.ExperimentalContracts")
    }
}
```

Dependency notations: `fluid(name, version)` for `io.fluidsonic.*` modules, `kotlin(name, version?)` for
`kotlin-stdlib-*`, `kotlinx(name, version)` for `kotlinx-*`, and `project(path)` for local Gradle projects.
Each dependency scope is available as `api`, `implementation`, `compileOnly`, and `runtimeOnly`.


Options
-------

The plugin applies sensible defaults that can be adjusted per module or target:

```kotlin
fluidLibraryModule(description = "…") {
    disableDokka()          // skip Dokka documentation generation
    disablePublishing()     // exclude this module from Maven publication

    targets {
        jvm {
            jdk(24)                                  // JDK to compile against (21–24, default 21)
            disableKotlinDependencyVersionAlignment() // drop the BOM aligning all Kotlin dependencies
        }
    }
    language {
        disableExplicitApi() // turn off explicit API mode for this module
    }
}
```

Root-level options are available inside `fluidLibrary(…) { … }`:

- `gradleVersion("9.7.1")` — sets the Gradle wrapper version.
- `dependencyUpdatesIncludeUnstableVersions()` — includes pre-release versions when checking for updates.


Test output
-----------

Test tasks are quiet by default so that a failure is easy to spot.

- A **successful** run prints one summary line per test task:
  ```
  :module:jvmTest — 50 tests, all passed
  ```
- A **failing** run prints that summary with the counts (`51 tests, 50 passed, 1 failed`) plus, for each failed test,
  the full exception with its causes and stack trace.
- Per-test standard output and standard error are not printed to the console. They are always recorded in the HTML
  report at `<module>/build/reports/tests/<task>/index.html`.
- `./gradlew check --info` restores the verbose console output — an event per passed, failed, and skipped test, plus
  each test's standard output and standard error.


Requirements
------------

- Kotlin 2.3+
- Gradle 9.4+
- JDK 21+


Breaking changes from 2.x
--------------------------

- Darwin/Native targets have been removed.
- JS target is deprecated and will be removed in a future version.
- JDK 21+ is now required (was 8+).
- The `compiler` parameter has been removed from `TargetsDsl.js()` (IR is the only compiler).
- `kapt()` is deprecated in favor of `ksp()`.
- Default opt-ins for `kotlin.ExperimentalUnsignedTypes` and `kotlin.RequiresOptIn` have been removed (both are stable).
- JetBrains Space Maven repositories have been removed (Space was sunset).
- Dokka has been migrated from v1 to v2 API.
- JUnit has been upgraded from 5.x to 6.x (requires Java 17+).


License
-------

Apache 2.0
