package io.fluidsonic.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Exercises the private `resolve` dispatch inside [LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder]
 * by declaring each special dependency through the public DSL and then driving the stored
 * [KotlinDependencyHandler] configuration lambda against a recording handler.
 *
 * These tests protect the catch-all branch that turns an unhandled `SpecialDependency` subtype into an
 * error instead of silently leaking it to Gradle as a raw notation: every currently supported subtype must
 * resolve to a real Kotlin-Gradle dependency (never leak the internal marker object through the `else` branch).
 */
@Suppress("DEPRECATION")
class KotlinDependencyResolveTest {

	private val rootProject: Project = ProjectBuilder.builder().build()
	private val childProject: Project = ProjectBuilder.builder().withParent(rootProject).withName("child").build()

	private fun drive(create: LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder.() -> Any): RecordingKotlinDependencyHandler =
		driveVia(configure = {
			api(it)
		}, create = create)

	private fun driveVia(
		configure: LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder.(notation: Any) -> Unit,
		create: LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder.() -> Any,
	): RecordingKotlinDependencyHandler {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		val notation = builder.create()
		builder.configure(notation)

		val handler = RecordingKotlinDependencyHandler(project = rootProject, childProject = childProject)
		builder.build().configurations.single().invoke(handler)

		return handler
	}

	private fun assertResolvedWithout(handler: RecordingKotlinDependencyHandler, expectedCreatorCall: String) {
		assertEquals(actual = handler.creatorCalls, expected = listOf(expectedCreatorCall))
		assertEquals(actual = handler.addedNotations.size, expected = 1)
		assertTrue(
			actual = handler.addedNotations.single() is Dependency,
			message = "expected the special dependency to be resolved to a Gradle Dependency, but got ${handler.addedNotations.single()}",
		)
	}

	private fun assertResolvedVia(handler: RecordingKotlinDependencyHandler, expectedConfiguration: String, expectedCreatorCall: String) {
		assertEquals(actual = handler.creatorCalls, expected = listOf(expectedCreatorCall))
		assertEquals(actual = handler.additions.size, expected = 1)

		val addition = handler.additions.single()
		assertEquals(actual = addition.configuration, expected = expectedConfiguration)
		assertTrue(
			actual = addition.notation is Dependency,
			message = "expected the special dependency to be resolved to a Gradle Dependency, but got ${addition.notation}",
		)
	}

	@Test
	fun api_resolvesKotlinWithVersion() {
		assertResolvedWithout(drive { kotlin("stdlib", "2.0.0") }, "kotlin(stdlib,2.0.0)")
	}

	@Test
	fun api_resolvesKotlinWithoutVersion() {
		assertResolvedWithout(drive { kotlin("stdlib") }, "kotlin(stdlib,null)")
	}

	@Test
	fun api_resolvesProject() {
		assertResolvedWithout(drive { project(mapOf("path" to ":child")) }, "project({path=:child})")
	}

	@Test
	fun api_resolvesNpmNameVersion() {
		assertResolvedWithout(drive { npm("react", "17.0.0") }, "npm(react,17.0.0)")
	}

	@Test
	fun api_resolvesNpmNameDirectory() {
		assertResolvedWithout(drive { npm("react", File("npm/react")) }, "npm(react,npm/react)")
	}

	@Test
	fun api_resolvesNpmDirectory() {
		assertResolvedWithout(drive { npm(File("npm/react")) }, "npm(npm/react)")
	}

	@Test
	fun api_resolvesDevNpmNameVersion() {
		assertResolvedWithout(drive { devNpm("webpack", "5.0.0") }, "devNpm(webpack,5.0.0)")
	}

	@Test
	fun api_resolvesDevNpmNameDirectory() {
		assertResolvedWithout(drive { devNpm("webpack", File("npm/webpack")) }, "devNpm(webpack,npm/webpack)")
	}

	@Test
	fun api_resolvesDevNpmDirectory() {
		assertResolvedWithout(drive { devNpm(File("npm/webpack")) }, "devNpm(npm/webpack)")
	}

	@Test
	fun api_resolvesOptionalNpmNameVersion() {
		assertResolvedWithout(drive { optionalNpm("lodash", "4.0.0") }, "optionalNpm(lodash,4.0.0)")
	}

	@Test
	fun api_resolvesOptionalNpmNameDirectory() {
		assertResolvedWithout(drive { optionalNpm("lodash", File("npm/lodash")) }, "optionalNpm(lodash,npm/lodash)")
	}

	@Test
	fun api_resolvesOptionalNpmDirectory() {
		assertResolvedWithout(drive { optionalNpm(File("npm/lodash")) }, "optionalNpm(npm/lodash)")
	}

	@Test
	fun api_resolvesPeerNpmNameVersion() {
		assertResolvedWithout(drive { peerNpm("react", "17.0.0") }, "peerNpm(react,17.0.0)")
	}

	@Test
	fun compileOnly_resolvesNpmNameVersion() {
		assertResolvedVia(
			driveVia({ compileOnly(it) }) { npm("react", "17.0.0") },
			expectedConfiguration = "compileOnly",
			expectedCreatorCall = "npm(react,17.0.0)",
		)
	}

