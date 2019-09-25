package com.github.fluidsonic.fluid.library

import com.github.benmanes.gradle.versions.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*


class FluidJvmLibraryConfiguration private constructor(
	private val project: Project
) {

	var gradleVersion = "5.6.2"
	var name = ""
	var version = ""


	private fun Project.configureBasics() {
		val libraryName = this@FluidJvmLibraryConfiguration.name
		val libraryVersion = this@FluidJvmLibraryConfiguration.version

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
			this.gradleVersion = this@FluidJvmLibraryConfiguration.gradleVersion
		}

		extensions.add("fluid-library", FluidLibraryPluginExtension(
			name = libraryName,
			version = libraryVersion
		))

		subprojects {
			apply<KotlinPlatformJvmPlugin>()
			apply<JavaLibraryPlugin>()
		}
	}


	private fun configureProject(): Unit = project.run {
		configureBasics()
	}


	companion object {

		internal fun applyTo(project: Project, configure: FluidJvmLibraryConfiguration.() -> Unit = {}) {
			check(project.parent == null) { "fluidJvmLibrary {} must only be used in the root project" }
			check(project.extensions.findByType<FluidLibraryPluginExtension>() == null) { "fluidLibrary/fluidJvmLibrary {} must only be used once" }

			FluidJvmLibraryConfiguration(project = project).apply(configure).configureProject()
		}
	}
}
