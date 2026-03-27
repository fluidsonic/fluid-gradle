package io.fluidsonic.gradle

import kotlin.test.*


class LibraryModuleConfigurationBuilderTest {

	@Test
	fun build_withNoConfiguration_producesDefaults() {
		val config = LibraryModuleConfigurationBuilder(description = null).build()
		assertEquals(actual = config.description, expected = null)
		assertEquals(actual = config.isPublishingEnabled, expected = true)
		assertEquals(actual = config.noDokka, expected = false)
		assertEquals(actual = config.customConfigurations.size, expected = 0)
	}

	@Test
	fun build_setsDescription() {
		val config = LibraryModuleConfigurationBuilder(description = "My module").build()
		assertEquals(actual = config.description, expected = "My module")
	}

	@Test
	fun withoutPublishing_disablesPublishing() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			withoutPublishing()
		}.build()
		assertEquals(actual = config.isPublishingEnabled, expected = false)
	}

	@Test
	fun noDokka_setsNoDokkaTrue() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			noDokka()
		}.build()
		assertEquals(actual = config.noDokka, expected = true)
	}

	@Test
	fun custom_addsCustomConfiguration() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			custom {}
		}.build()
		assertEquals(actual = config.customConfigurations.size, expected = 1)
	}

	@Test
	fun language_configuresLanguage() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			language {
				withExperimentalApi("some.api")
				version("2.0")
			}
		}.build()
		assertTrue(config.language.experimentalApisToUse.contains("some.api"))
		assertEquals(actual = config.language.version, expected = "2.0")
	}

	@Test
	fun targets_configuresTargets() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			targets {
				jvm()
			}
		}.build()
		assertNotNull(config.targets.jvm)
	}
}
