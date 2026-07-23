package io.fluidsonic.gradle

import kotlin.test.*


class LibraryModuleConfigurationBuilderTest {

	@Test
	fun build_withNoConfiguration_producesDefaults() {
		val config = LibraryModuleConfigurationBuilder(description = null).build()
		assertEquals(actual = config.description, expected = null)
		assertEquals(actual = config.isPublishingEnabled, expected = true)
		assertEquals(actual = config.isDokkaEnabled, expected = true)
		assertEquals(actual = config.customConfigurations.size, expected = 0)
	}

	@Test
	fun build_setsDescription() {
		val config = LibraryModuleConfigurationBuilder(description = "My module").build()
		assertEquals(actual = config.description, expected = "My module")
	}

	@Test
	fun disablePublishing_disablesPublishing() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			disablePublishing()
		}.build()
		assertEquals(actual = config.isPublishingEnabled, expected = false)
	}

	@Test
	@Suppress("DEPRECATION")
	fun withoutPublishing_stillDisablesPublishing() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			withoutPublishing()
		}.build()
		assertEquals(actual = config.isPublishingEnabled, expected = false)
	}

	@Test
	fun disableDokka_disablesDokka() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			disableDokka()
		}.build()
		assertEquals(actual = config.isDokkaEnabled, expected = false)
	}

	@Test
	@Suppress("DEPRECATION")
	fun noDokka_stillDisablesDokka() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			noDokka()
		}.build()
		assertEquals(actual = config.isDokkaEnabled, expected = false)
	}

	@Test
	fun disableExplicitApi_disablesExplicitApi() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			language {
				disableExplicitApi()
			}
		}.build()
		assertEquals(actual = config.language.isExplicitApiEnabled, expected = false)
	}

	@Test
	@Suppress("DEPRECATION")
	fun withoutExplicitApi_stillDisablesExplicitApi() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			language {
				withoutExplicitApi()
			}
		}.build()
		assertEquals(actual = config.language.isExplicitApiEnabled, expected = false)
	}

	@Test
	fun disableKotlinDependencyVersionAlignment_disablesAlignment() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			targets {
				jvm {
					disableKotlinDependencyVersionAlignment()
				}
			}
		}.build()
		assertEquals(actual = config.targets.jvm?.enforcesSameVersionForAllKotlinDependencies, expected = false)
	}

	@Test
	@Suppress("DEPRECATION")
	fun withoutEnforcingSameVersionForAllKotlinDependencies_stillDisablesAlignment() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			targets {
				jvm {
					withoutEnforcingSameVersionForAllKotlinDependencies()
				}
			}
		}.build()
		assertEquals(actual = config.targets.jvm?.enforcesSameVersionForAllKotlinDependencies, expected = false)
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

	@Test
	fun jvm_defaultsToJdk21() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			targets {
				jvm()
			}
		}.build()
		assertEquals(actual = config.targets.jvm?.jdkVersion, expected = JdkVersion.v21)
	}

	@Test
	fun jvm_jdkSelectsRequestedVersion() {
		val config = LibraryModuleConfigurationBuilder(description = null).apply {
			targets {
				jvm {
					jdk(22)
				}
			}
		}.build()
		assertEquals(actual = config.targets.jvm?.jdkVersion, expected = JdkVersion.v22)
	}

	@Test
	fun jvm_jdkRejectsUnsupportedVersion() {
		val exception = assertFailsWith<IllegalArgumentException> {
			LibraryModuleConfigurationBuilder(description = null).apply {
				targets {
					jvm {
						jdk(20)
					}
				}
			}.build()
		}
		assertEquals(
			actual = exception.message,
			expected = "Unsupported JDK version 20. Supported versions are 21 to 24.",
		)
	}
}
