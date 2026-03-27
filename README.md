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
    id("io.fluidsonic.gradle") version "3.0.0"
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
        jvm {
            dependencies {
                api(fluid("stdlib", "0.15.0"))
            }
        }
    }
    language {
        withExperimentalApi("kotlin.contracts.ExperimentalContracts")
    }
}
```


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
