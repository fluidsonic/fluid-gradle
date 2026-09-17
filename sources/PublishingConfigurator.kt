package io.fluidsonic.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningPlugin

/**
 * Configures Maven publishing for a single library module: applies the publishing and signing plugins, registers the
 * GitHub Packages repository when a token is available, makes the GitHub publish tasks republish a failed SNAPSHOT (see
 * [retryGithubSnapshotPublications]), fills in the POM, signs all publications, and attaches a Javadoc JAR to the JVM
 * publication.
 *
 * Applied only when the module has publishing enabled.
 */
internal class PublishingConfigurator(
	private val project: Project,
	private val configuration: LibraryModuleConfiguration,
	private val libraryConfiguration: LibraryConfiguration,
) {

	fun configure() = with(project) {
		val githubToken: String? = System.getenv("GITHUB_PACKAGES_AUTH_TOKEN") ?: findProperty("GITHUB_PACKAGES_AUTH_TOKEN") as String?

		apply<MavenPublishPlugin>()
		apply<SigningPlugin>()

		publishing {
			if (githubToken != null) {
				repositories {
					maven {
						name = "github"
						setUrl("https://maven.pkg.github.com/fluidsonic/${libraryConfiguration.fullName}")
						credentials {
							username = "unused"
							password = githubToken
						}
					}
				}
			}

			publications.withType<MavenPublication> {
				pom {
					name.set(libraryConfiguration.fullName)
					description.set(project.description)
					url.set("https://github.com/fluidsonic/${libraryConfiguration.fullName}")
					developers {
						developer {
							id.set("fluidsonic")
							name.set("Marc Knaup")
							email.set("marc@knaup.io")
						}
					}
					licenses {
						license {
							name.set("Apache License 2.0")
							url.set("https://github.com/fluidsonic/${libraryConfiguration.fullName}/blob/master/LICENSE")
						}
					}
					scm {
						connection.set("scm:git:https://github.com/fluidsonic/${libraryConfiguration.fullName}.git")
						developerConnection.set("scm:git:git@github.com:fluidsonic/${libraryConfiguration.fullName}.git")
						url.set("https://github.com/fluidsonic/${libraryConfiguration.fullName}")
					}
				}
			}
		}

		retryGithubSnapshotPublications()

		signing {
			sign(publishing.publications)
		}

		if (configuration.targets.jvm != null) {
			val javadocJar = tasks.register<Jar>("javadocJar") {
				if (configuration.isDokkaEnabled) {
					from(tasks.named("dokkaGeneratePublicationHtml"))
				}

				archiveClassifier.set("javadoc")
			}

			publishing.publications
				.filterIsInstance<MavenPublication>()
				.single { it.name == "jvm" }
				.artifact(javadocJar)
		}
	}
}
