package io.fluidsonic.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class DependenciesBuilderTest {

	@Test
	fun fluid_returnsCorrectMavenCoordinateWithPrefix() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		val result = builder.fluid("json", "1.0")
		assertEquals(actual = result, expected = "io.fluidsonic.json:fluid-json:1.0")
	}

	@Test
	fun fluid_returnsCorrectMavenCoordinateWithoutPrefix() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		val result = builder.fluid("json", "1.0", usePrefix = false)
		assertEquals(actual = result, expected = "io.fluidsonic.json:json:1.0")
	}

	@Test
	fun fluid_handlesModuleNameWithHyphen() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		val result = builder.fluid("country-data", "1.0")
		assertEquals(actual = result, expected = "io.fluidsonic.country:fluid-country-data:1.0")
	}

	@Test
	fun kotlinx_returnsCorrectCoordinateWithPrefix() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		val result = builder.kotlinx("coroutines-core", "1.7")
		assertEquals(actual = result, expected = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7")
	}

	@Test
	fun kotlinx_returnsCorrectCoordinateWithoutPrefix() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		val result = builder.kotlinx("coroutines-core", "1.7", usePrefix = false)
		assertEquals(actual = result, expected = "org.jetbrains.kotlinx:coroutines-core:1.7")
	}

	@Test
	fun api_addsToConfigurations() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		builder.api("some:dep:1.0")
		val deps = builder.build()
		assertEquals(actual = deps.configurations.size, expected = 1)
	}

	@Test
	fun implementation_addsToConfigurations() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		builder.implementation("some:dep:1.0")
		val deps = builder.build()
		assertEquals(actual = deps.configurations.size, expected = 1)
	}

	@Test
	fun compileOnly_addsToConfigurations() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		builder.compileOnly("some:dep:1.0")
		val deps = builder.build()
		assertEquals(actual = deps.configurations.size, expected = 1)

		val handler = RecordingKotlinDependencyHandler()
		deps.configurations.single().invoke(handler)
		assertEquals(
			actual = handler.additions,
			expected = listOf(RecordingKotlinDependencyHandler.Addition(configuration = "compileOnly", notation = "some:dep:1.0")),
		)
	}

	@Test
	fun runtimeOnly_addsToConfigurations() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		builder.runtimeOnly("some:dep:1.0")
		val deps = builder.build()
		assertEquals(actual = deps.configurations.size, expected = 1)

		val handler = RecordingKotlinDependencyHandler()
		deps.configurations.single().invoke(handler)
		assertEquals(
			actual = handler.additions,
			expected = listOf(RecordingKotlinDependencyHandler.Addition(configuration = "runtimeOnly", notation = "some:dep:1.0")),
		)
	}

	@Test
	fun kapt_addsToKaptConfigurations() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		builder.kapt("some:dep:1.0")
		val deps = builder.build()
		assertEquals(actual = deps.kaptConfigurations.size, expected = 1)
	}

	@Test
	fun ksp_addsToKspConfigurations() {
		val builder = LibraryModuleConfigurationBuilder.TargetBuilder.DependenciesBuilder()
		builder.ksp("some:dep:1.0")
		val deps = builder.build()
		assertEquals(actual = deps.kspConfigurations.size, expected = 1)
	}

	/**
	 * A [KotlinDependencyHandler] test double that records which configuration method (`api`/`compileOnly`/`runtimeOnly`)
	 * each resolved notation was routed to, so a plain notation stored by [DependenciesBuilder] can be verified to reach
	 * the correct configuration bucket rather than merely counting how many configuration lambdas were stored. Only the
	 * members reachable from these tests are implemented; every other member fails loudly if unexpectedly touched.
	 */
	@Suppress("DEPRECATION")
	private class RecordingKotlinDependencyHandler : KotlinDependencyHandler {

		val additions: MutableList<Addition> = mutableListOf()

		/** A single notation together with the configuration method (`api`/`compileOnly`/`runtimeOnly`) it was routed to. */
		data class Addition(val configuration: String, val notation: Any?)

		override val project: Project
			get() = throw UnsupportedOperationException("not exercised by these tests")

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

		override fun implementation(dependencyNotation: Any): Dependency? {
			additions += Addition(configuration = "implementation", notation = dependencyNotation)

			return null
		}

		override fun kotlin(simpleModuleName: String, version: String?): ExternalModuleDependency =
			throw UnsupportedOperationException("not exercised by these tests")

		override fun project(notation: Map<String, Any?>): ProjectDependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun npm(name: String, version: String): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun npm(name: String, directory: File): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun npm(directory: File): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun devNpm(name: String, version: String): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun devNpm(name: String, directory: File): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun devNpm(directory: File): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun optionalNpm(name: String, version: String): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun optionalNpm(name: String, directory: File): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun optionalNpm(directory: File): Dependency = throw UnsupportedOperationException("not exercised by these tests")

		override fun peerNpm(name: String, version: String): Dependency = throw UnsupportedOperationException("not exercised by these tests")

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
