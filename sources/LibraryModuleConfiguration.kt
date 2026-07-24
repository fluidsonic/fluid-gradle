package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/** Resolved configuration for a single library module, produced by [LibraryModuleConfigurationBuilder]. */
internal class LibraryModuleConfiguration(
	val customConfigurations: List<KotlinMultiplatformExtension.() -> Unit>,
	val description: String?,
	val isDokkaEnabled: Boolean,
	val isPublishingEnabled: Boolean,
	val language: Language,
	val targets: Targets,
) {

	/**
	 * Returns a new configuration combining this one with [other].
	 *
	 * Boolean feature flags are combined with logical AND — a feature stays enabled only when enabled in both inputs,
	 * so disabling it in either place wins. List and set settings are concatenated or unioned, and nested
	 * configurations merge recursively.
	 *
	 * @param addTargetsAutomatically When `true`, a target present only in this configuration is carried into the
	 *   result even if [other] declares no targets; when `false`, only targets [other] also declares survive.
	 */
	fun mergeWith(other: LibraryModuleConfiguration, addTargetsAutomatically: Boolean) = LibraryModuleConfiguration(
		customConfigurations = customConfigurations + other.customConfigurations,
		description = other.description ?: description,
		language = language.mergeWith(other.language),
		isDokkaEnabled = isDokkaEnabled && other.isDokkaEnabled,
		isPublishingEnabled = isPublishingEnabled && other.isPublishingEnabled,
		targets = targets.mergeWith(other.targets, addAutomatically = addTargetsAutomatically),
	)

	companion object {

		val default = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			isDokkaEnabled = true,
			language = Language.default,
			isPublishingEnabled = true,
			targets = Targets.default,
		)
	}

	/** Dependency declarations for a single source set, kept as deferred configuration blocks per dependency kind. */
	class Dependencies(
		val configurations: List<KotlinDependencyHandler.() -> Unit>,
		val kaptConfigurations: List<DependencyHandler.() -> Unit>,
		val kspConfigurations: List<DependencyHandler.() -> Unit>,
	) {

		fun mergeWith(other: Dependencies) = Dependencies(
			configurations = configurations + other.configurations,
			kaptConfigurations = kaptConfigurations + other.kaptConfigurations,
			kspConfigurations = kspConfigurations + other.kspConfigurations,
		)

		companion object {

			val default = Dependencies(
				configurations = emptyList(),
				kaptConfigurations = emptyList(),
				kspConfigurations = emptyList(),
			)
		}
	}

	/** Kotlin language settings for a module: opt-in experimental APIs, explicit-API mode, and language version. */
	class Language(
		val customConfigurations: List<LanguageSettingsBuilder.() -> Unit>,
		val experimentalApisToUse: Set<String>,
		val isExplicitApiEnabled: Boolean,
		val version: String?,
	) {

		fun mergeWith(other: Language) = Language(
			customConfigurations = customConfigurations + other.customConfigurations,
			experimentalApisToUse = experimentalApisToUse + other.experimentalApisToUse,
			isExplicitApiEnabled = isExplicitApiEnabled && other.isExplicitApiEnabled,
			version = other.version ?: version,
		)

		companion object {

			val default = Language(
				customConfigurations = emptyList(),
				experimentalApisToUse = emptySet(),
				isExplicitApiEnabled = true,
				version = null,
			)
		}
	}

	/** Base for a single compilation target's resolved configuration. */
	sealed class Target(val enforcesSameVersionForAllKotlinDependencies: Boolean) {

		/** Configuration for the common (metadata) target. */
		class Common(
			val customConfigurations: List<KotlinOnlyTarget<KotlinMetadataCompilation<Any>>.() -> Unit>,
			val dependencies: Dependencies = Dependencies.default,
			enforcesSameVersionForAllKotlinDependencies: Boolean,
			val testDependencies: Dependencies = Dependencies.default,
		) : Target(
			enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
		) {

			fun mergeWith(other: Common) = Common(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencies = dependencies.mergeWith(other.dependencies),
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies && other.enforcesSameVersionForAllKotlinDependencies,
				testDependencies = testDependencies.mergeWith(other.testDependencies),
			)

			companion object {

				val default = Common(
					customConfigurations = emptyList(),
					dependencies = Dependencies.default,
					enforcesSameVersionForAllKotlinDependencies = true,
					testDependencies = Dependencies.default,
				)
			}
		}

		/** Configuration for the Kotlin/JS target, including which browser and Node.js sub-targets are enabled. */
		class Js(
			val customConfigurations: List<KotlinJsTargetDsl.() -> Unit>,
			val dependencies: Dependencies = Dependencies.default,
			enforcesSameVersionForAllKotlinDependencies: Boolean,
			val isBrowserEnabled: Boolean,
			val isNodeJsEnabled: Boolean,
			val testDependencies: Dependencies = Dependencies.default,
		) : Target(
			enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
		) {

			fun mergeWith(other: Js) = Js(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencies = dependencies.mergeWith(other.dependencies),
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies && other.enforcesSameVersionForAllKotlinDependencies,
				isBrowserEnabled = isBrowserEnabled && other.isBrowserEnabled,
				isNodeJsEnabled = isNodeJsEnabled && other.isNodeJsEnabled,
				testDependencies = testDependencies.mergeWith(other.testDependencies),
			)
		}

		/** Configuration for the Kotlin/JVM target, including the [jdkVersion] it compiles against. */
		class Jvm(
			val customConfigurations: List<KotlinJvmTarget.() -> Unit>,
			val dependencies: Dependencies = Dependencies.default,
			enforcesSameVersionForAllKotlinDependencies: Boolean,
			val jdkVersion: JdkVersion = JdkVersion.v21,
			val testDependencies: Dependencies = Dependencies.default,
		) : Target(
			enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
		) {

			// The higher JDK version wins on merge: a JDK selection is a minimum requirement, so taking the
			// maximum never violates the minimum requested by either the module or the `allModules` default.
			fun mergeWith(other: Jvm) = Jvm(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencies = dependencies.mergeWith(other.dependencies),
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies && other.enforcesSameVersionForAllKotlinDependencies,
				jdkVersion = maxOf(jdkVersion, other.jdkVersion),
				testDependencies = testDependencies.mergeWith(other.testDependencies),
			)
		}
	}

	/** The set of compilation targets for a module. [js] and [jvm] are `null` when that target is not enabled. */
	class Targets(val common: Target.Common, val js: Target.Js?, val jvm: Target.Jvm?) {

		fun mergeWith(other: Targets, addAutomatically: Boolean) = Targets(
			common = common.mergeWith(other.common),
			js = other.js?.let { js?.mergeWith(it) } ?: other.js ?: js.takeIf { addAutomatically },
			jvm = other.jvm?.let { jvm?.mergeWith(it) } ?: other.jvm ?: jvm.takeIf { addAutomatically },
		)

		companion object {

			val default = Targets(
				common = Target.Common.default,
				js = null,
				jvm = null,
			)
		}
	}
}
