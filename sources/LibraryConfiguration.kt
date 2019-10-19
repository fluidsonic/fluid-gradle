package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.*
import org.gradle.api.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*


class LibraryConfiguration internal constructor(
	val name: String,
	private val project: Project,
	val version: String
) {

	init {
		check(name.isNotEmpty()) { "'name' must be set" }
		check(version.isNotEmpty()) { "'version' must be set" }
	}


	var dependencyUpdatesIncludeUnstableVersions = false
	var gradleVersion = Versions.gradle


	private fun configureBasics(): Unit = project.run {
		val libraryName = this@LibraryConfiguration.name
		val libraryVersion = this@LibraryConfiguration.version

		apply<VersionsPlugin>()

		repositories {
			mavenCentral()
			jcenter()
			bintray("fluidsonic/kotlin")
			bintray("kotlin/kotlin-eap")
			bintray("kotlin/kotlinx")
		}

		wrapper {
			distributionType = Wrapper.DistributionType.ALL
			gradleVersion = this@LibraryConfiguration.gradleVersion
		}

		dependencyUpdates {
			outputFormatter = null

			if (!dependencyUpdatesIncludeUnstableVersions)
				rejectVersionIf {
					isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
				}
		}

		extensions.add("io.fluidsonic.gradle", FluidsonicPluginExtension(
			name = libraryName,
			version = libraryVersion
		))
	}


	internal fun configureProject() {
		configureBasics()
	}


	companion object
}


fun Project.fluidLibrary(name: String, version: String, configure: LibraryConfiguration.() -> Unit = {}) {
	check(project.parent == null) { "fluidLibrary(…) {} must only be used in the root project" }
	check(project.extensions.findByType<FluidsonicPluginExtension>() == null) { "fluidLibrary/fluidJvmLibrary(…) {} must only be used once" }
	check(project.name == "fluid-$name") { "Project name '${project.name}' must not differ from library name 'fluid-$name' in fluidLibrary(…)" }

	LibraryConfiguration(name = name, project = project, version = version).apply(configure).configureProject()
}
