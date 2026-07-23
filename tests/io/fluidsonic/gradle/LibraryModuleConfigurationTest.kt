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
			isExplicitApiEnabled = true,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = setOf("api2"),
			isExplicitApiEnabled = true,
			version = null,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.experimentalApisToUse, expected = setOf("api1", "api2"))
	}

	@Test
	fun language_mergeWith_concatenatesCustomConfigurations() {
		val config1: LanguageSettingsBuilder.() -> Unit = {}
		val config2: LanguageSettingsBuilder.() -> Unit = {}
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = listOf(config1),
			experimentalApisToUse = emptySet(),
			isExplicitApiEnabled = true,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = listOf(config2),
			experimentalApisToUse = emptySet(),
			isExplicitApiEnabled = true,
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
			isExplicitApiEnabled = true,
			version = "1.9",
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			isExplicitApiEnabled = true,
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
			isExplicitApiEnabled = true,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			isExplicitApiEnabled = true,
			version = "2.0",
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.version, expected = "2.0")
	}

	@Test
	fun language_mergeWith_andsIsExplicitApiEnabled() {
		val a = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			isExplicitApiEnabled = false,
			version = null,
		)
		val b = LibraryModuleConfiguration.Language(
			customConfigurations = emptyList(),
			experimentalApisToUse = emptySet(),
			isExplicitApiEnabled = true,
			version = null,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.isExplicitApiEnabled, expected = false)
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

	@Test
	fun targetJvm_mergeWith_usesHigherJdkVersion() {
		val a = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			jdkVersion = JdkVersion.v24,
		)
		val b = LibraryModuleConfiguration.Target.Jvm(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			jdkVersion = JdkVersion.v21,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.jdkVersion, expected = JdkVersion.v24)
	}


	// Target.Js.mergeWith()

	@Test
	fun targetJs_mergeWith_andsIsBrowserEnabled() {
		val a = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			isBrowserEnabled = false,
			isNodeJsEnabled = true,
		)
		val b = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			isBrowserEnabled = true,
			isNodeJsEnabled = true,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.isBrowserEnabled, expected = false)
	}

	@Test
	fun targetJs_mergeWith_andsIsNodeJsEnabled() {
		val a = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			isBrowserEnabled = true,
			isNodeJsEnabled = true,
		)
		val b = LibraryModuleConfiguration.Target.Js(
			customConfigurations = emptyList(),
			enforcesSameVersionForAllKotlinDependencies = true,
			isBrowserEnabled = true,
			isNodeJsEnabled = false,
		)
		val merged = a.mergeWith(b)
		assertEquals(actual = merged.isNodeJsEnabled, expected = false)
	}

	@Test
	fun targetJs_mergeWith_concatenatesCustomConfigurations() {
		val a = LibraryModuleConfiguration.Target.Js(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
			isBrowserEnabled = true,
			isNodeJsEnabled = true,
		)
		val b = LibraryModuleConfiguration.Target.Js(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
			isBrowserEnabled = true,
			isNodeJsEnabled = true,
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
			isBrowserEnabled = true,
			isNodeJsEnabled = true,
		)
		val jsB = LibraryModuleConfiguration.Target.Js(
			customConfigurations = listOf({}),
			enforcesSameVersionForAllKotlinDependencies = true,
			isBrowserEnabled = true,
			isNodeJsEnabled = true,
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
			isDokkaEnabled = true,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = listOf({}),
			description = null,
			isDokkaEnabled = true,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
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
			isDokkaEnabled = true,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = "override",
			isDokkaEnabled = true,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
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
			isDokkaEnabled = true,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isDokkaEnabled = true,
			isPublishingEnabled = false,
			language = LibraryModuleConfiguration.Language.default,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val merged = a.mergeWith(b, addTargetsAutomatically = false)
		assertEquals(actual = merged.isPublishingEnabled, expected = false)
	}

	@Test
	fun mergeWith_andsIsDokkaEnabled() {
		val a = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isDokkaEnabled = true,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val b = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isDokkaEnabled = false,
			isPublishingEnabled = true,
			language = LibraryModuleConfiguration.Language.default,
			targets = LibraryModuleConfiguration.Targets.default,
		)
		val merged = a.mergeWith(b, addTargetsAutomatically = false)
		assertEquals(actual = merged.isDokkaEnabled, expected = false)
	}
}
