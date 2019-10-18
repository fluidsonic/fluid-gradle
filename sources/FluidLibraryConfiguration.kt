package com.github.fluidsonic.fluid.library

import com.github.benmanes.gradle.versions.*
import org.gradle.api.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*


class FluidLibraryConfiguration private constructor(
	private val project: Project
) {

	var dependencyUpdatesIncludeUnstableVersions = false
	var gradleVersion = "5.6.3"
	var name = ""
	var version = ""


	private fun Project.configureBasics() {
		val libraryName = this@FluidLibraryConfiguration.name
		val libraryVersion = this@FluidLibraryConfiguration.version

		check(libraryName.isNotEmpty()) { "'name' must be set" }
		check(libraryVersion.isNotEmpty()) { "'version' must be set" }

		apply<VersionsPlugin>()

		repositories {
			mavenCentral()
			jcenter()
			bintray("fluidsonic/maven")
			bintray("kotlin/kotlin-eap")
			bintray("kotlin/kotlinx")
		}

		tasks.withType<Wrapper> {
			this.distributionType = Wrapper.DistributionType.ALL
			this.gradleVersion = this@FluidLibraryConfiguration.gradleVersion
		}

		dependencyUpdates {
			outputFormatter = null

			if (!dependencyUpdatesIncludeUnstableVersions)
				rejectVersionIf {
					isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
				}
		}

		extensions.add("fluid-library", FluidLibraryPluginExtension(
			name = libraryName,
			version = libraryVersion
		))
	}


	private fun configureProject(): Unit = project.run {
		configureBasics()
	}


	companion object {

		internal fun applyTo(project: Project, configure: FluidLibraryConfiguration.() -> Unit = {}) {
			check(project.parent == null) { "fluidLibrary {} must only be used in the root project" }
			check(project.extensions.findByType<FluidLibraryPluginExtension>() == null) { "fluidLibrary/fluidJvmLibrary {} must only be used once" }

			FluidLibraryConfiguration(project = project).apply(configure).configureProject()
		}
	}
}
