# Configuration merge: per-field winner rules are inconsistent

The DSL -> Builder -> Configuration pipeline layers an `allModules {}` default under each module's own config via `mergeWith`, and which layer wins is **not uniform across fields**. Read the exact expression before assuming.

In `sources/LibraryModuleConfiguration.kt`:
- `LibraryModuleConfiguration.mergeWith` resolves `description` as `other.description ?: description` — the incoming/override (per-module) wins.
- `Language.mergeWith` resolves `version` as `version ?: other.version` — the **receiver** (the `allModules` default) wins when both are set. Opposite direction from `description`.
- Booleans combine structurally, also inconsistently: `isPublishingEnabled` and `enforcesSameVersionForAllKotlinDependencies` use AND (any `false` wins/disables); `noDokka`, `noExplicitApi`, `noBrowser`, `noNodeJs` use OR (any `true` wins/enables).
- Lists/sets (`customConfigurations`, `experimentalApisToUse`, dependency `configurations`) are always concatenated/unioned — never replaced. Every `custom {}` block accumulates.

The `addTargetsAutomatically` flag flips behavior between the two call sites. `Targets.mergeWith` always merges `common`, but `js`/`jvm` survive a one-sided merge only when `addAutomatically` is true (`... ?: js.takeIf { addAutomatically }`). `LibraryConfigurationBuilder.allModules` merges with the flag **true** (so default targets propagate); `LibraryModuleConfigurator` merges the default into each module with the flag **false**. Test expectations in `tests/io/fluidsonic/gradle/LibraryModuleConfigurationTest.kt` pin these rules.
