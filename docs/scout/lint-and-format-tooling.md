# Linting and formatting: ktlint + detekt division

How static analysis is split across two tools, and why detekt runs a pre-release line. Matters before touching `config/detekt/detekt.yml` or the `detekt {}` block in `build.gradle.kts`.

**Strict ownership split, enforced by config.** ktlint owns all formatting and style — imports, naming format, line length. detekt owns only complexity, code smells, and bugs. To prevent the same issue being reported by both tools, `config/detekt/detekt.yml` disables detekt's overlapping rules: the entire `naming` ruleset (`active: false`), plus `style/WildcardImport` and `style/MaxLineLength`. Do **not** re-enable those in detekt or add formatting rules to it — put style concerns in ktlint's `.editorconfig` instead (see `ktlint-editorconfig-baseline.md`).

**detekt runs 2.0-alpha deliberately, not the stable 1.23.x line.** Per this repo's tooling-adoption session: the 2.0-alpha line is built against Kotlin 2.4 and avoids the `kotlin-compiler-embeddable` classpath-conflict warning (detekt issue #7883) that stable triggers on Kotlin-2.4 Gradle-plugin projects. Accepted downside: alpha rule/config APIs may shift. The plugin id is `dev.detekt` (2.0), not the 1.x `io.gitlab.arturbosch.detekt`.

**Calibrated, not silenced:** `CyclomaticComplexMethod.ignoreSingleWhenExpression` (exhaustive sealed `when`), `EmptyFunctionBlock.ignoreOverridden` (`LibraryPlugin.apply()` no-op), `TooManyFunctions` excludes `**/tests/**`.

**The ktlint CLI version and the ktlint-gradle plugin version are two separate catalog keys.** `gradle/libs.versions.toml` defines `ktlint-gradle` (14.2.0, referenced by the `ktlint` plugin alias's `version.ref`) and `ktlint-cli` (1.8.0) independently — per the version-bump commit, the plugin's bundled default trails current ktlint releases, so the catalog pins the CLI explicitly. `build.gradle.kts`'s `ktlint { version.set(libs.versions.ktlint.cli) }` block reads that catalog value instead of a hardcoded literal, so `dependencyUpdates` and Dependabot both see it.

detekt version: 2.0.0-alpha.6, ktlint-gradle: 14.2.0, ktlint-cli: 1.8.0 (as of 2026-09, `gradle/libs.versions.toml` and `build.gradle.kts`).
