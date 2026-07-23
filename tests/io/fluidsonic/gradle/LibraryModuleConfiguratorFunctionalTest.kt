package io.fluidsonic.gradle

import org.gradle.testkit.runner.*
import java.io.*
import kotlin.test.*


class LibraryModuleConfiguratorFunctionalTest {

	@Test
	fun jvmTarget_registersKotlinJvmCompileAndTestTasks() {
		val projectDirectory = createLibraryProject(
			moduleBuildScript = """
				import io.fluidsonic.gradle.fluidLibraryModule

				plugins {
					id("io.fluidsonic.gradle")
				}

				fluidLibraryModule(description = "Test module") {
					targets {
						jvm()
					}
				}
			""".trimIndent()
		)

		val result = runGradle(projectDirectory, ":module:jvmTest", "--dry-run")

		assertTrue(
			actual = result.output.contains(":module:compileKotlinJvm"),
			message = "expected compileKotlinJvm task in output but got:\n${result.output}",
		)
		assertTrue(
			actual = result.output.contains(":module:compileTestKotlinJvm"),
			message = "expected compileTestKotlinJvm task in output but got:\n${result.output}",
		)
		assertTrue(
			actual = result.output.contains(":module:jvmTest"),
			message = "expected jvmTest task in output but got:\n${result.output}",
		)
	}


	@Test
	fun publishingEnabled_registersMavenPublicationTasks() {
		val projectDirectory = createLibraryProject(
			moduleBuildScript = """
				import io.fluidsonic.gradle.fluidLibraryModule

				plugins {
					id("io.fluidsonic.gradle")
				}

				fluidLibraryModule(description = "Test module") {
					targets {
						jvm()
					}
				}
			""".trimIndent()
		)

		val result = runGradle(projectDirectory, ":module:publishToMavenLocal", "--dry-run")

		assertTrue(
			actual = result.output.contains(":module:publishJvmPublicationToMavenLocal"),
			message = "expected the JVM MavenPublication task in output but got:\n${result.output}",
		)
	}


	@Test
	fun disablePublishing_removesPublishTasks() {
		val projectDirectory = createLibraryProject(
			moduleBuildScript = """
				import io.fluidsonic.gradle.fluidLibraryModule

				plugins {
					id("io.fluidsonic.gradle")
				}

				fluidLibraryModule(description = "Test module") {
					disablePublishing()

					targets {
						jvm()
					}
				}
			""".trimIndent()
		)

		val result = runGradleAndFail(projectDirectory, ":module:publishToMavenLocal", "--dry-run")

		assertTrue(
			actual = result.output.contains("Cannot locate tasks that match ':module:publishToMavenLocal'")
				|| result.output.contains("Task 'publishToMavenLocal' not found"),
			message = "expected publishToMavenLocal to be absent but got:\n${result.output}",
		)
	}


	private fun createLibraryProject(moduleBuildScript: String): File {
		val projectDirectory = createTempDirectory()

		projectDirectory.resolve("settings.gradle.kts").writeText(
			"""
				rootProject.name = "fluid-test"
				include(":module")
			""".trimIndent()
		)

		projectDirectory.resolve("build.gradle.kts").writeText(
			"""
				import io.fluidsonic.gradle.fluidLibrary

				plugins {
					id("io.fluidsonic.gradle")
				}

				fluidLibrary(name = "test", version = "1.0.0")
			""".trimIndent()
		)

		val moduleDirectory = projectDirectory.resolve("module")
		moduleDirectory.mkdirs()
		moduleDirectory.resolve("build.gradle.kts").writeText(moduleBuildScript)

		return projectDirectory
	}


	private fun createTempDirectory(): File {
		val directory = File.createTempFile("fluid-gradle-functional", "")
		directory.delete()
		directory.mkdirs()
		directory.deleteOnExit()

		return directory
	}


	private fun runGradle(projectDirectory: File, vararg arguments: String): BuildResult =
		gradleRunner(projectDirectory, *arguments).build()


	private fun runGradleAndFail(projectDirectory: File, vararg arguments: String): BuildResult =
		gradleRunner(projectDirectory, *arguments).buildAndFail()


	private fun gradleRunner(projectDirectory: File, vararg arguments: String): GradleRunner =
		GradleRunner.create()
			.withProjectDir(projectDirectory)
			.withPluginClasspath()
			.withArguments(*arguments)
}
