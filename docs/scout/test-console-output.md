# Gradle `testLogging` traps behind the quiet test output

The three counter-intuitive parts of Gradle's `testLogging` API that shape `configureTestTask` in `sources/LibraryModuleConfigurator.kt`. Matters whenever a consumer module's test console output is changed. See `test-summary-listener.md` for the per-task summary line.

`configureTestTask` is applied to the JVM target's test-run execution task and to the JS `browser`/`nodejs` test tasks.

**`events(…)` adds, `events = …` replaces.** `DefaultTestLogging.events(Object...)` calls `events.addAll(...)`, despite a javadoc that says it sets them; only the property assignment (`setEvents`) replaces the set. Always assign here, never call the method form, or previously configured events survive unnoticed.

**`showStandardStreams` is not an independent flag.** `setShowStandardStreams` adds (or removes) `STANDARD_OUT` and `STANDARD_ERROR` in that very same event set, so an `events = …` assignment after it silently undoes it, and vice versa.

**The container-level accessors reach the lifecycle view only.** `DefaultTestLoggingContainer` keeps one `TestLogging` per log level and delegates its own getters/setters to the lifecycle one. The `info` and `debug` views keep their stock defaults (`info`: `FAILED`, `SKIPPED`, `STANDARD_OUT`, `STANDARD_ERROR`), so restoring the verbose view under `--info` requires the separate `info { }` block.

Pitfall: this plugin does not apply itself. Its own `test` task (`build.gradle.kts`, the `tasks.withType<Test>` block) only calls `useJUnitPlatform()`, so it runs Gradle's stock lifecycle logging — `FAILED` only, `SHORT` exception format. A `println` in this repo's own tests never reaches the console; read `build/test-results/test/TEST-*.xml` or fold the value into an assertion message instead.

Gradle version: 9.7.1 (as of 2026-09, `gradle/wrapper/gradle-wrapper.properties`).
