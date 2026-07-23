package io.fluidsonic.gradle


/**
 * Resolved root-level configuration produced by [LibraryConfigurationBuilder] and consumed by [LibraryConfigurator].
 *
 * @property defaultModuleConfiguration Baseline merged into every module's own configuration (see `allModules {}`).
 * @property fullName Published library name, including the `fluid-` prefix when one was applied; also the required root
 *   Gradle project name. Distinct from [name], the unprefixed short name.
 */
internal class LibraryConfiguration(
	val defaultModuleConfiguration: LibraryModuleConfiguration,
	val dependencyUpdatesIncludeUnstableVersions: Boolean,
	val fullName: String,
	val gradleVersion: String,
	val name: String,
	val version: String
)
