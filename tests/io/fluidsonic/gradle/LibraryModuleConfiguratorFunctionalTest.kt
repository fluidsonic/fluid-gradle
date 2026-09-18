package io.fluidsonic.gradle

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
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
	fun dependencyUpdates_reportsModuleDependenciesFromRoot() {
		// Since gradle-versions-plugin 0.55.0 the root `dependencyUpdates` task aggregates per-project
		// `partialDependencyUpdates` tasks instead of resolving every project itself, so a module-only dependency proves
		// the modules still end up in the root report.
		val projectDirectory = createLibraryProject(
			modules = mapOf(
				"moduleA" to """
					import io.fluidsonic.gradle.fluidLibraryModule

					plugins {
						id("io.fluidsonic.gradle")
					}

					fluidLibraryModule(description = "Module A") {
						targets {
							jvm {
								dependencies {
									implementation("$OUTDATED_DEPENDENCY")
								}
							}
						}
					}
				""".trimIndent(),
				"moduleB" to plainModuleBuildScript(description = "Module B"),
			),
		)

		val result = runGradle(projectDirectory, ":dependencyUpdates")

		assertTrue(
			actual = result.output.contains("$OUTDATED_DEPENDENCY_MODULE [$OUTDATED_DEPENDENCY_VERSION -> "),
			message = "expected :moduleA's outdated dependency in the root dependencyUpdates report but got:\n${result.output}",
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

	@Test
	fun jvmTest_greenRunIsQuiet() {
		val projectDirectory = createJvmTestFixtureProject(includeFailingTest = false, gradleProperties = null)

		val result = runGradle(projectDirectory, ":module:jvmTest")
		val output = result.output
		val metrics = metricsOf(output)

		assertTrue(
			actual = output.contains("> Task :module:jvmTest"),
			message = "expected the jvmTest task to have run ($metrics) but got:\n$output",
		)
		assertEquals(
			actual = output.occurrenceCount(GREEN_SUMMARY_LINE),
			expected = 1,
			message = "expected exactly one summary line for all $FIXTURE_TEST_COUNT tests ($metrics) but got:\n$output",
		)
		assertFalse(
			actual = output.contains("PASSED"),
			message = "expected no per-test PASSED events ($metrics) but got:\n$output",
		)
		// A successful run of this fixture measures 21 lines. The ceiling guards against a future re-flood, not against
		// small changes in Gradle's own task output.
		assertTrue(
			actual = output.lines().size <= GREEN_RUN_LINE_CEILING,
			message = "expected a quiet green run but got $metrics:\n$output",
		)
	}

	@Test
	fun jvmTest_failingRunStaysDiagnosable() {
		val projectDirectory = createJvmTestFixtureProject(includeFailingTest = true, gradleProperties = null)

		val result = runGradleAndFail(projectDirectory, ":module:jvmTest")
		val output = result.output
		val metrics = metricsOf(output)

		assertTrue(
			actual = output.contains("FAILED"),
			message = "expected a FAILED marker ($metrics) but got:\n$output",
		)
		assertTrue(
			actual = output.contains(FAILING_ASSERTION_MESSAGE),
			message = "expected the assertion message ($metrics) but got:\n$output",
		)
		assertTrue(
			actual = output.contains(FAILING_EXPECTED_VALUE.toString()),
			message = "expected the expected value ($metrics) but got:\n$output",
		)
		assertTrue(
			actual = output.contains(FAILING_ACTUAL_VALUE.toString()),
			message = "expected the actual value ($metrics) but got:\n$output",
		)
		assertEquals(
			actual = output.occurrenceCount(":module:jvmTest — ${FIXTURE_TEST_COUNT + 1} tests, $FIXTURE_TEST_COUNT passed, 1 failed"),
			expected = 1,
			message = "expected exactly one summary line reporting the failure ($metrics) but got:\n$output",
		)
	}

	@Test
	fun jvmTest_summarySurvivesConfigurationCache() {
		val projectDirectory = createJvmTestFixtureProject(
			includeFailingTest = false,
			gradleProperties = "org.gradle.configuration-cache=true",
		)

		// `--rerun` on both runs keeps the requested task graph identical (so the cache entry is reusable) while forcing
		// the test task to execute again instead of being up-to-date, which would skip the listener entirely.
		val firstRun = runGradle(projectDirectory, ":module:jvmTest", "--rerun", "--configuration-cache")

		assertTrue(
			actual = firstRun.output.contains("Configuration cache entry stored"),
			message = "expected a configuration cache entry to be stored but got:\n${firstRun.output}",
		)
		assertFalse(
			actual = firstRun.output.contains("problems were found storing the configuration cache"),
			message = "expected no configuration cache problems but got:\n${firstRun.output}",
		)

		val secondRun = runGradle(projectDirectory, ":module:jvmTest", "--rerun", "--configuration-cache")

		assertTrue(
			actual = secondRun.output.contains("Reusing configuration cache"),
			message = "expected the configuration cache to be reused but got:\n${secondRun.output}",
		)
		assertEquals(
			actual = secondRun.output.occurrenceCount(GREEN_SUMMARY_LINE),
			expected = 1,
			message = "expected the summary line on the cache-reusing run but got:\n${secondRun.output}",
		)
	}

	@Test
	fun jvmTest_infoRestoresVerboseOutput() {
		val projectDirectory = createJvmTestFixtureProject(includeFailingTest = false, gradleProperties = null)

		val result = runGradle(projectDirectory, ":module:jvmTest", "--info")
		val output = result.output

		assertEquals(
			actual = output.occurrenceCount("PASSED"),
			expected = FIXTURE_TEST_COUNT,
			message = "expected a PASSED event per test with --info but got:\n$output",
		)
		assertEquals(
			actual = output.occurrenceCount(FIXTURE_STANDARD_OUTPUT_MARKER),
			expected = FIXTURE_TEST_COUNT,
			message = "expected the tests' standard output with --info but got:\n$output",
		)
	}

	/**
	 * Creates a single-module library project whose JVM test source set holds [FIXTURE_TEST_COUNT] passing tests — each
	 * printing to standard output — plus, when [includeFailingTest] is set, one deliberately failing test.
	 */
	private fun createJvmTestFixtureProject(includeFailingTest: Boolean, gradleProperties: String?): File {
		val projectDirectory = createLibraryProject(
			modules = mapOf("module" to plainModuleBuildScript(description = "Test module")),
			gradleProperties = gradleProperties,
		)

		// The JVM test source set maps to `tests-jvm/`, not `src/test/kotlin` (see configureSourceSetBasics).
		val testDirectory = projectDirectory.resolve("module").resolve("tests-jvm").resolve("fixture")
		testDirectory.mkdirs()
		testDirectory.resolve("GeneratedTest.kt").writeText(generateTestSource(includeFailingTest = includeFailingTest))

		return projectDirectory
	}

	private fun generateTestSource(includeFailingTest: Boolean): String = buildString {
		appendLine("package fixture")
		appendLine()
		appendLine("import kotlin.test.Test")
		appendLine("import kotlin.test.assertEquals")
		appendLine()
		appendLine("class GeneratedTest {")

		repeat(FIXTURE_TEST_COUNT) { index ->
			appendLine()
			appendLine("\t@Test")
			appendLine("\tfun passing$index() {")
			appendLine("\t\tprintln(\"$FIXTURE_STANDARD_OUTPUT_MARKER $index\")")
			appendLine("\t\tassertEquals(actual = $index + 1, expected = ${index + 1})")
			appendLine("\t}")
		}

		if (includeFailingTest) {
			appendLine()
			appendLine("\t@Test")
			appendLine("\tfun failing() {")
			appendLine("\t\tassertEquals(actual = $FAILING_ACTUAL_VALUE, expected = $FAILING_EXPECTED_VALUE, message = \"$FAILING_ASSERTION_MESSAGE\")")
			appendLine("\t}")
		}

		appendLine("}")
	}

	private fun createLibraryProject(moduleBuildScript: String): File = createLibraryProject(modules = mapOf("module" to moduleBuildScript))

	private fun createLibraryProject(modules: Map<String, String>, rootBuildScript: String = defaultRootBuildScript, gradleProperties: String? = null): File {
		val projectDirectory = createFunctionalTestDirectory()

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

	private fun metricsOf(output: String) = "${output.lines().size} lines, ${output.toByteArray().size} bytes"

	private companion object {

		const val FAILING_ACTUAL_VALUE = 54321
		const val FAILING_ASSERTION_MESSAGE = "widget count mismatch"
		const val FAILING_EXPECTED_VALUE = 12345
		const val FIXTURE_STANDARD_OUTPUT_MARKER = "FIXTURE_STDOUT"
		const val FIXTURE_TEST_COUNT = 50
		const val GREEN_RUN_LINE_CEILING = 40
		const val GREEN_SUMMARY_LINE = ":module:jvmTest — $FIXTURE_TEST_COUNT tests, all passed"
		const val OUTDATED_DEPENDENCY_MODULE = "org.jetbrains.kotlinx:kotlinx-coroutines-core"
		const val OUTDATED_DEPENDENCY_VERSION = "1.7.0"
		const val OUTDATED_DEPENDENCY = "$OUTDATED_DEPENDENCY_MODULE:$OUTDATED_DEPENDENCY_VERSION"

		val defaultRootBuildScript = """
			import io.fluidsonic.gradle.fluidLibrary

			plugins {
				id("io.fluidsonic.gradle")
			}

			fluidLibrary(name = "test", version = "1.0.0")
		""".trimIndent()
	}
}
