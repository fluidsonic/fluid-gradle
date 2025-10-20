package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.*
import com.github.benmanes.gradle.versions.updates.gradle.*
import io.github.gradlenexus.publishplugin.*
import org.gradle.api.*
import org.gradle.api.publish.maven.plugins.*
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

		configurePublishing()

		extensions.add("io.fluidsonic.gradle", LibraryPluginExtension(configuration = configuration))
	}


	private fun Project.configurePublishing() {
		val sonatypeUsername: String? = System.getenv("SONATYPE_USERNAME")
		val sonatypePassword: String? = System.getenv("SONATYPE_PASSWORD")

		apply<MavenPublishPlugin>()
		apply<NexusPublishPlugin>()

		if (sonatypeUsername != null && sonatypePassword != null)
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
