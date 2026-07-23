# Kotlin dependencies are silently forced to the plugin's Kotlin version

A default behavior that silently overrides any `org.jetbrains.kotlin:*` version a consumer declares.

`configureTargetBasics` in `sources/LibraryModuleConfigurator.kt` installs, on every compilation's compile configuration, a `resolutionStrategy.eachDependency` that rewrites any dependency with `group == "org.jetbrains.kotlin"` to `project.getKotlinPluginVersion()`, with reason "All Kotlin modules must have the same version." So a declared `org.jetbrains.kotlin:kotlin-reflect:<other>` is quietly resolved to the plugin's bundled Kotlin version instead.

This is on by default for every target (the flag `enforcesSameVersionForAllKotlinDependencies` defaults to `true` in `sources/LibraryModuleConfigurationBuilder.kt`, `TargetBuilder`). The only opt-out is the easily-missed `disableKotlinDependencyVersionAlignment()` in the target DSL (the old name `withoutEnforcingSameVersionForAllKotlinDependencies()` survives as a `@Deprecated` alias). If a consumer's Kotlin dependency version appears "ignored", this hook is why.

Kotlin version: 2.4.10 (as of 2026-07, `gradle/libs.versions.toml`).
