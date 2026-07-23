# ksp() and kapt() are JVM-only; kapt is deprecated for ksp

The annotation-processing DSL methods look target-agnostic but only take effect on the JVM target.

In `sources/LibraryModuleConfigurationBuilder.kt` (`DependenciesBuilder`), `ksp()` adds to the literal configuration name `"kspJvm"` (JVM-specific), and `kapt()` adds to `"kapt"`. Both the KSP plugin (`pluginManager.apply("com.google.devtools.ksp")`) and kapt (`Kapt3GradleSubplugin`) are applied only when the respective configuration list is non-empty, and both wirings live **inside** `configureJvmTarget` in `sources/LibraryModuleConfigurator.kt`. Consequence: declaring `ksp(...)`/`kapt(...)` in a common or JS dependencies block produces no processing — it only works under a `jvm { }` target.

Direction of travel: `JvmDependenciesDsl.kapt(...)` in `sources/LibraryModuleDsl.kt` is `@Deprecated` with `ReplaceWith` pointing to `ksp(...)`. Both are still wired (separate `kaptConfigurations`/`kspConfigurations` lists), but new code should use `ksp()`.

ksp version: 2.3.6 (as of 2026-07, `gradle/libs.versions.toml`).
