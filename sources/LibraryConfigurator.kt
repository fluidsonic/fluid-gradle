package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign

/**
 * Applies a resolved [LibraryConfiguration] to the root [project]: the dependency-updates plugin, the Gradle wrapper
 * version, Maven Central publishing to Sonatype, and the [LibraryPluginExtension] that exposes the configuration to
 * each module.
 */
internal class LibraryConfigurator(private val configuration: LibraryConfiguration, private val project: Project) {

	/** Applies the configuration to the root project. */
	fun configure() = project.run {
		apply<VersionsPlugin>()

		useMavenCentralRepository()

		wrapper {
			distributionType = Wrapper.DistributionType.ALL
			gradleVersion = configuration.gradleVersion
		}

		dependencyUpdates {
			gradleReleaseChannel = GradleReleaseChannel.CURRENT.id

			if (!configuration.dependencyUpdatesIncludeUnstableVersions) {
				rejectVersionIf {
					isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
				}
			}
		}

		configurePublishing()

		extensions.add("io.fluidsonic.gradle", LibraryPluginExtension(configuration = configuration))
	}

	private fun Project.configurePublishing() {
		val sonatypeUsername: String? = System.getenv("SONATYPE_USERNAME")
		val sonatypePassword: String? = System.getenv("SONATYPE_PASSWORD")

		apply<MavenPublishPlugin>()
		apply<NexusPublishPlugin>()

		if (sonatypeUsername != null && sonatypePassword != null) {
			nexusPublishing {
				packageGroup = "io.fluidsonic"

				repositories {
					sonatype {
						username = sonatypeUsername
						password = sonatypePassword

						nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
						snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
					}
				}
			}
		}
	}
}
