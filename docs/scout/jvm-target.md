# JVM target: JDK 21 floor and auto-injected JUnit 6

What the JVM target forces onto a consumer module without the module declaring it.

In `sources/LibraryModuleConfigurator.kt`, `configureJvmTargets` always calls `configureJvmTarget` with `JdkVersion.v21`, and `configureJvmTarget` opens with `require(jdkVersion >= JdkVersion.v21)`. The JDK 21 floor is a hard `require`, not a configurable setting.

The JVM **test** source set is force-injected with test dependencies the user never declares: `kotlin("test-junit5")`, `org.junit.jupiter:junit-jupiter-api`, and `runtimeOnly` `junit-jupiter-engine`, all at `Versions.junit` (see `versions.md`). Test runs are pinned to `useJUnitPlatform { includeEngines("junit-jupiter") }`. Do not add these JUnit dependencies in a consumer module — they are already present, and the engine list is restricted to `junit-jupiter`.

By contrast the **common** test source set (`configureCommonTarget`) only gets `kotlin("test")`.

Both main and test JVM compilations also set the `TargetJvmVersion` attribute to `jdkVersion.code`.

junit version: 6.1.2 (as of 2026-07, `sources/Versions.kt` `Versions.junit` — the constant the code actually injects).
