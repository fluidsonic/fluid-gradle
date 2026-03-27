package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.jvm.*


internal class LibraryModuleConfiguration(
	val customConfigurations: List<KotlinMultiplatformExtension.() -> Unit>,
	val description: String?,
	val isPublishingEnabled: Boolean,
	val language: Language,
	val noDokka: Boolean,
	val targets: Targets,
) {

	fun mergeWith(
		other: LibraryModuleConfiguration,
		addTargetsAutomatically: Boolean,
	) = LibraryModuleConfiguration(
		customConfigurations = customConfigurations + other.customConfigurations,
		description = other.description ?: description,
		language = language.mergeWith(other.language),
		isPublishingEnabled = isPublishingEnabled && other.isPublishingEnabled,
		noDokka = noDokka || other.noDokka,
		targets = targets.mergeWith(other.targets, addAutomatically = addTargetsAutomatically)
	)


	companion object {

		val default = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			language = Language.default,
			isPublishingEnabled = true,
			noDokka = false,
			targets = Targets.default
		)
	}


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


	class Language(
		val customConfigurations: List<LanguageSettingsBuilder.() -> Unit>,
		val experimentalApisToUse: Set<String>,
		val languageFeaturesToEnable: Set<String>,
		val noExplicitApi: Boolean,
		val version: String?,
	) {

		fun mergeWith(other: Language) = Language(
			customConfigurations = customConfigurations + other.customConfigurations,
			experimentalApisToUse = experimentalApisToUse + other.experimentalApisToUse,
			languageFeaturesToEnable = languageFeaturesToEnable + other.languageFeaturesToEnable,
			noExplicitApi = noExplicitApi || other.noExplicitApi,
			version = version ?: other.version
		)


		companion object {

			val default = Language(
				customConfigurations = emptyList(),
				experimentalApisToUse = emptySet(),
				languageFeaturesToEnable = emptySet(),
				noExplicitApi = false,
				version = null
			)
		}
	}


	sealed class Target(
		val enforcesSameVersionForAllKotlinDependencies: Boolean,
	) {

		class Common(
			val customConfigurations: List<KotlinOnlyTarget<KotlinMetadataCompilation<Any>>.() -> Unit>,
			val dependencies: Dependencies = Dependencies.default,
			enforcesSameVersionForAllKotlinDependencies: Boolean,
			val testDependencies: Dependencies = Dependencies.default,
		) : Target(
			enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies
		) {

			fun mergeWith(other: Common) = Common(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencies = dependencies.mergeWith(other.dependencies),
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies && other.enforcesSameVersionForAllKotlinDependencies,
				testDependencies = testDependencies.mergeWith(other.testDependencies)
			)


			companion object {

				val default = Common(
					customConfigurations = emptyList(),
					dependencies = Dependencies.default,
					enforcesSameVersionForAllKotlinDependencies = true,
					testDependencies = Dependencies.default
				)
			}
		}


		class Js(
			val customConfigurations: List<KotlinJsTargetDsl.() -> Unit>,
			val dependencies: Dependencies = Dependencies.default,
			enforcesSameVersionForAllKotlinDependencies: Boolean,
			val noBrowser: Boolean,
			val noNodeJs: Boolean,
			val testDependencies: Dependencies = Dependencies.default,
		) : Target(
			enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies
		) {

			fun mergeWith(other: Js) = Js(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencies = dependencies.mergeWith(other.dependencies),
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies && other.enforcesSameVersionForAllKotlinDependencies,
				noBrowser = noBrowser || other.noBrowser,
				noNodeJs = noNodeJs || other.noNodeJs,
				testDependencies = testDependencies.mergeWith(other.testDependencies)
			)
		}


		class Jvm(
			val customConfigurations: List<KotlinJvmTarget.() -> Unit>,
			val dependencies: Dependencies = Dependencies.default,
			enforcesSameVersionForAllKotlinDependencies: Boolean,
			val testDependencies: Dependencies = Dependencies.default,
		) : Target(
			enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies
		) {

			fun mergeWith(other: Jvm) = Jvm(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencies = dependencies.mergeWith(other.dependencies),
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies && other.enforcesSameVersionForAllKotlinDependencies,
				testDependencies = testDependencies.mergeWith(other.testDependencies)
			)
		}
	}


	class Targets(
		val common: Target.Common,
		val js: Target.Js?,
		val jvm: Target.Jvm?,
	) {

		fun mergeWith(other: Targets, addAutomatically: Boolean) = Targets(
			common = common.mergeWith(other.common),
			js = other.js?.let { js?.mergeWith(it) } ?: other.js ?: js.takeIf { addAutomatically },
			jvm = other.jvm?.let { jvm?.mergeWith(it) } ?: other.jvm ?: jvm.takeIf { addAutomatically }
		)


		companion object {

			val default = Targets(
				common = Target.Common.default,
				js = null,
				jvm = null
			)
		}
	}
}
