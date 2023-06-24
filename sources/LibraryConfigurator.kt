package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.*
import com.github.benmanes.gradle.versions.updates.gradle.*
import org.gradle.api.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*


internal class LibraryConfigurator(
	private val configuration: LibraryConfiguration,
	private val project: Project,
) {

	fun configure() = project.run {
		apply<VersionsPlugin>()

		repositories {
			mavenCentral()
			maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers/")
		}

		wrapper {
			distributionType = Wrapper.DistributionType.ALL
			gradleVersion = configuration.gradleVersion
		}

		dependencyUpdates {
			gradleReleaseChannel = GradleReleaseChannel.CURRENT.id

			if (!configuration.dependencyUpdatesIncludeUnstableVersions)
				rejectVersionIf {
					isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
				}
		}

		extensions.add("io.fluidsonic.gradle", LibraryPluginExtension(configuration = configuration))
	}
}
