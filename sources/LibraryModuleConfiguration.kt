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
	) {

		fun mergeWith(other: Dependencies) = Dependencies(
			configurations = configurations + other.configurations,
			kaptConfigurations = kaptConfigurations + other.kaptConfigurations
		)


		companion object {

			val default = Dependencies(
				configurations = emptyList(),
				kaptConfigurations = emptyList()
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


		class Darwin(
			val customConfigurations: List<KotlinNativeTarget.() -> Unit>,
			val dependencies: Dependencies = Dependencies.default,
			enforcesSameVersionForAllKotlinDependencies: Boolean,
			val noIosArm64: Boolean,
			val noIosSimulatorArm64: Boolean,
			val noIosX64: Boolean,
			val noMacosArm64: Boolean,
			val noMacosX64: Boolean,
			val noTvosArm64: Boolean,
			val noTvosSimulatorArm64: Boolean,
			val noTvosX64: Boolean,
			val noWatchosArm32: Boolean,
			val noWatchosArm64: Boolean,
			val noWatchosSimulatorArm64: Boolean,
			val noWatchosX64: Boolean,
			val testDependencies: Dependencies = Dependencies.default,
		) : Target(
			enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies
		) {

			fun mergeWith(other: Darwin) = Darwin(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencies = dependencies.mergeWith(other.dependencies),
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies && other.enforcesSameVersionForAllKotlinDependencies,
				noIosArm64 = noIosArm64 || other.noIosArm64,
				noIosSimulatorArm64 = noIosSimulatorArm64 || other.noIosSimulatorArm64,
				noIosX64 = noIosX64 || other.noIosX64,
				noMacosArm64 = noMacosArm64 || other.noMacosArm64,
				noMacosX64 = noMacosX64 || other.noMacosX64,
				noTvosArm64 = noTvosArm64 || other.noTvosArm64,
				noTvosSimulatorArm64 = noTvosSimulatorArm64 || other.noTvosSimulatorArm64,
				noTvosX64 = noTvosX64 || other.noTvosX64,
				noWatchosArm32 = noWatchosArm32 || other.noWatchosArm32,
				noWatchosArm64 = noWatchosArm64 || other.noWatchosArm64,
				noWatchosSimulatorArm64 = noWatchosSimulatorArm64 || other.noWatchosSimulatorArm64,
				noWatchosX64 = noWatchosX64 || other.noWatchosX64,
				testDependencies = testDependencies.mergeWith(other.testDependencies)
			)
		}


		class Js(
			val compiler: KotlinJsCompilerType?,
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
				compiler = compiler ?: other.compiler,
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
		val darwin: Target.Darwin?,
		val js: Target.Js?,
		val jvm: Target.Jvm?,
	) {

		fun mergeWith(other: Targets, addAutomatically: Boolean) = Targets(
			common = common.mergeWith(other.common),
			darwin = other.darwin?.let { darwin?.mergeWith(it) } ?: other.darwin ?: darwin.takeIf { addAutomatically },
			js = other.js?.let { js?.mergeWith(it) } ?: other.js ?: js.takeIf { addAutomatically },
			jvm = other.jvm?.let { jvm?.mergeWith(it) } ?: other.jvm ?: jvm.takeIf { addAutomatically }
		)


		companion object {

			val default = Targets(
				common = Target.Common.default,
				darwin = null,
				js = null,
				jvm = null
			)
		}
	}
}
