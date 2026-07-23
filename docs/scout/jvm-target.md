# JVM target: selectable JDK 21–24 (default 21) and auto-injected JUnit 6

What the JVM target forces onto a consumer module, and how its JDK version is chosen.

The compiled JDK version is selectable per module via `jdk(version: Int)` on `JvmTargetDsl` (`sources/LibraryModuleDsl.kt`). It defaults to JDK 21 when not called, and only 21–24 are accepted — any other value throws `IllegalArgumentException` (`JvmBuilder.jdk` in `sources/LibraryModuleConfigurationBuilder.kt`). Values map through the `JdkVersion` enum (`sources/utility/JdkVersion.kt`, `v21`–`v24`), whose `kotlinJvmTargetValue` is the matching Kotlin `JvmTarget`. On merge with the `allModules {}` default the higher JDK wins (`maxOf`) — see `configuration-merge.md`. JDK 21 remains a hard floor: `configureJvmTarget` in `sources/LibraryModuleConfigurator.kt` opens with `require(jdkVersion >= JdkVersion.v21)`; `configureJvmTargets` passes `jvmConfiguration.jdkVersion`.

The JVM **test** source set is force-injected with dependencies the user never declares — `addJvmTestDependencies` (`sources/LibraryModuleConfigurator.kt`) adds `kotlin("test-junit5")`, `org.junit.jupiter:junit-jupiter-api`, and `runtimeOnly` `junit-jupiter-engine`, all at `Versions.junit` (see `versions.md`). Test runs are pinned to `useJUnitPlatform { includeEngines("junit-jupiter") }`. Do not add these JUnit dependencies in a consumer module. By contrast the **common** test source set only gets `kotlin("test")` (`addKotlinTestDependency`).

Both compilations also set the `TargetJvmVersion` attribute to `jdkVersion.code`.

junit version: 6.1.2 (as of 2026-07, `sources/Versions.kt` `Versions.junit`).
