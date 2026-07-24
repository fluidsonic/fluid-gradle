package io.fluidsonic.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
			""".trimIndent(),
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
			""".trimIndent(),
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
			""".trimIndent(),
		)

		val result = runGradleAndFail(projectDirectory, ":module:publishToMavenLocal", "--dry-run")

		assertTrue(
			actual = result.output.contains("Cannot locate tasks that match ':module:publishToMavenLocal'") ||
				result.output.contains("Task 'publishToMavenLocal' not found"),
			message = "expected publishToMavenLocal to be absent but got:\n${result.output}",
		)
	}

	@Test
	fun dokkaAggregation_registersDokkaEnabledModulesOnRoot() {
		val projectDirectory = createLibraryProject(
			rootBuildScript = """
				import io.fluidsonic.gradle.fluidLibrary
				import org.gradle.api.artifacts.ProjectDependency

				plugins {
					id("io.fluidsonic.gradle")
				}

				fluidLibrary(name = "test", version = "1.0.0")

				tasks.register("printDokkaAggregation") {
					doLast {
						configurations["dokka"].dependencies
							.filterIsInstance<ProjectDependency>()
							.forEach { println("DOKKA_AGG ${'$'}{it.path}") }
					}
				}
			""".trimIndent(),
			modules = mapOf(
				"moduleA" to plainModuleBuildScript(description = "Module A"),
				"moduleB" to plainModuleBuildScript(description = "Module B"),
				"moduleNoDokka" to """
					import io.fluidsonic.gradle.fluidLibraryModule

					plugins {
						id("io.fluidsonic.gradle")
					}

					fluidLibraryModule(description = "No dokka") {
						disableDokka()

						targets {
							jvm()
						}
					}
				""".trimIndent(),
			),
		)

		val result = runGradle(projectDirectory, ":printDokkaAggregation")

		assertTrue(
			actual = result.output.contains("DOKKA_AGG :moduleA"),
			message = "expected :moduleA in the root dokka aggregation but got:\n${result.output}",
		)
		assertTrue(
			actual = result.output.contains("DOKKA_AGG :moduleB"),
			message = "expected :moduleB in the root dokka aggregation but got:\n${result.output}",
		)
		assertFalse(
			actual = result.output.contains("DOKKA_AGG :moduleNoDokka"),
			message = "expected :moduleNoDokka to be excluded from the root dokka aggregation but got:\n${result.output}",
		)
	}

	@Test
	fun rootDokkaConfiguration_absentWhenAllModulesDisableDokka() {
		val projectDirectory = createLibraryProject(
			rootBuildScript = """
				import io.fluidsonic.gradle.fluidLibrary

				plugins {
					id("io.fluidsonic.gradle")
				}

				fluidLibrary(name = "test", version = "1.0.0") {
					allModules {
						disableDokka()
					}
				}

				tasks.register("printDokkaConfigPresence") {
					doLast {
						if (configurations.findByName("dokka") != null)
							println("DOKKA_CONFIG_PRESENT")
						else
							println("DOKKA_CONFIG_ABSENT")
					}
				}
			""".trimIndent(),
			modules = mapOf(
				"moduleA" to plainModuleBuildScript(description = "Module A"),
				"moduleB" to plainModuleBuildScript(description = "Module B"),
			),
		)

		val result = runGradle(projectDirectory, ":printDokkaConfigPresence")

		assertTrue(
			actual = result.output.contains("DOKKA_CONFIG_ABSENT"),
			message = "expected the root dokka configuration to be absent but got:\n${result.output}",
		)
		assertFalse(
			actual = result.output.contains("DOKKA_CONFIG_PRESENT"),
			message = "expected the root dokka configuration to be absent but got:\n${result.output}",
		)
	}

	@Test
	fun rootDokkaAggregation_isConfigurationCacheCompatible() {
		val projectDirectory = createLibraryProject(
			modules = mapOf(
				"moduleA" to plainModuleBuildScript(description = "Module A"),
				"moduleB" to plainModuleBuildScript(description = "Module B"),
			),
			gradleProperties = "org.gradle.configuration-cache=true",
		)

		// `dokkaGeneratePublicationHtml` is the root aggregating Dokka v2 HTML task (see PublishingConfigurator).
		val firstRun = runGradle(projectDirectory, ":dokkaGeneratePublicationHtml", "--dry-run", "--configuration-cache")

		assertTrue(
			actual = firstRun.output.contains("Configuration cache entry stored"),
			message = "expected a configuration cache entry to be stored but got:\n${firstRun.output}",
		)
		assertFalse(
			actual = firstRun.output.contains("problems were found storing the configuration cache"),
			message = "expected no configuration cache problems but got:\n${firstRun.output}",
		)

		val secondRun = runGradle(projectDirectory, ":dokkaGeneratePublicationHtml", "--dry-run", "--configuration-cache")

		assertTrue(
			actual = secondRun.output.contains("Reusing configuration cache"),
			message = "expected the configuration cache to be reused but got:\n${secondRun.output}",
		)
	}

	private fun createLibraryProject(moduleBuildScript: String): File = createLibraryProject(modules = mapOf("module" to moduleBuildScript))

	private fun createLibraryProject(modules: Map<String, String>, rootBuildScript: String = defaultRootBuildScript, gradleProperties: String? = null): File {
		val projectDirectory = createTempDirectory()

		val settings = buildString {
			appendLine("rootProject.name = \"fluid-test\"")
			modules.keys.forEach { appendLine("include(\":$it\")") }
		}
		projectDirectory.resolve("settings.gradle.kts").writeText(settings)

		projectDirectory.resolve("build.gradle.kts").writeText(rootBuildScript)

		if (gradleProperties != null) {
			projectDirectory.resolve("gradle.properties").writeText(gradleProperties)
		}

		modules.forEach { (name, script) ->
			val moduleDirectory = projectDirectory.resolve(name)
			moduleDirectory.mkdirs()
			moduleDirectory.resolve("build.gradle.kts").writeText(script)
		}

		return projectDirectory
	}

	private fun plainModuleBuildScript(description: String) = """
		import io.fluidsonic.gradle.fluidLibraryModule

		plugins {
			id("io.fluidsonic.gradle")
		}

		fluidLibraryModule(description = "$description") {
			targets {
				jvm()
			}
		}
	""".trimIndent()

	private fun createTempDirectory(): File {
		val directory = File.createTempFile("fluid-gradle-functional", "")
		directory.delete()
		directory.mkdirs()
		directory.deleteOnExit()

		return directory
	}

	private fun runGradle(projectDirectory: File, vararg arguments: String): BuildResult = gradleRunner(projectDirectory, *arguments).build()

	private fun runGradleAndFail(projectDirectory: File, vararg arguments: String): BuildResult = gradleRunner(projectDirectory, *arguments).buildAndFail()

	private fun gradleRunner(projectDirectory: File, vararg arguments: String): GradleRunner = GradleRunner.create()
		.withProjectDir(projectDirectory)
		.withPluginClasspath()
		.withArguments(*arguments)

	private companion object {

		val defaultRootBuildScript = """
			import io.fluidsonic.gradle.fluidLibrary

			plugins {
				id("io.fluidsonic.gradle")
			}

			fluidLibrary(name = "test", version = "1.0.0")
		""".trimIndent()
	}
}
