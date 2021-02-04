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
import java.io.*


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
		private var version: String? = null


		fun build() = LibraryModuleConfiguration.Language(
			customConfigurations = customConfigurations.toList(),
			experimentalApisToUse = experimentalApisToUse.toSet(),
			languageFeaturesToEnable = languageFeaturesToEnable.toSet(),
			noExplicitApi = noExplicitApi,
			version = version
		)


		override fun custom(configure: LanguageSettingsBuilder.() -> Unit) {
			customConfigurations += configure
		}


		override fun version(version: String) {
			this.version = version
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
	}


	sealed class TargetBuilder<out Dependencies : DependenciesDsl, Custom> : TargetDsl<Dependencies, Custom> {

		protected val customConfigurations: MutableList<Custom.() -> Unit> = mutableListOf()
		protected var enforcesSameVersionForAllKotlinDependencies = true
		protected var dependencies = LibraryModuleConfiguration.Dependencies.default
		protected var testDependencies = LibraryModuleConfiguration.Dependencies.default


		final override fun custom(configure: Custom.() -> Unit) {
			customConfigurations += configure
		}


		final override fun dependencies(configure: Dependencies.() -> Unit) {
			@Suppress("UNCHECKED_CAST")
			val dependencies = DependenciesBuilder().apply(configure as DependenciesDsl.() -> Unit).build()

			this.dependencies = this.dependencies.mergeWith(dependencies)
		}


		final override fun testDependencies(configure: Dependencies.() -> Unit) {
			@Suppress("UNCHECKED_CAST")
			val dependencies = DependenciesBuilder().apply(configure as DependenciesDsl.() -> Unit).build()

			this.testDependencies = this.testDependencies.mergeWith(dependencies)
		}


		final override fun withoutEnforcingSameVersionForAllKotlinDependencies() {
			enforcesSameVersionForAllKotlinDependencies = false
		}


		class CommonBuilder : TargetBuilder<DependenciesDsl, KotlinOnlyTarget<AbstractKotlinCompilation<*>>>(), CommonTargetDsl {

			fun build() = LibraryModuleConfiguration.Target.Common(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
				testDependencies = testDependencies
			)
		}


		class DependenciesBuilder : JsDependenciesDsl, JvmDependenciesDsl {

			private val configurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()
			private val kaptConfigurations: MutableList<DependencyHandler.() -> Unit> = mutableListOf()


			fun build() = LibraryModuleConfiguration.Dependencies(
				configurations = configurations.toList(),
				kaptConfigurations = kaptConfigurations.toList()
			)

			override fun devNpm(name: String, version: String): Any =
				SpecialDependency.DevNpmNameVersion(name = name, version = version)


			override fun devNpm(name: String, directory: File): Any =
				SpecialDependency.DevNpmNameDirectory(name = name, directory = directory)


			override fun devNpm(directory: File): Any =
				SpecialDependency.DevNpmDirectory(directory = directory)


			override fun npm(name: String, version: String, generateExternals: Boolean): Any =
				SpecialDependency.NpmNameVersion(generateExternals = generateExternals, name = name, version = version)


			override fun npm(name: String, directory: File, generateExternals: Boolean): Any =
				SpecialDependency.NpmNameDirectory(generateExternals = generateExternals, name = name, directory = directory)


			override fun npm(directory: File, generateExternals: Boolean): Any =
				SpecialDependency.NpmDirectory(generateExternals = generateExternals, directory = directory)


			override fun optionalNpm(name: String, version: String, generateExternals: Boolean): Any =
				SpecialDependency.OptionalNpmNameVersion(generateExternals = generateExternals, name = name, version = version)


			override fun optionalNpm(name: String, directory: File, generateExternals: Boolean): Any =
				SpecialDependency.OptionalNpmNameDirectory(generateExternals = generateExternals, name = name, directory = directory)


			override fun optionalNpm(directory: File, generateExternals: Boolean): Any =
				SpecialDependency.OptionalNpmDirectory(generateExternals = generateExternals, directory = directory)


			override fun peerNpm(name: String, version: String): Any =
				SpecialDependency.PeerNpmNameVersion(name = name, version = version)


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
				is SpecialDependency -> error("Unsupported: $notation")
				else -> notation
			}


			private fun KotlinDependencyHandler.resolve(notation: Any) = when (notation) {
				is SpecialDependency.Kotlin -> kotlin(notation.simpleModuleName, notation.version)
				is SpecialDependency.DevNpmDirectory -> devNpm(notation.directory)
				is SpecialDependency.DevNpmNameDirectory -> devNpm(notation.name, notation.directory)
				is SpecialDependency.DevNpmNameVersion -> devNpm(notation.name, notation.version)
				is SpecialDependency.NpmDirectory -> npm(notation.directory)
				is SpecialDependency.NpmNameDirectory -> npm(notation.name, notation.directory)
				is SpecialDependency.NpmNameVersion -> npm(notation.name, notation.version)
				is SpecialDependency.OptionalNpmDirectory -> optionalNpm(notation.directory)
				is SpecialDependency.OptionalNpmNameDirectory -> optionalNpm(notation.name, notation.directory)
				is SpecialDependency.OptionalNpmNameVersion -> optionalNpm(notation.name, notation.version)
				is SpecialDependency.PeerNpmNameVersion -> peerNpm(notation.name, notation.version)
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
				class DevNpmDirectory(val directory: File) : SpecialDependency()
				class DevNpmNameDirectory(val name: String, val directory: File) : SpecialDependency()
				class DevNpmNameVersion(val name: String, val version: String) : SpecialDependency()
				class NpmDirectory(val generateExternals: Boolean, val directory: File) : SpecialDependency()
				class NpmNameDirectory(val generateExternals: Boolean, val name: String, val directory: File) : SpecialDependency()
				class NpmNameVersion(val generateExternals: Boolean, val name: String, val version: String) : SpecialDependency()
				class OptionalNpmDirectory(val generateExternals: Boolean, val directory: File) : SpecialDependency()
				class OptionalNpmNameDirectory(val generateExternals: Boolean, val name: String, val directory: File) : SpecialDependency()
				class OptionalNpmNameVersion(val generateExternals: Boolean, val name: String, val version: String) : SpecialDependency()
				class PeerNpmNameVersion(val name: String, val version: String) : SpecialDependency()
				class Project(val notation: Map<String, Any?>) : SpecialDependency()
			}
		}


		class JsBuilder(
			private val compiler: KotlinJsCompilerType?
		) : TargetBuilder<JsDependenciesDsl, KotlinJsTargetDsl>(), JsTargetDsl {

			private var noBrowser = false
			private var noNodeJs = false


			fun build() = LibraryModuleConfiguration.Target.Js(
				compiler = compiler,
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
				noBrowser = noBrowser,
				noNodeJs = noNodeJs,
				testDependencies = testDependencies
			)


			override fun withoutBrowser() {
				noBrowser = true
			}


			override fun withoutNodeJs() {
				noNodeJs = true
			}
		}


		class JvmBuilder : TargetBuilder<JvmDependenciesDsl, KotlinJvmTarget>(), JvmTargetDsl {

			private var includesJava = false
			private var noIR = false


			fun build() = LibraryModuleConfiguration.Target.Jvm(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
				includesJava = includesJava,
				noIR = noIR,
				testDependencies = testDependencies
			)


			override fun noIR() {
				noIR = true
			}


			override fun withJava() {
				includesJava = true
			}
		}


		class DarwinBuilder : TargetBuilder<DependenciesDsl, KotlinNativeTarget>(), DarwinTargetDsl {

			private var noIosArm32 = false
			private var noIosArm64 = false
			private var noIosX64 = false
			private var noMacosX64 = false
			private var noTvosArm64 = false
			private var noTvosX64 = false
			private var noWatchosArm32 = false
			private var noWatchosArm64 = false
			private var noWatchosX64 = false
			private var noWatchosX86 = false


			fun build() = LibraryModuleConfiguration.Target.Darwin(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
				noIosArm32 = noIosArm32,
				noIosArm64 = noIosArm64,
				noIosX64 = noIosX64,
				noMacosX64 = noMacosX64,
				testDependencies = testDependencies,
				noTvosArm64 = noTvosArm64,
				noTvosX64 = noTvosX64,
				noWatchosArm32 = noWatchosArm32,
				noWatchosArm64 = noWatchosArm64,
				noWatchosX64 = noWatchosX64,
				noWatchosX86 = noWatchosX86
			)


			override fun withoutIosArm32() {
				noIosArm32 = true
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


			override fun withoutTvosArm64() {
				noTvosArm64 = true
			}


			override fun withoutTvosX64() {
				noTvosX64 = true
			}


			override fun withoutWatchosArm32() {
				noWatchosArm32 = true
			}


			override fun withoutWatchosArm64() {
				noWatchosArm64 = true
			}


			override fun withoutWatchosX64() {
				noWatchosX64 = true
			}


			override fun withoutWatchosX86() {
				noWatchosX86 = true
			}
		}
	}


	class TargetsBuilder : TargetsDsl {

		private var commonConfiguration: LibraryModuleConfiguration.Target.Common? = null
		private var darwinConfiguration: LibraryModuleConfiguration.Target.Darwin? = null
		private var jsConfiguration: LibraryModuleConfiguration.Target.Js? = null
		private var jvmConfiguration: LibraryModuleConfiguration.Target.Jvm? = null


		fun build() = LibraryModuleConfiguration.Targets(
			common = commonConfiguration ?: LibraryModuleConfiguration.Target.Common.default,
			darwin = darwinConfiguration,
			js = jsConfiguration,
			jvm = jvmConfiguration
		)


		override fun common(configure: CommonTargetDsl.() -> Unit) {
			TargetBuilder.CommonBuilder().apply(configure).build().also { configuration ->
				commonConfiguration = commonConfiguration?.mergeWith(configuration) ?: configuration
			}
		}


		override fun darwin(configure: DarwinTargetDsl.() -> Unit) {
			TargetBuilder.DarwinBuilder().apply(configure).build().also { configuration ->
				darwinConfiguration = darwinConfiguration?.mergeWith(configuration) ?: configuration
			}
		}


		override fun js(compiler: KotlinJsCompilerType?, configure: JsTargetDsl.() -> Unit) {
			TargetBuilder.JsBuilder(compiler = compiler).apply(configure).build().also { configuration ->
				jsConfiguration = jsConfiguration?.mergeWith(configuration) ?: configuration
			}
		}


		override fun jvm(configure: JvmTargetDsl.() -> Unit) {
			TargetBuilder.JvmBuilder().apply(configure).build().also { configuration ->
				jvmConfiguration = jvmConfiguration?.mergeWith(configuration) ?: configuration
			}
		}
	}
}
