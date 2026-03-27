package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import kotlin.test.*


class LibraryModuleConfigurationTest {

	// Language.mergeWith()

	@Test
	fun language_mergeWith_combinesExperimentalApis() {
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = setOf("api1"),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = setOf("api2"),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = null,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.experimentalApisToUse, expected = setOf("api1", "api2"))
	}

	@Test
	fun language_mergeWith_combinesLanguageFeatures() {
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = setOf("feature1"),
			noExplicitApi = false,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = setOf("feature2"),
			noExplicitApi = false,
			version = null,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.languageFeaturesToEnable, expected = setOf("feature1", "feature2"))
	}

	@Test
	fun language_mergeWith_concatenatesCustomConfigurations() {
		val config1: LanguageSettingsBuilder.() -> Unit = {}
		val config2: LanguageSettingsBuilder.() -> Unit = {}
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = listOf(config1),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = listOf(config2),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = null,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.customConfigurations.size, expected = 2)
	}

	@Test
	fun language_mergeWith_usesReceiverVersionWhenBothSet() {
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = "1.9",
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = "2.0",
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.version, expected = "1.9")
	}

	@Test
	fun language_mergeWith_usesOtherVersionWhenReceiverIsNull() {
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = "2.0",
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.version, expected = "2.0")
	}

	@Test
	fun language_mergeWith_orsNoExplicitApi() {
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = true,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			languageFeaturesToEnable = emptySet(),
			noExplicitApi = false,
			version = null,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.noExplicitApi, expected = true)
	}


	// Dependencies.mergeWith()

	@Test
	fun dependencies_mergeWith_concatenatesConfigurations() {
		val config1: KotlinDependencyHandler.() -> Unit = {}
		val config2: KotlinDependencyHandler.() -> Unit = {}
		val a = LibraryModuleConfiguration.Dependencies(
			configurations = listOf(config1),
			kaptConfigurations = emptyList(),
			kspConfigurations = emptyList(),
		)
		val b = LibraryModuleConfiguration.Dependencies(
			configurations = listOf(config2),
			kaptConfigurations = emptyList(),
			kspConfigurations = emptyList(),
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.configurations.size, expected = 2)
	}

	@Test
	fun dependencies_mergeWith_concatenatesKaptConfigurations() {
		val config1: DependencyHandler.() -> Unit = {}
		val config2: DependencyHandler.() -> Unit = {}
		val a = LibraryModuleConfiguration.Dependencies(
			configurations = emptyList(),
			kaptConfigurations = listOf(config1),
			kspConfigurations = emptyList(),
		)
		val b = LibraryModuleConfiguration.Dependencies(
			configurations = emptyList(),
			kaptConfigurations = listOf(config2),
			kspConfigurations = emptyList(),
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.kaptConfigurations.size, expected = 2)
	}

	@Test
	fun dependencies_mergeWith_concatenatesKspConfigurations() {
		val config1: DependencyHandler.() -> Unit = {}
		val config2: DependencyHandler.() -> Unit = {}
		val a = LibraryModuleConfiguration.Dependencies(
			configurations = emptyList(),
			kaptConfigurations = emptyList(),
			kspConfigurations = listOf(config1),
		)
		val b = LibraryModuleConfiguration.Dependencies(
			configurations = emptyList(),
			kaptConfigurations = emptyList(),
			kspConfigurations = listOf(config2),
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.kspConfigurations.size, expected = 2)
	}


	// Target.Common.mergeWith()

	@Test
	fun targetCommon_mergeWith_concatenatesCustomConfigurations() {
		val a = LibraryModuleConfiguration.Target.Common(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val b = LibraryModuleConfiguration.Target.Common(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.customConfigurations.size, expected = 2)
	}

	@Test
	fun targetCommon_mergeWith_andsEnforcesSameVersionForAllKotlinDependencies() {
		val a = LibraryModuleConfiguration.Target.Common(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val b = LibraryModuleConfiguration.Target.Common(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = false,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.enforcesSameVersionForAllKotlinDependencies, expected = false)
	}


	// Target.Jvm.mergeWith()

	@Test
	fun targetJvm_mergeWith_concatenatesCustomConfigurations() {
		val a = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val b = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.customConfigurations.size, expected = 2)
	}

	@Test
	fun targetJvm_mergeWith_andsEnforcesSameVersionForAllKotlinDependencies() {
		val a = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val b = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = false,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.enforcesSameVersionForAllKotlinDependencies, expected = false)
	}


	// Target.Js.mergeWith()

	@Test
	fun targetJs_mergeWith_orsNoBrowser() {
		val a = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = true,
			noNodeJs = false,
		)
		val b = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = false,
			noNodeJs = false,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.noBrowser, expected = true)
	}

	@Test
	fun targetJs_mergeWith_orsNoNodeJs() {
		val a = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = false,
			noNodeJs = false,
		)
		val b = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = false,
			noNodeJs = true,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.noNodeJs, expected = true)
	}

	@Test
	fun targetJs_mergeWith_concatenatesCustomConfigurations() {
		val a = LibraryModuleConfiguration.Target.Js(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = false,
			noNodeJs = false,
		)
		val b = LibraryModuleConfiguration.Target.Js(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = false,
			noNodeJs = false,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.customConfigurations.size, expected = 2)
	}


	// Targets.mergeWith()

	@Test
	fun targets_mergeWith_alwaysMergesCommon() {
		val a = LibraryModuleConfiguration.Targets(
			common = LibraryModuleConfiguration.Target.Common(
				customConfigurations = listOf({}),
				enforcesSameVersionForAllKotlinDependencies = true,
			),
			js = null,
			jvm = null,
		)
		val b = LibraryModuleConfiguration.Targets(
			common = LibraryModuleConfiguration.Target.Common(
				customConfigurations = listOf({}),
				enforcesSameVersionForAllKotlinDependencies = true,
			),
			js = null,
			jvm = null,
		)
		val merged = a.mergeWith(b, addAutomatically = false)
		assertEquals(actual = merged.common.customConfigurations.size, expected = 2)
	}

	@Test
	fun targets_mergeWith_mergesJsWhenBothPresent() {
		val jsA = LibraryModuleConfiguration.Target.Js(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = false,
			noNodeJs = false,
		)
		val jsB = LibraryModuleConfiguration.Target.Js(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
			noBrowser = false,
			noNodeJs = false,
		)
		val a = LibraryModuleConfiguration.Targets(
			common = LibraryModuleConfiguration.Target.Common.default,
			js = jsA,
			jvm = null,
		)
		val b = LibraryModuleConfiguration.Targets(
			common = LibraryModuleConfiguration.Target.Common.default,
			js = jsB,
			jvm = null,
		)
		val merged = a.mergeWith(b, addAutomatically = false)
		assertNotNull(merged.js)
		assertEquals(actual = merged.js!!.customConfigurations.size, expected = 2)
	}

	@Test
	fun targets_mergeWith_mergesJvmWhenBothPresent() {
		val jvmA = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val jvmB = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
		)
		val a = LibraryModuleConfiguration.Targets(
			common = LibraryModuleConfiguration.Target.Common.default,
			js = null,
			jvm = jvmA,
		)
		val b = LibraryModuleConfiguration.Targets(
			common = LibraryModuleConfiguration.Target.Common.default,
			js = null,
			jvm = jvmB,
		)
		val merged = a.mergeWith(b, addAutomatically = false)
		assertNotNull(merged.jvm)
		assertEquals(actual = merged.jvm!!.customConfigurations.size, expected = 2)
	}


	// Top-level mergeWith()

	@Test
	fun mergeWith_concatenatesCustomConfigurations() {
		val a = LibraryModuleConfiguration(
			customConfigurations = listOf({}),
			description = null,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = false,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = listOf({}),
			description = null,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = false,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val merged = a.mergeWith(b, addTargetsAutomatically = false)
		assertEquals(actual = merged.customConfigurations.size, expected = 2)
	}

	@Test
	fun mergeWith_usesOtherDescription() {
		val a = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = "original",
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = false,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = "override",
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = false,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val merged = a.mergeWith(b, addTargetsAutomatically = false)
		assertEquals(actual = merged.description, expected = "override")
	}

	@Test
	fun mergeWith_andsIsPublishingEnabled() {
		val a = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = false,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isPublishingEnabled = false,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = false,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val merged = a.mergeWith(b, addTargetsAutomatically = false)
		assertEquals(actual = merged.isPublishingEnabled, expected = false)
	}

	@Test
	fun mergeWith_orsNoDokka() {
		val a = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = false,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			noDokka = true,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val merged = a.mergeWith(b, addTargetsAutomatically = false)
		assertEquals(actual = merged.noDokka, expected = true)
	}
}