	@Test
	fun runtimeOnly_resolvesKotlinWithVersion() {
		assertResolvedVia(
			driveVia({ runtimeOnly(it) }) { kotlin("stdlib", "2.0.0") },
			expectedConfiguration = "runtimeOnly",
			expectedCreatorCall = "kotlin(stdlib,2.0.0)",
		)
	}

	@Test
	fun api_passesThroughPlainNotation() {
		val handler = drive { "com.example:library:1.0" }

		assertTrue(actual = handler.creatorCalls.isEmpty(), message = "a plain notation must not trigger any dependency-creator call")
		assertEquals(actual = handler.addedNotations.single(), expected = "com.example:library:1.0")
	}

	/**
	 * A [KotlinDependencyHandler] test double that records which creator method the `resolve` dispatch invoked
	 * and which resolved notation was ultimately handed to `api`. Only the members reachable from the resolve
	 * paths under test are implemented; everything else fails loudly if unexpectedly touched.
	 */
	private class RecordingKotlinDependencyHandler(override val project: Project, childProject: Project) : KotlinDependencyHandler {

		val creatorCalls: MutableList<String> = mutableListOf()
		val additions: MutableList<Addition> = mutableListOf()

		/** The resolved notations handed to a configuration method, without recording which method received them. */
		val addedNotations: List<Any?> get() = additions.map { it.notation }

		/** A single resolved dependency together with the configuration method (`api`/`compileOnly`/`runtimeOnly`) it was routed to. */
		data class Addition(val configuration: String, val notation: Any?)

		private val externalDummy = project.dependencies.create("io.fluidsonic:dummy:1.0") as ExternalModuleDependency
		private val projectDummy = project.dependencies.project(mapOf("path" to childProject.path)) as ProjectDependency

		private fun recordDependency(call: String): Dependency {
			creatorCalls += call

			return externalDummy
		}

		private fun recordExternal(call: String): ExternalModuleDependency {
			creatorCalls += call

			return externalDummy
		}

		private fun recordProject(call: String): ProjectDependency {
			creatorCalls += call

			return projectDummy
		}

		override fun api(dependencyNotation: Any): Dependency? {
			additions += Addition(configuration = "api", notation = dependencyNotation)

			return null
		}

		override fun compileOnly(dependencyNotation: Any): Dependency? {
			additions += Addition(configuration = "compileOnly", notation = dependencyNotation)

			return null
		}

		override fun runtimeOnly(dependencyNotation: Any): Dependency? {
			additions += Addition(configuration = "runtimeOnly", notation = dependencyNotation)

			return null
		}

		override fun kotlin(simpleModuleName: String, version: String?): ExternalModuleDependency = recordExternal("kotlin($simpleModuleName,$version)")

		override fun project(notation: Map<String, Any?>): ProjectDependency = recordProject("project($notation)")

		override fun npm(name: String, version: String): Dependency = recordDependency("npm($name,$version)")

		override fun npm(name: String, directory: File): Dependency = recordDependency("npm($name,$directory)")

		override fun npm(directory: File): Dependency = recordDependency("npm($directory)")

		override fun devNpm(name: String, version: String): Dependency = recordDependency("devNpm($name,$version)")

		override fun devNpm(name: String, directory: File): Dependency = recordDependency("devNpm($name,$directory)")

		override fun devNpm(directory: File): Dependency = recordDependency("devNpm($directory)")

		override fun optionalNpm(name: String, version: String): Dependency = recordDependency("optionalNpm($name,$version)")

		override fun optionalNpm(name: String, directory: File): Dependency = recordDependency("optionalNpm($name,$directory)")

		override fun optionalNpm(directory: File): Dependency = recordDependency("optionalNpm($directory)")

		override fun peerNpm(name: String, version: String): Dependency = recordDependency("peerNpm($name,$version)")

		override fun implementation(dependencyNotation: Any): Dependency? = throw UnsupportedOperationException("not exercised by these tests")

		override fun implementation(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency =
			throw UnsupportedOperationException("not exercised by these tests")

		override fun <T : Dependency> implementation(dependency: T, configure: T.() -> Unit): T = throw UnsupportedOperationException("not exercised by these tests")

		override fun api(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency =
			throw UnsupportedOperationException("not exercised by these tests")

		override fun <T : Dependency> api(dependency: T, configure: T.() -> Unit): T = throw UnsupportedOperationException("not exercised by these tests")

		override fun compileOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency =
			throw UnsupportedOperationException("not exercised by these tests")

		override fun <T : Dependency> compileOnly(dependency: T, configure: T.() -> Unit): T = throw UnsupportedOperationException("not exercised by these tests")

		override fun runtimeOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit): ExternalModuleDependency =
			throw UnsupportedOperationException("not exercised by these tests")

		override fun <T : Dependency> runtimeOnly(dependency: T, configure: T.() -> Unit): T = throw UnsupportedOperationException("not exercised by these tests")
	}
}
