package io.fluidsonic.gradle.buildsupport


/**
 * Publishing metadata shared by the project's own `build.gradle.kts`.
 *
 * These constants and URL templates mirror the POM and Sonatype configuration used when
 * publishing this Gradle plugin. The templated URLs take the GitHub repository name
 * (`project.name`) as [repository] so the same values can be reused for any
 * `io.fluidsonic.*` project.
 */
public object PublishingMetadata {

	/** Maven package group registered with Sonatype. */
	public const val packageGroup: String = "io.fluidsonic"

	/** POM developer id. */
	public const val developerId: String = "fluidsonic"

	/** POM developer name. */
	public const val developerName: String = "Marc Knaup"

	/** POM developer email. */
	public const val developerEmail: String = "marc@knaup.io"

	/** POM license name. */
	public const val licenseName: String = "Apache License 2.0"

	/** Sonatype Nexus staging API URL for release publications. */
	public const val sonatypeNexusUrl: String = "https://ossrh-staging-api.central.sonatype.com/service/local/"

	/** Sonatype repository URL for snapshot publications. */
	public const val sonatypeSnapshotRepositoryUrl: String = "https://central.sonatype.com/repository/maven-snapshots/"

	/** POM license URL for the given GitHub [repository] name. */
	public fun licenseUrl(repository: String): String =
		"https://github.com/fluidsonic/$repository/blob/master/LICENSE"

	/** POM project URL for the given GitHub [repository] name. */
	public fun projectUrl(repository: String): String =
		"https://github.com/fluidsonic/$repository"

	/** POM SCM connection for the given GitHub [repository] name. */
	public fun scmConnection(repository: String): String =
		"scm:git:https://github.com/fluidsonic/$repository.git"

	/** POM SCM developer connection for the given GitHub [repository] name. */
	public fun scmDeveloperConnection(repository: String): String =
		"scm:git:git@github.com:fluidsonic/$repository.git"
}
