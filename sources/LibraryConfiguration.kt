package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.*
import com.github.benmanes.gradle.versions.updates.gradle.*
import org.gradle.api.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*


class LibraryConfiguration internal constructor(
	val fullName: String,
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
		val libraryFullName = this@LibraryConfiguration.fullName
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
			gradleReleaseChannel = GradleReleaseChannel.CURRENT.id
			outputFormatter = null

			if (!dependencyUpdatesIncludeUnstableVersions)
				rejectVersionIf {
					isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
				}
		}

		extensions.add("io.fluidsonic.gradle", FluidsonicPluginExtension(
			fullName = libraryFullName,
			name = libraryName,
			version = libraryVersion
		))
	}


	internal fun configureProject() {
		configureBasics()
	}


	companion object
}


fun Project.fluidLibrary(
	name: String,
	version: String,
	prefixName: Boolean = true,
	configure: LibraryConfiguration.() -> Unit = {}
) {
	val fullName = if (prefixName) "fluid-$name" else name

	check(project.parent == null) { "fluidLibrary(…) {} must only be used in the root project" }
	check(project.extensions.findByType<FluidsonicPluginExtension>() == null) { "fluidLibrary/fluidJvmLibrary(…) {} must only be used once" }
	check(project.name == fullName) { "Project name '${project.name}' must not differ from library name '$fullName' in fluidLibrary(…)" }

	LibraryConfiguration(
		fullName = fullName,
		name = name,
		project = project,
		version = version
	).apply(configure).configureProject()
}
