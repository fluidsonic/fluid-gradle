package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.jvm.*


internal class LibraryModuleConfiguration(
	val customConfigurations: List<KotlinMultiplatformExtension.() -> Unit>,
	val description: String?,
	val isPublishingEnabled: Boolean,
	val isPublishingSingleTargetAsModule: Boolean,
	val language: Language,
	val targets: Targets
) {

	fun mergeWith(
		other: LibraryModuleConfiguration,
		addTargetsAutomatically: Boolean
	) = LibraryModuleConfiguration(
		customConfigurations = customConfigurations + other.customConfigurations,
		description = other.description ?: description,
		language = language.mergeWith(other.language),
		isPublishingEnabled = isPublishingEnabled && other.isPublishingEnabled,
		isPublishingSingleTargetAsModule = isPublishingSingleTargetAsModule || other.isPublishingSingleTargetAsModule,
		targets = targets.mergeWith(other.targets, addAutomatically = addTargetsAutomatically)
	)


	companion object {

		val default = LibraryModuleConfiguration(
			customConfigurations = emptyList(),
			description = null,
			language = Language.default,
			isPublishingEnabled = true,
			isPublishingSingleTargetAsModule = false,
			targets = Targets.default
		)
	}


	data class Language(
		val customConfigurations: List<LanguageSettingsBuilder.() -> Unit>,
		val experimentalApisToUse: Set<String>,
		val languageFeaturesToEnable: Set<String>,
		val noExplicitApi: Boolean,
		val noNewInference: Boolean
	) {

		fun mergeWith(other: Language) = Language(
			customConfigurations = customConfigurations + other.customConfigurations,
			experimentalApisToUse = experimentalApisToUse + other.experimentalApisToUse,
			languageFeaturesToEnable = languageFeaturesToEnable + other.languageFeaturesToEnable,
			noExplicitApi = noExplicitApi || other.noExplicitApi,
			noNewInference = noNewInference || other.noNewInference
		)


		companion object {

			val default = Language(
				customConfigurations = emptyList(),
				experimentalApisToUse = emptySet(),
				languageFeaturesToEnable = emptySet(),
				noExplicitApi = false,
				noNewInference = false
			)
		}
	}


	class Targets(
		val common: Common,
		val js: Js?,
		val jvm: Jvm?,
		val jvmJdk7: Jvm?,
		val nativeDarwin: NativeDarwin?
	) {

		fun mergeWith(other: Targets, addAutomatically: Boolean) = Targets(
			common = common.mergeWith(other.common),
			js = other.js?.let { js?.mergeWith(it) } ?: other.js ?: js.takeIf { addAutomatically },
			jvm = other.jvm?.let { jvm?.mergeWith(it) } ?: other.jvm ?: jvm.takeIf { addAutomatically },
			jvmJdk7 = other.jvmJdk7?.let { jvmJdk7?.mergeWith(it) } ?: other.jvmJdk7 ?: jvmJdk7.takeIf { addAutomatically },
			nativeDarwin = other.nativeDarwin?.let { nativeDarwin?.mergeWith(it) } ?: other.nativeDarwin ?: nativeDarwin.takeIf { addAutomatically }
		)


		companion object {

			val default = Targets(
				common = Common.default,
				js = null,
				jvm = null,
				jvmJdk7 = null,
				nativeDarwin = null
			)
		}


		class Common(
			val customConfigurations: List<KotlinOnlyTarget<AbstractKotlinCompilation<*>>.() -> Unit>,
			val dependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>,
			val testDependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>
		) {

			fun mergeWith(other: Common) = Common(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencyConfigurations = dependencyConfigurations + other.dependencyConfigurations,
				testDependencyConfigurations = testDependencyConfigurations + other.testDependencyConfigurations
			)


			companion object {

				val default = Common(
					customConfigurations = emptyList(),
					dependencyConfigurations = emptyList(),
					testDependencyConfigurations = emptyList()
				)
			}
		}


		class Js(
			val customConfigurations: List<KotlinJsTargetDsl.() -> Unit>,
			val dependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>,
			val noBrowser: Boolean,
			val noNodeJs: Boolean,
			val testDependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>
		) {

			fun mergeWith(other: Js) = Js(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencyConfigurations = dependencyConfigurations + other.dependencyConfigurations,
				noBrowser = noBrowser || other.noBrowser,
				noNodeJs = noNodeJs || other.noNodeJs,
				testDependencyConfigurations = testDependencyConfigurations + other.testDependencyConfigurations
			)
		}


		class Jvm(
			val customConfigurations: List<KotlinJvmTarget.() -> Unit>,
			val dependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>,
			val includesJava: Boolean,
			val testDependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>
		) {

			fun mergeWith(other: Jvm) = Jvm(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencyConfigurations = dependencyConfigurations + other.dependencyConfigurations,
				includesJava = includesJava || other.includesJava,
				testDependencyConfigurations = testDependencyConfigurations + other.testDependencyConfigurations
			)
		}


		class NativeDarwin(
			val customConfigurations: List<KotlinNativeTarget.() -> Unit>,
			val dependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>,
			val noIosArm64: Boolean,
			val noIosX64: Boolean,
			val noMacosX64: Boolean,
			val testDependencyConfigurations: List<KotlinDependencyHandler.() -> Unit>
		) {

			fun mergeWith(other: NativeDarwin) = NativeDarwin(
				customConfigurations = customConfigurations + other.customConfigurations,
				dependencyConfigurations = dependencyConfigurations + other.dependencyConfigurations,
				noIosArm64 = noIosArm64 || other.noIosArm64,
				noIosX64 = noIosX64 || other.noIosX64,
				noMacosX64 = noMacosX64 || other.noMacosX64,
				testDependencyConfigurations = testDependencyConfigurations + other.testDependencyConfigurations
			)
		}
	}
}
