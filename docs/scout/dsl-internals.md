# DSL internals: custom() escape hatches and the shared dependencies builder

Two internal design choices in the DSL that aren't visible from the interface signatures.

**`custom { }` escape hatches bridge to raw Kotlin-Gradle types.** Several DSL interfaces in `sources/LibraryModuleDsl.kt` expose a generically-named `custom` block that hands back the underlying third-party builder: `LibraryModuleDsl.custom` -> `KotlinMultiplatformExtension`, `LanguageDsl.custom` -> `LanguageSettingsBuilder`, `DependenciesDsl.custom` -> `KotlinDependencyHandler`, `TargetDsl.custom` -> the underlying Kotlin target type. All `custom` blocks are stored as `customConfigurations` lists and **accumulate** across merges (never replace) — see `configuration-merge.md`. Use these for anything the typed DSL doesn't cover.

**One builder implements both JS and JVM dependency DSLs.** `DependenciesBuilder` in `sources/LibraryModuleConfigurationBuilder.kt` implements both `JsDependenciesDsl` and `JvmDependenciesDsl` on a single object. Per-target type safety exists only at the interface boundary: `TargetBuilder.dependencies`/`testDependencies` apply the caller's typed lambda through an `UNCHECKED_CAST` to `DependenciesDsl`. This is why `ksp`/`kapt` (JVM) and `npm` (JS) physically live on the same builder even though the target DSLs expose them separately.
