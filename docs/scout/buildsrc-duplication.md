# buildSrc holds a second, unmergeable copy of version-stability and publishing metadata

Why some logic exists twice in this repo, and why you must not try to consolidate it into one shared symbol.

This plugin does **not** apply itself, and its runtime code under `sources/` sits on a classpath disjoint from the build's own scripts. `buildSrc/` is visible only to the build scripts (`build.gradle.kts`), never to the plugin runtime; conversely `sources/` cannot see `buildSrc`. So any logic the build script needs that also ships in the plugin must be **physically duplicated** — there is no location visible to both.

Four things live in both copies as a result:
- **Unstable-version detection.** Build-side: `VersionStability.isUnstableVersion` in `buildSrc/src/main/kotlin/io/fluidsonic/gradle/buildsupport/VersionStability.kt`. Plugin-runtime: `isUnstableVersion` in `sources/utility/Project.kt` (see `versions.md`).
- **Publishing/POM metadata.** Build-side: `PublishingMetadata` (same `buildsupport` package), consumed by the root `build.gradle.kts` `nexusPublishing`/`pom` blocks. Plugin-runtime equivalent: `sources/PublishingConfigurator.kt` (see `publishing.md`).
- **Gradle wrapper checksum fetching.** `fetchGradleDistributionSha256Sum`/`parseSha256Checksum` in `buildSrc/src/main/kotlin/io/fluidsonic/gradle/buildsupport/GradleWrapperChecksum.kt` (build-side) and `sources/utility/GradleWrapperChecksum.kt` (plugin-runtime). Only the plugin-runtime copy is unit-tested (`tests/io/fluidsonic/gradle/ParseSha256ChecksumTest.kt`) — a fix to the parsing logic must be applied to both files by hand. See `wrapper-checksum-pinning.md`.
- **GitHub Packages snapshot publish retry.** `buildSrc/src/main/kotlin/io/fluidsonic/gradle/buildsupport/GithubPackagesPublishing.kt` (build-side) and `sources/utility/GithubPackagesPublishing.kt` (plugin-runtime) are identical apart from package, header comment, blank-line convention, the entry point's `public`/`internal`, and `rootCauseMessage()` being `internal` (unit-tested in `tests/io/fluidsonic/gradle/RootCauseMessageTest.kt`) in the runtime copy versus `private` in buildSrc. Only the plugin-runtime copy has functional tests (`tests/io/fluidsonic/gradle/GithubPackagesPublishRetryFunctionalTest.kt`). See `github-packages-snapshot-retry.md`.

`buildSrc` consolidates only the **build-side** copies (previously inline in `build.gradle.kts`); it cannot absorb the plugin-runtime copies. Do not attempt to "dedupe" the two sides into one shared symbol — it is structurally impossible here.

The package is `buildsupport`, not `build`, deliberately — see `gitignore-build-trap.md`.
