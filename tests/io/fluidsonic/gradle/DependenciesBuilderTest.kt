package io.fluidsonic.gradle

import kotlin.test.*


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
}
