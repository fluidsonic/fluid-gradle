package io.fluidsonic.gradle

import io.fluidsonic.gradle.LibraryModuleDsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.jvm.*


internal class LibraryModuleConfigurationBuilder(
	private val description: String?
) : LibraryModuleDsl {

	private val customConfigurations: MutableList<KotlinMultiplatformExtension.() -> Unit> = mutableListOf()
	private var isPublishingEnabled = true
	private var isPublishingSingleTargetAsModule = false
	private var languageConfiguration: LibraryModuleConfiguration.Language? = null
	private var targetsConfiguration: LibraryModuleConfiguration.Targets? = null


	fun build() = LibraryModuleConfiguration(
		customConfigurations = customConfigurations.toList(),
		description = description,
		isPublishingEnabled = isPublishingEnabled,
		isPublishingSingleTargetAsModule = isPublishingSingleTargetAsModule,
		language = languageConfiguration ?: LibraryModuleConfiguration.Language.default,
		targets = targetsConfiguration ?: LibraryModuleConfiguration.Targets.default
	)


	override fun custom(configure: KotlinMultiplatformExtension.() -> Unit) {
		customConfigurations += configure
	}


	override fun language(configure: LanguageDsl.() -> Unit) {
		LanguageBuilder().apply(configure).build().also { configuration ->
			languageConfiguration = languageConfiguration?.mergeWith(configuration) ?: configuration
		}
	}


	override fun publishSingleTargetAsModule() {
		isPublishingSingleTargetAsModule = true
	}


	override fun targets(configure: TargetsDsl.() -> Unit) {
		TargetsBuilder().apply(configure).build().also { configuration ->
			targetsConfiguration = targetsConfiguration?.mergeWith(configuration, addAutomatically = true) ?: configuration
		}
	}


	override fun withoutPublishing() {
		isPublishingEnabled = false
	}


	class LanguageBuilder : LanguageDsl {

		private val customConfigurations: MutableList<LanguageSettingsBuilder.() -> Unit> = mutableListOf()
		private val experimentalApisToUse: MutableSet<String> = mutableSetOf()
		private val languageFeaturesToEnable: MutableSet<String> = mutableSetOf()
		private var noExplicitApi = false
		private var noNewInference = false


		fun build() = LibraryModuleConfiguration.Language(
			customConfigurations = customConfigurations.toList(),
			experimentalApisToUse = experimentalApisToUse.toSet(),
			languageFeaturesToEnable = languageFeaturesToEnable.toSet(),
			noExplicitApi = noExplicitApi,
			noNewInference = noNewInference
		)


		override fun custom(configure: LanguageSettingsBuilder.() -> Unit) {
			customConfigurations += configure
		}


		override fun withExperimentalApi(name: String) {
			experimentalApisToUse += name
		}


		override fun withLanguageFeature(name: String) {
			languageFeaturesToEnable += name
		}


		override fun withoutExplicitApi() {
			noExplicitApi = true
		}


		override fun withoutNewInference() {
			noNewInference = true
		}
	}


	class TargetsBuilder : TargetsDsl {

		private var commonConfiguration: LibraryModuleConfiguration.Targets.Common? = null
		private var jsConfiguration: LibraryModuleConfiguration.Targets.Js? = null
		private var jvmConfiguration: LibraryModuleConfiguration.Targets.Jvm? = null
		private var jvmJdk7Configuration: LibraryModuleConfiguration.Targets.Jvm? = null
		private var nativeDarwinConfiguration: LibraryModuleConfiguration.Targets.NativeDarwin? = null


		fun build() = LibraryModuleConfiguration.Targets(
			common = commonConfiguration ?: LibraryModuleConfiguration.Targets.Common.default,
			js = jsConfiguration,
			jvm = jvmConfiguration,
			jvmJdk7 = jvmJdk7Configuration,
			nativeDarwin = nativeDarwinConfiguration
		)


		override fun common(configure: CommonTargetDsl.() -> Unit) {
			CommonBuilder().apply(configure).build().also { configuration ->
				commonConfiguration = commonConfiguration?.mergeWith(configuration) ?: configuration
			}
		}


		override fun js(configure: JsTargetDsl.() -> Unit) {
			JsBuilder().apply(configure).build().also { configuration ->
				jsConfiguration = jsConfiguration?.mergeWith(configuration) ?: configuration
			}
		}


		override fun jvm(configure: JvmTargetDsl.() -> Unit) {
			JvmBuilder().apply(configure).build().also { configuration ->
				jvmConfiguration = jvmConfiguration?.mergeWith(configuration) ?: configuration
			}
		}


		override fun jvmJdk7(configure: JvmTargetDsl.() -> Unit) {
			JvmBuilder().apply(configure).build().also { configuration ->
				jvmJdk7Configuration = jvmJdk7Configuration?.mergeWith(configuration) ?: configuration
			}
		}


		override fun nativeDarwin(configure: NativeDarwinTargetDsl.() -> Unit) {
			NativeDarwinBuilder().apply(configure).build().also { configuration ->
				nativeDarwinConfiguration = nativeDarwinConfiguration?.mergeWith(configuration) ?: configuration
			}
		}


		class CommonBuilder : CommonTargetDsl {

			private val customConfigurations: MutableList<KotlinOnlyTarget<AbstractKotlinCompilation<*>>.() -> Unit> = mutableListOf()
			private val dependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()
			private val testDependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()


			fun build() = LibraryModuleConfiguration.Targets.Common(
				customConfigurations = customConfigurations.toList(),
				dependencyConfigurations = dependencyConfigurations.toList(),
				testDependencyConfigurations = testDependencyConfigurations.toList()
			)


			override fun custom(configure: KotlinOnlyTarget<AbstractKotlinCompilation<*>>.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
				dependencyConfigurations += configure
			}


			override fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) {
				testDependencyConfigurations += configure
			}
		}


		class JsBuilder : JsTargetDsl {

			private val customConfigurations: MutableList<KotlinJsTargetDsl.() -> Unit> = mutableListOf()
			private val dependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()
			private var noBrowser = false
			private var noNodeJs = false
			private val testDependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()


			fun build() = LibraryModuleConfiguration.Targets.Js(
				customConfigurations = customConfigurations.toList(),
				dependencyConfigurations = dependencyConfigurations.toList(),
				noBrowser = noBrowser,
				noNodeJs = noNodeJs,
				testDependencyConfigurations = testDependencyConfigurations.toList()
			)


			override fun custom(configure: KotlinJsTargetDsl.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
				dependencyConfigurations += configure
			}


			override fun withoutBrowser() {
				noBrowser = true
			}


			override fun withoutNodeJs() {
				noNodeJs = true
			}


			override fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) {
				testDependencyConfigurations += configure
			}
		}


		class JvmBuilder : JvmTargetDsl {

			private val customConfigurations: MutableList<KotlinJvmTarget.() -> Unit> = mutableListOf()
			private val dependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()
			private var includesJava = false
			private val testDependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()


			fun build() = LibraryModuleConfiguration.Targets.Jvm(
				customConfigurations = customConfigurations.toList(),
				dependencyConfigurations = dependencyConfigurations.toList(),
				includesJava = includesJava,
				testDependencyConfigurations = testDependencyConfigurations.toList()
			)


			override fun custom(configure: KotlinJvmTarget.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
				dependencyConfigurations += configure
			}


			override fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) {
				testDependencyConfigurations += configure
			}


			override fun withJava() {
				includesJava = true
			}
		}


		class NativeDarwinBuilder : NativeDarwinTargetDsl {

			private val customConfigurations: MutableList<KotlinNativeTarget.() -> Unit> = mutableListOf()
			private val dependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()
			private var noIosArm64 = false
			private var noIosX64 = false
			private var noMacosX64 = false
			private val testDependencyConfigurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()


			fun build() = LibraryModuleConfiguration.Targets.NativeDarwin(
				customConfigurations = customConfigurations.toList(),
				dependencyConfigurations = dependencyConfigurations.toList(),
				noIosArm64 = noIosArm64,
				noIosX64 = noIosX64,
				noMacosX64 = noMacosX64,
				testDependencyConfigurations = testDependencyConfigurations.toList()
			)


			override fun custom(configure: KotlinNativeTarget.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
				dependencyConfigurations += configure
			}


			override fun withoutIosArm64() {
				noIosArm64 = true
			}


			override fun withoutIosX64() {
				noIosX64 = true
			}


			override fun withoutMacosX64() {
				noMacosX64 = true
			}


			override fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) {
				testDependencyConfigurations += configure
			}
		}
	}
}
