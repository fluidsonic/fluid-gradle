package io.fluidsonic.gradle

import io.fluidsonic.gradle.LibraryModuleDsl.CommonTargetDsl
import io.fluidsonic.gradle.LibraryModuleDsl.DependenciesDsl
import io.fluidsonic.gradle.LibraryModuleDsl.JsDependenciesDsl
import io.fluidsonic.gradle.LibraryModuleDsl.JsTargetDsl
import io.fluidsonic.gradle.LibraryModuleDsl.JvmDependenciesDsl
import io.fluidsonic.gradle.LibraryModuleDsl.JvmTargetDsl
import io.fluidsonic.gradle.LibraryModuleDsl.LanguageDsl
import io.fluidsonic.gradle.LibraryModuleDsl.TargetDsl
import io.fluidsonic.gradle.LibraryModuleDsl.TargetsDsl
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinOnlyTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File

/** Collects [LibraryModuleDsl] calls and resolves them into a [LibraryModuleConfiguration] via [build]. */
internal class LibraryModuleConfigurationBuilder(private val description: String?) : LibraryModuleDsl {

	private val customConfigurations: MutableList<KotlinMultiplatformExtension.() -> Unit> = mutableListOf()
	private var isDokkaEnabled = true
	private var isPublishingEnabled = true
	private var languageConfiguration: LibraryModuleConfiguration.Language? = null
	private var targetsConfiguration: LibraryModuleConfiguration.Targets? = null

	fun build() = LibraryModuleConfiguration(
		customConfigurations = customConfigurations.toList(),
		description = description,
		isDokkaEnabled = isDokkaEnabled,
		isPublishingEnabled = isPublishingEnabled,
		language = languageConfiguration ?: LibraryModuleConfiguration.Language.default,
		targets = targetsConfiguration ?: LibraryModuleConfiguration.Targets.default,
	)

	override fun custom(configure: KotlinMultiplatformExtension.() -> Unit) {
		customConfigurations += configure
	}

	override fun language(configure: LanguageDsl.() -> Unit) {
		LanguageBuilder().apply(configure).build().also { configuration ->
			languageConfiguration = languageConfiguration?.mergeWith(configuration) ?: configuration
		}
	}

	override fun disableDokka() {
		isDokkaEnabled = false
	}

	override fun targets(configure: TargetsDsl.() -> Unit) {
		TargetsBuilder().apply(configure).build().also { configuration ->
			targetsConfiguration = targetsConfiguration?.mergeWith(configuration, addAutomatically = true) ?: configuration
		}
	}

	override fun disablePublishing() {
		isPublishingEnabled = false
	}

	/** Collects [LanguageDsl] calls into a [LibraryModuleConfiguration.Language]. */
	class LanguageBuilder : LanguageDsl {

		private val customConfigurations: MutableList<LanguageSettingsBuilder.() -> Unit> = mutableListOf()
		private val experimentalApisToUse: MutableSet<String> = mutableSetOf()
		private var isExplicitApiEnabled = true
		private var version: String? = null

		fun build() = LibraryModuleConfiguration.Language(
			customConfigurations = customConfigurations.toList(),
			experimentalApisToUse = experimentalApisToUse.toSet(),
			isExplicitApiEnabled = isExplicitApiEnabled,
			version = version,
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

		override fun disableExplicitApi() {
			isExplicitApiEnabled = false
		}
	}

	/** Base builder for a single compilation target, shared by the common, JS, and JVM target builders. */
	sealed class TargetBuilder<out Dependencies : DependenciesDsl, Custom> : TargetDsl<Dependencies, Custom> {

		protected val customConfigurations: MutableList<Custom.() -> Unit> = mutableListOf()
		protected var enforcesSameVersionForAllKotlinDependencies = true
		protected var dependencies = LibraryModuleConfiguration.Dependencies.default
		protected var testDependencies = LibraryModuleConfiguration.Dependencies.default

		final override fun custom(configure: Custom.() -> Unit) {
			customConfigurations += configure
		}

		final override fun dependencies(configure: Dependencies.() -> Unit) {
			// Safe: the single DependenciesBuilder implements every per-target dependency DSL (JsDependenciesDsl,
			// JvmDependenciesDsl), so widening the caller's typed lambda to DependenciesDsl never reaches a missing
			// member — the per-target members don't collide. See docs/scout/dsl-internals.md.
			@Suppress("UNCHECKED_CAST")
			val dependencies = DependenciesBuilder().apply(configure as DependenciesDsl.() -> Unit).build()

			this.dependencies = this.dependencies.mergeWith(dependencies)
		}

		final override fun testDependencies(configure: Dependencies.() -> Unit) {
			// Safe: the single DependenciesBuilder implements every per-target dependency DSL (JsDependenciesDsl,
			// JvmDependenciesDsl), so widening the caller's typed lambda to DependenciesDsl never reaches a missing
			// member — the per-target members don't collide. See docs/scout/dsl-internals.md.
			@Suppress("UNCHECKED_CAST")
			val dependencies = DependenciesBuilder().apply(configure as DependenciesDsl.() -> Unit).build()

			this.testDependencies = this.testDependencies.mergeWith(dependencies)
		}

		final override fun disableKotlinDependencyVersionAlignment() {
			enforcesSameVersionForAllKotlinDependencies = false
		}

		/** Builds the common (metadata) target configuration. */
		class CommonBuilder :
			TargetBuilder<DependenciesDsl, KotlinOnlyTarget<KotlinMetadataCompilation<Any>>>(),
			CommonTargetDsl {

			fun build() = LibraryModuleConfiguration.Target.Common(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
				testDependencies = testDependencies,
			)
		}

