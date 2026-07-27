# ktlint: .editorconfig control and the baseline

How ktlint is configured here and how deliberate naming exceptions are kept passing. Matters when a lint run flags naming/indentation, or when adding intentional-exception code. See `lint-and-format-tooling.md` for the tool split.

**ktlint's primary config is `.editorconfig`, not the Gradle DSL.** Three settings there drive its behavior: `indent_style = tab` preserves the project's literal-tab indentation (ktlint would otherwise rewrite to spaces); `ktlint_code_style = intellij_idea` (not the stricter `ktlint_official`) to match existing formatting; and `max_line_length = 160`. Setting any line-length cap activates ktlint's wrapping rules (argument-list-wrapping, function/class-signature), which `ktlintFormat` auto-applies — the wide reformat in this branch's history came from that.

**ktlint rule enable/disable is binary — no per-pattern config.** Deliberate convention exceptions therefore live in a baseline at `config/ktlint/baseline.xml` (auto-detected on `ktlintCheck`; regenerate with `./gradlew ktlintGenerateBaseline`). Current exceptions:
- `enum-entry-name-case` — the `JdkVersion.v21`–`v24` entries (`sources/utility/JdkVersion.kt`).
- `property-naming` — the lowercase `junit`/`gradle` consts in `Versions` (`sources/Versions.kt`).

If you legitimately add another unconventional name, regenerate the baseline rather than reformatting the code — but note that baseline entries are anchored by line number, so prefer a conforming name in files that change often. For constants specifically, detekt pushes in the opposite direction and narrows the choice to two options; see `constant-naming-lint-conflict.md`.

Note: ktlint's `function-naming` already exempts `@Test` functions, so this project's underscore-separated test names pass ktlint without a baseline entry (they were only flagged by detekt's now-disabled `naming` ruleset).
