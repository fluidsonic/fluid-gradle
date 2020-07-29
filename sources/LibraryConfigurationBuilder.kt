package io.fluidsonic.gradle


internal class LibraryConfigurationBuilder(
	private val fullName: String,
	private val name: String,
	private val version: String
) : LibraryDsl {

	private var defaultModuleConfiguration: LibraryModuleConfiguration? = null
	private var dependencyUpdatesIncludeUnstableVersions = false
	private var gradleVersion: String? = null


	fun build() = LibraryConfiguration(
		defaultModuleConfiguration = defaultModuleConfiguration ?: LibraryModuleConfiguration.default,
		dependencyUpdatesIncludeUnstableVersions = dependencyUpdatesIncludeUnstableVersions,
		fullName = fullName,
		gradleVersion = gradleVersion ?: Versions.gradle,
		name = name,
		version = version
	)


	override fun allModules(configure: LibraryModuleDsl.() -> Unit) {
		LibraryModuleConfigurationBuilder(description = null).apply(configure).build().also { configuration ->
			defaultModuleConfiguration = defaultModuleConfiguration?.mergeWith(configuration, addTargetsAutomatically = true) ?: configuration
		}
	}


	override fun dependencyUpdatesIncludeUnstableVersions() {
		dependencyUpdatesIncludeUnstableVersions = true
	}


	override fun gradleVersion(version: String) {
		gradleVersion = version
	}
}
