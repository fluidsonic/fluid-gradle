# Kotlin/JS target: deprecated, but wired as IR-only with CommonJS

The JS target and its whole DSL surface are on the way out, yet still fully functional with fixed defaults.

`JsTargetDsl` and `JsDependenciesDsl` (the `npm`/`devNpm`/`optionalNpm`/`peerNpm`, `withoutBrowser`, `withoutNodeJs` methods) in `sources/LibraryModuleDsl.kt` are annotated `@Deprecated("JS target support will be removed in a future version.")`. Do not build new functionality on the JS target.

While present, `configureJsTargets` in `sources/LibraryModuleConfigurator.kt` wires it as `js(KotlinJsCompilerType.IR)` with `useCommonJs()`, and enables both `browser` and `nodejs` sub-targets unless `withoutBrowser()`/`withoutNodeJs()` were called. Source directories use the `-js` suffix (see `source-set-layout.md`).

NPM notations are not resolved eagerly: `npm()` and friends return sealed `SpecialDependency` placeholder objects, resolved to real Kotlin `npm(...)` calls only inside the `KotlinDependencyHandler.resolve` overload in `sources/LibraryModuleConfigurationBuilder.kt`.