		/**
		 * Collects dependency declarations for one source set into a [LibraryModuleConfiguration.Dependencies].
		 *
		 * Implements every per-target dependency DSL ([JsDependenciesDsl], [JvmDependenciesDsl]) so a single builder
		 * serves all targets; see the `dependencies`/`testDependencies` overrides and `docs/scout/dsl-internals.md`.
		 */
		// TooManyFunctions: implements every per-target dependency DSL (see KDoc above), so the function count is intentional.
		@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION", "TooManyFunctions")
		class DependenciesBuilder :
			JsDependenciesDsl,
			JvmDependenciesDsl {

			private val configurations: MutableList<KotlinDependencyHandler.() -> Unit> = mutableListOf()
			private val kaptConfigurations: MutableList<DependencyHandler.() -> Unit> = mutableListOf()
			private val kspConfigurations: MutableList<DependencyHandler.() -> Unit> = mutableListOf()

			fun build() = LibraryModuleConfiguration.Dependencies(
				configurations = configurations.toList(),
				kaptConfigurations = kaptConfigurations.toList(),
				kspConfigurations = kspConfigurations.toList(),
			)

			override fun devNpm(name: String, version: String): Any = SpecialDependency.DevNpmNameVersion(name = name, version = version)

			override fun devNpm(name: String, directory: File): Any = SpecialDependency.DevNpmNameDirectory(name = name, directory = directory)

			override fun devNpm(directory: File): Any = SpecialDependency.DevNpmDirectory(directory = directory)

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

			override fun peerNpm(name: String, version: String): Any = SpecialDependency.PeerNpmNameVersion(name = name, version = version)

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

			override fun ksp(notation: Any) {
				kspConfigurations += { add("kspJvm", resolve(notation)) }
			}

			override fun ksp(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit) {
				kspConfigurations += { add("kspJvm", dependencyNotation, configure) }
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
				is SpecialDependency -> error("Unsupported: $notation")
				else -> notation
			}

			override fun fluid(simpleModuleName: String, version: String, usePrefix: Boolean): Any = if (usePrefix) {
				"io.fluidsonic.${simpleModuleName.substringBefore('-')}:fluid-$simpleModuleName:$version"
			} else {
				"io.fluidsonic.${simpleModuleName.substringBefore('-')}:$simpleModuleName:$version"
			}

			override fun kotlin(simpleModuleName: String, version: String?): Any = SpecialDependency.Kotlin(simpleModuleName = simpleModuleName, version = version)

			override fun kotlinx(simpleModuleName: String, version: String, usePrefix: Boolean): Any = if (usePrefix) {
				"org.jetbrains.kotlinx:kotlinx-$simpleModuleName:$version"
			} else {
				"org.jetbrains.kotlinx:$simpleModuleName:$version"
			}

			override fun project(notation: Map<String, Any?>): Any = SpecialDependency.Project(notation = notation)

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

		/** Builds the Kotlin/JS target configuration. */
		@Suppress("DEPRECATION")
		class JsBuilder :
			TargetBuilder<JsDependenciesDsl, KotlinJsTargetDsl>(),
			JsTargetDsl {

			private var isBrowserEnabled = true
			private var isNodeJsEnabled = true

			fun build() = LibraryModuleConfiguration.Target.Js(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
				isBrowserEnabled = isBrowserEnabled,
				isNodeJsEnabled = isNodeJsEnabled,
				testDependencies = testDependencies,
			)

			override fun withoutBrowser() {
				isBrowserEnabled = false
			}

			override fun withoutNodeJs() {
				isNodeJsEnabled = false
			}
		}

		/** Builds the Kotlin/JVM target configuration. */
		class JvmBuilder :
			TargetBuilder<JvmDependenciesDsl, KotlinJvmTarget>(),
			JvmTargetDsl {

			private var jdkVersion = JdkVersion.v21

			fun build() = LibraryModuleConfiguration.Target.Jvm(
				customConfigurations = customConfigurations.toList(),
				dependencies = dependencies,
				enforcesSameVersionForAllKotlinDependencies = enforcesSameVersionForAllKotlinDependencies,
				jdkVersion = jdkVersion,
				testDependencies = testDependencies,
			)

			override fun jdk(version: Int) {
				jdkVersion = JdkVersion.entries.firstOrNull { it.code == version }
					?: throw IllegalArgumentException("Unsupported JDK version $version. Supported versions are 21 to 24.")
			}
		}
	}

	/** Collects [TargetsDsl] calls into a [LibraryModuleConfiguration.Targets]. */
	class TargetsBuilder : TargetsDsl {

		private var commonConfiguration: LibraryModuleConfiguration.Target.Common? = null
		private var jsConfiguration: LibraryModuleConfiguration.Target.Js? = null
		private var jvmConfiguration: LibraryModuleConfiguration.Target.Jvm? = null

		fun build() = LibraryModuleConfiguration.Targets(
			common = commonConfiguration ?: LibraryModuleConfiguration.Target.Common.default,
			js = jsConfiguration,
			jvm = jvmConfiguration,
		)

		override fun common(configure: CommonTargetDsl.() -> Unit) {
			TargetBuilder.CommonBuilder().apply(configure).build().also { configuration ->
				commonConfiguration = commonConfiguration?.mergeWith(configuration) ?: configuration
			}
		}

		@Suppress("DEPRECATION")
		override fun js(configure: JsTargetDsl.() -> Unit) {
			TargetBuilder.JsBuilder().apply(configure).build().also { configuration ->
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
