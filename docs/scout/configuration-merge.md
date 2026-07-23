# Configuration merge: per-field winner rules are inconsistent

The DSL -> Builder -> Configuration pipeline layers an `allModules {}` default under each module's own config via `mergeWith`, and which layer wins is **not uniform across fields**. Read the exact expression before assuming.

In `sources/LibraryModuleConfiguration.kt`:
- `LibraryModuleConfiguration.mergeWith` resolves `description` as `other.description ?: description` — the incoming/override (per-module) wins.
- `Language.mergeWith` resolves `version` as `version ?: other.version` — the **receiver** (the `allModules` default) wins when both are set. Opposite direction from `description`. A language `version` set in `allModules {}` therefore cannot be overridden per-module.
- Boolean feature flags all combine with AND, so disabling in either place wins: `isPublishingEnabled`, `enforcesSameVersionForAllKotlinDependencies`, `isDokkaEnabled`, `language.isExplicitApiEnabled`, and JS `isBrowserEnabled`/`isNodeJsEnabled`. (These were phrased as `noDokka`/`noExplicitApi`/… OR-combined before the flags were renamed to positive form; behavior is unchanged.)
- JVM `jdkVersion` merges with `maxOf` — the higher JDK wins (a comment in `Jvm.mergeWith` explains a JDK selection is a minimum, so the maximum never violates either side's minimum). See `jvm-target.md`.
- Lists/sets (`customConfigurations`, `experimentalApisToUse`, dependency `configurations`) are always concatenated/unioned — never replaced. Every `custom {}` block accumulates.

The `addTargetsAutomatically` flag flips behavior between the two call sites. `Targets.mergeWith` always merges `common`, but `js`/`jvm` survive a one-sided merge only when `addAutomatically` is true (`... ?: js.takeIf { addAutomatically }`). `LibraryConfigurationBuilder.allModules` merges with the flag **true** (so default targets propagate); `LibraryModuleConfigurator` merges the default into each module with the flag **false**. Test expectations in `tests/io/fluidsonic/gradle/LibraryModuleConfigurationTest.kt` pin these rules.
