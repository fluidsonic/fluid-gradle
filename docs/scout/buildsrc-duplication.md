# buildSrc holds a second, unmergeable copy of version-stability and publishing metadata

Why some logic exists twice in this repo, and why you must not try to consolidate it into one shared symbol.

This plugin does **not** apply itself, and its runtime code under `sources/` sits on a classpath disjoint from the build's own scripts. `buildSrc/` is visible only to the build scripts (`build.gradle.kts`), never to the plugin runtime; conversely `sources/` cannot see `buildSrc`. So any logic the build script needs that also ships in the plugin must be **physically duplicated** — there is no location visible to both.

Two things live in both copies as a result:
- **Unstable-version detection.** Build-side: `VersionStability.isUnstableVersion` in `buildSrc/src/main/kotlin/io/fluidsonic/gradle/buildsupport/VersionStability.kt`. Plugin-runtime: `isUnstableVersion` in `sources/utility/Project.kt` (see `versions.md`).
- **Publishing/POM metadata.** Build-side: `PublishingMetadata` (same `buildsupport` package), consumed by the root `build.gradle.kts` `nexusPublishing`/`pom` blocks. Plugin-runtime equivalent: `sources/PublishingConfigurator.kt` (see `publishing.md`).

`buildSrc` consolidates only the **build-side** copies (previously inline in `build.gradle.kts`); it cannot absorb the plugin-runtime copies. Do not attempt to "dedupe" the two sides into one shared symbol — it is structurally impossible here.

The package is `buildsupport`, not `build`, deliberately — see `gitignore-build-trap.md`.
