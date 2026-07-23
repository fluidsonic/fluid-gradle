# ksp() and kapt() are JVM-only; kapt is deprecated for ksp

The annotation-processing DSL methods look target-agnostic but only take effect on the JVM target.

In `sources/LibraryModuleConfigurationBuilder.kt` (`DependenciesBuilder`), `ksp()` adds to the literal configuration name `"kspJvm"` (JVM-specific), and `kapt()` adds to `"kapt"`. Both the KSP plugin (`pluginManager.apply("com.google.devtools.ksp")`) and kapt (`Kapt3GradleSubplugin`) are applied only when the respective configuration list is non-empty, and both wirings live **inside** `configureJvmTarget` in `sources/LibraryModuleConfigurator.kt`. Consequence: declaring `ksp(...)`/`kapt(...)` in a common or JS dependencies block produces no processing — it only works under a `jvm { }` target.

Direction of travel: `JvmDependenciesDsl.kapt(...)` in `sources/LibraryModuleDsl.kt` is `@Deprecated` with `ReplaceWith` pointing to `ksp(...)`. Both are still wired (separate `kaptConfigurations`/`kspConfigurations` lists), but new code should use `ksp()`.

Version-bump trap: the `ksp` version in `gradle/libs.versions.toml` is decoupled from the `kotlin` version and does not track it (KSP2 has its own release line). When bumping Kotlin, do not force KSP to the same number — move it independently to the KSP release that supports the target Kotlin (e.g. Kotlin 2.4.10 pairs with KSP 2.3.10, not 2.4.x).

ksp version: 2.3.10 (as of 2026-07, `gradle/libs.versions.toml`).
