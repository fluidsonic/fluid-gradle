package io.fluidsonic.gradle


internal class LibraryConfiguration(
	val defaultModuleConfiguration: LibraryModuleConfiguration,
	val dependencyUpdatesIncludeUnstableVersions: Boolean,
	val fullName: String,
	val gradleVersion: String,
	val name: String,
	val version: String
)
