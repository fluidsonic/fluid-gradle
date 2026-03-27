package io.fluidsonic.gradle

import kotlin.test.*


class LibraryConfigurationBuilderTest {

	@Test
	fun build_withMinimalConfiguration() {
		val config = LibraryConfigurationBuilder(
			fullName = "fluid-test",
			name = "test",
			version = "1.0.0",
		).build()
		assertEquals(actual = config.fullName, expected = "fluid-test")
		assertEquals(actual = config.name, expected = "test")
		assertEquals(actual = config.version, expected = "1.0.0")
	}

	@Test
	fun build_usesDefaultGradleVersion() {
		val config = LibraryConfigurationBuilder(
			fullName = "fluid-test",
			name = "test",
			version = "1.0.0",
		).build()
		assertEquals(actual = config.gradleVersion, expected = Versions.gradle)
	}

	@Test
	fun gradleVersion_overridesDefault() {
		val config = LibraryConfigurationBuilder(
			fullName = "fluid-test",
			name = "test",
			version = "1.0.0",
		).apply {
			gradleVersion("8.5")
		}.build()
		assertEquals(actual = config.gradleVersion, expected = "8.5")
	}

	@Test
	fun allModules_configuresDefaultModuleConfiguration() {
		val config = LibraryConfigurationBuilder(
			fullName = "fluid-test",
			name = "test",
			version = "1.0.0",
		).apply {
			allModules {
				noDokka()
			}
		}.build()
		assertEquals(actual = config.defaultModuleConfiguration.noDokka, expected = true)
	}
}
