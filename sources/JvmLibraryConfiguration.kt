package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*


class JvmLibraryConfiguration internal constructor(
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
		val libraryName = this@JvmLibraryConfiguration.name
		val libraryVersion = this@JvmLibraryConfiguration.version

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
			gradleVersion = this@JvmLibraryConfiguration.gradleVersion
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

		subprojects {
			apply<KotlinPlatformJvmPlugin>()
			apply<JavaLibraryPlugin>()
		}
	}


	internal fun configureProject() {
		configureBasics()
	}


	companion object
}


fun Project.fluidJvmLibrary(name: String, version: String, configure: JvmLibraryConfiguration.() -> Unit = {}) {
	check(project.parent == null) { "fluidJvmLibrary(…) {} must only be used in the root project" }
	check(project.extensions.findByType<FluidsonicPluginExtension>() == null) { "fluidLibrary/fluidJvmLibrary(…) {} must only be used once" }
	check(project.name == "fluid-$name") { "Project name '${project.name}' must not differ from library name 'fluid-$name' in fluidJvmLibrary(…)" }

	JvmLibraryConfiguration(name = name, project = project, version = version).apply(configure).configureProject()
}
