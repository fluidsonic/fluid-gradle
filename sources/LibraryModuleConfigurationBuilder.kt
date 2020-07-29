package io.fluidsonic.gradle

import io.fluidsonic.gradle.LibraryModuleDsl.*
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.*
import org.gradle.kotlin.dsl.*
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
			private var dependencies = LibraryModuleConfiguration.Dependencies.default
			private var testDependencies = LibraryModuleConfiguration.Dependencies.default


			fun build() = LibraryModuleConfiguration.Targets.Common(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				testDependencies = testDependencies
			)


			override fun custom(configure: KotlinOnlyTarget<AbstractKotlinCompilation<*>>.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: DependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.dependencies = this.dependencies.mergeWith(dependencies)
			}


			override fun testDependencies(configure: DependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.testDependencies = this.testDependencies.mergeWith(dependencies)
			}
		}


		class DependenciesBuilder : JvmDependenciesDsl {

			private val configurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()
			private val kaptConfigurations: MutableList<DependencyHandler.() -> Unit> = mutableListOf()


			fun build() = LibraryModuleConfiguration.Dependencies(
				configurations = configurations.toList(),
				kaptConfigurations = kaptConfigurations.toList()
			)

			override fun api(notation: Any) {
				configurations += { api(resolve(notation)) }
			}

			override fun api(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit) {
				configurations += { api(dependencyNotation, configure) }
			}

			override fun compileOnly(notation: Any) {
				configurations += { compileOnly(resolve(notation)) }
			}

			override fun compileOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit) {
				configurations += { compileOnly(dependencyNotation, configure) }
			}

			override fun custom(configure: KotlinDependencyHandler.() -> Unit) {
				configurations += configure
			}

			override fun implementation(notation: Any) {
				configurations += { implementation(resolve(notation)) }
			}

			override fun implementation(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit) {
				configurations += { implementation(dependencyNotation, configure) }
			}

			override fun kapt(notation: Any) {
				kaptConfigurations += { add("kapt", resolve(notation)) }
			}

			override fun kapt(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit) {
				kaptConfigurations += { add("kapt", dependencyNotation, configure) }
			}

			override fun runtimeOnly(notation: Any) {
				configurations += { runtimeOnly(resolve(notation)) }
			}

			override fun runtimeOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit) {
				configurations += { runtimeOnly(dependencyNotation, configure) }
			}


			private fun DependencyHandler.resolve(notation: Any) = when (notation) {
				is SpecialDependency.Kotlin -> kotlin(notation.simpleModuleName, notation.version)
				is SpecialDependency.Project -> project(notation.notation)
				else -> notation
			}


			private fun KotlinDependencyHandler.resolve(notation: Any) = when (notation) {
				is SpecialDependency.Kotlin -> kotlin(notation.simpleModuleName, notation.version)
				is SpecialDependency.Project -> project(notation.notation)
				else -> notation
			}


			override fun fluid(simpleModuleName: String, version: String, usePrefix: Boolean): Any =
				if (usePrefix) "io.fluidsonic.${simpleModuleName.substringBefore('-')}:fluid-$simpleModuleName:$version"
				else "io.fluidsonic.${simpleModuleName.substringBefore('-')}:$simpleModuleName:$version"


			override fun kotlin(simpleModuleName: String, version: String?): Any =
				SpecialDependency.Kotlin(simpleModuleName = simpleModuleName, version = version)


			override fun kotlinx(simpleModuleName: String, version: String, usePrefix: Boolean): Any =
				if (usePrefix) "org.jetbrains.kotlinx:kotlinx-$simpleModuleName:$version"
				else "org.jetbrains.kotlinx:$simpleModuleName:$version"


			override fun project(notation: Map<String, Any?>): Any =
				SpecialDependency.Project(notation = notation)


			private sealed class SpecialDependency {

				class Kotlin(val simpleModuleName: String, val version: String?) : SpecialDependency()
				class Project(val notation: Map<String, Any?>) : SpecialDependency()
			}
		}


		class JsBuilder : JsTargetDsl {

			private val customConfigurations: MutableList<KotlinJsTargetDsl.() -> Unit> = mutableListOf()
			private var dependencies = LibraryModuleConfiguration.Dependencies.default
			private var noBrowser = false
			private var noNodeJs = false
			private var testDependencies = LibraryModuleConfiguration.Dependencies.default


			fun build() = LibraryModuleConfiguration.Targets.Js(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				noBrowser = noBrowser,
				noNodeJs = noNodeJs,
				testDependencies = testDependencies
			)


			override fun custom(configure: KotlinJsTargetDsl.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: DependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.dependencies = this.dependencies.mergeWith(dependencies)
			}


			override fun testDependencies(configure: DependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.testDependencies = this.testDependencies.mergeWith(dependencies)
			}


			override fun withoutBrowser() {
				noBrowser = true
			}


			override fun withoutNodeJs() {
				noNodeJs = true
			}
		}


		class JvmBuilder : JvmTargetDsl {

			private val customConfigurations: MutableList<KotlinJvmTarget.() -> Unit> = mutableListOf()
			private var dependencies = LibraryModuleConfiguration.Dependencies.default
			private var includesJava = false
			private var testDependencies = LibraryModuleConfiguration.Dependencies.default


			fun build() = LibraryModuleConfiguration.Targets.Jvm(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				includesJava = includesJava,
				testDependencies = testDependencies
			)


			override fun custom(configure: KotlinJvmTarget.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: JvmDependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.dependencies = this.dependencies.mergeWith(dependencies)
			}


			override fun testDependencies(configure: JvmDependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.testDependencies = this.testDependencies.mergeWith(dependencies)
			}


			override fun withJava() {
				includesJava = true
			}
		}


		class NativeDarwinBuilder : NativeDarwinTargetDsl {

			private val customConfigurations: MutableList<KotlinNativeTarget.() -> Unit> = mutableListOf()
			private var dependencies = LibraryModuleConfiguration.Dependencies.default
			private var noIosArm64 = false
			private var noIosX64 = false
			private var noMacosX64 = false
			private var testDependencies = LibraryModuleConfiguration.Dependencies.default


			fun build() = LibraryModuleConfiguration.Targets.NativeDarwin(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				noIosArm64 = noIosArm64,
				noIosX64 = noIosX64,
				noMacosX64 = noMacosX64,
				testDependencies = testDependencies
			)


			override fun custom(configure: KotlinNativeTarget.() -> Unit) {
				customConfigurations += configure
			}


			override fun dependencies(configure: DependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.dependencies = this.dependencies.mergeWith(dependencies)
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


			override fun testDependencies(configure: DependenciesDsl.() -> Unit) {
				val dependencies = DependenciesBuilder().apply(configure).build()

				this.testDependencies = this.testDependencies.mergeWith(dependencies)
			}
		}
	}
}
