# CI must re-list the setup-gradle defaults when caching the TestKit home

Why `.github/workflows/tests.yml`'s `gradle-home-cache-includes` names `caches` and `notifications` even though it only needs to add one more path.

The functional tests point TestKit at a persistent Gradle user home under the real one (`gradle.gradleUserHomeDir.resolve("testkit/io.fluidsonic.gradle")`, set via the `io.fluidsonic.gradle.testKitDirectory` system property in the `test` task of `build.gradle.kts`, read by `testKitDirectory` in `tests/io/fluidsonic/gradle/FunctionalTestSupport.kt`) instead of TestKit's own default under `build/`, so a `clean` no longer discards the nested builds' dependency cache. That relocated cache is invisible to `gradle/actions/setup-gradle`'s default caching unless told about it.

`gradle-home-cache-includes` on `setup-gradle` **replaces** the action's default include set (`caches`, `notifications`) rather than adding to it, per the action's own documentation. So the workflow step must spell out all three paths — `caches`, `notifications`, `testkit/io.fluidsonic.gradle/caches` — or it would silently stop caching the two defaults while adding the new one.

Pitfall: if the `testKitDirectory` system property name or the resolved subpath (`testkit/io.fluidsonic.gradle`) ever changes in `build.gradle.kts`, the CI cache path must be updated to match, or CI silently reverts to downloading the nested builds' dependencies on every run.
