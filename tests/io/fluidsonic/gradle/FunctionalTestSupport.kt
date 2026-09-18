package io.fluidsonic.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

/** A fresh, empty project directory for a TestKit build, deleted with its contents when the test JVM exits. */
internal fun createFunctionalTestDirectory(): File {
	val directory = File.createTempFile("fluid-gradle-functional", "")
	directory.delete()
	directory.mkdirs()
	Runtime.getRuntime().addShutdownHook(Thread { directory.deleteRecursively() })

	return directory
}

internal fun runGradle(projectDirectory: File, vararg arguments: String): BuildResult = gradleRunner(projectDirectory, *arguments).build()

internal fun runGradleAndFail(projectDirectory: File, vararg arguments: String): BuildResult = gradleRunner(projectDirectory, *arguments).buildAndFail()

internal fun gradleRunner(projectDirectory: File, vararg arguments: String): GradleRunner = GradleRunner.create()
	.withProjectDir(projectDirectory)
	.withPluginClasspath()
	.withArguments(*arguments)
	.let { runner -> testKitDirectory?.let(runner::withTestKitDir) ?: runner }

/**
 * The Gradle user home of the nested builds, set by the `test` task (see `build.gradle.kts`). TestKit's default lives
 * under the test JVM's temp dir inside `build/`, where `clean` discards the nested builds' dependency cache.
 */
private val testKitDirectory: File? = System.getProperty("io.fluidsonic.gradle.testKitDirectory")?.let(::File)

internal fun String.occurrenceCount(needle: String): Int = split(needle).size - 1
