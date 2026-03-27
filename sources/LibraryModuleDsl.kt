package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.jvm.*
import java.io.*


/** DSL for configuring a library module. */
@Dsl
public interface LibraryModuleDsl {

	/** Provides direct access to the [KotlinMultiplatformExtension] for custom configuration. */
	public fun custom(configure: KotlinMultiplatformExtension.() -> Unit)

	/** Configures Kotlin language settings for this module. */
	public fun language(configure: LanguageDsl.() -> Unit)

	/** Disables Dokka documentation generation for this module. */
	public fun noDokka()

	/** Configures the compilation targets for this module. */
	public fun targets(configure: TargetsDsl.() -> Unit)

	/** Excludes this module from Maven publication. */
	public fun withoutPublishing()


	/** DSL for configuring the common (metadata) target. */
	@Dsl
	public interface CommonTargetDsl : TargetDsl<DependenciesDsl, KotlinOnlyTarget<KotlinMetadataCompilation<Any>>>


	/** DSL for declaring dependencies on a source set. */
	@Dsl
	public interface DependenciesDsl {

		/** Adds an API dependency visible to consumers. */
		public fun api(notation: Any)

		/** Adds an API dependency visible to consumers with additional [configure] block. */
		public fun api(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)

		/** Adds a compile-only dependency not included at runtime. */
		public fun compileOnly(notation: Any)

		/** Adds a compile-only dependency not included at runtime with additional [configure] block. */
		public fun compileOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)

		/** Provides direct access to the [KotlinDependencyHandler] for custom configuration. */
		public fun custom(configure: KotlinDependencyHandler.() -> Unit)

		/** Adds an implementation dependency not visible to consumers. */
		public fun implementation(notation: Any)

		/** Adds an implementation dependency not visible to consumers with additional [configure] block. */
		public fun implementation(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)

		/** Adds a runtime-only dependency not available at compile time. */
		public fun runtimeOnly(notation: Any)

		/** Adds a runtime-only dependency not available at compile time with additional [configure] block. */
		public fun runtimeOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)


		/** Creates a dependency notation for a `io.fluidsonic.*` module. */
		public fun fluid(simpleModuleName: String, version: String, usePrefix: Boolean = true): Any

		/** Creates a dependency notation for a `kotlin-stdlib-*` module. */
		public fun kotlin(simpleModuleName: String, version: String? = null): Any

		/** Creates a dependency notation for a `kotlinx-*` module. */
		public fun kotlinx(simpleModuleName: String, version: String, usePrefix: Boolean = true): Any

		/** Creates a dependency notation for a Gradle project by [path]. */
		public fun project(path: String, configuration: String? = null): Any =
			project(mapOf("configuration" to configuration, "path" to path))

		/** Creates a dependency notation for a Gradle project by [notation] map. */
		public fun project(notation: Map<String, Any?>): Any
	}


	/** DSL for declaring JS-specific dependencies, including npm packages. */
	@Deprecated("JS target support will be removed in a future version.")
	@Dsl
	public interface JsDependenciesDsl : DependenciesDsl {

		public fun devNpm(name: String, version: String): Any

		public fun devNpm(name: String, directory: File): Any

		public fun devNpm(directory: File): Any

		public fun npm(name: String, version: String, generateExternals: Boolean = false): Any

		public fun npm(name: String, directory: File, generateExternals: Boolean = false): Any

		public fun npm(directory: File, generateExternals: Boolean = false): Any

		public fun optionalNpm(name: String, version: String, generateExternals: Boolean = false): Any

		public fun optionalNpm(name: String, directory: File, generateExternals: Boolean = false): Any

		public fun optionalNpm(directory: File, generateExternals: Boolean = false): Any

		public fun peerNpm(name: String, version: String): Any
	}


	/** DSL for configuring the Kotlin/JS target. */
	@Deprecated("JS target support will be removed in a future version.")
	@Suppress("DEPRECATION")
	@Dsl
	public interface JsTargetDsl : TargetDsl<JsDependenciesDsl, KotlinJsTargetDsl> {

		/** Disables browser sub-target. */
		public fun withoutBrowser()

		/** Disables Node.js sub-target. */
		public fun withoutNodeJs()
	}


	/** DSL for declaring JVM-specific dependencies, including annotation processors. */
	@Dsl
	public interface JvmDependenciesDsl : DependenciesDsl {

		/** Adds a kapt annotation processor dependency. */
		@Deprecated("Use ksp() instead.", ReplaceWith("ksp(notation)"))
		public fun kapt(notation: Any)

		/** Adds a kapt annotation processor dependency with additional configuration. */
		@Deprecated("Use ksp() instead.", ReplaceWith("ksp(dependencyNotation, configure)"))
		public fun kapt(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)

		/** Adds a KSP (Kotlin Symbol Processing) annotation processor dependency. */
		public fun ksp(notation: Any)

		/** Adds a KSP (Kotlin Symbol Processing) annotation processor dependency with additional configuration. */
		public fun ksp(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)
	}


	/** DSL for configuring the Kotlin/JVM target. */
	@Dsl
	public interface JvmTargetDsl : TargetDsl<JvmDependenciesDsl, KotlinJvmTarget>


	/** DSL for configuring Kotlin language settings. */
	@Dsl
	public interface LanguageDsl {

		/** Provides direct access to [LanguageSettingsBuilder] for custom configuration. */
		public fun custom(configure: LanguageSettingsBuilder.() -> Unit)

		/** Opts in to an experimental API identified by [name]. */
		public fun withExperimentalApi(name: String)

		/** Enables a Kotlin language feature identified by [name]. */
		public fun withLanguageFeature(name: String)

		/** Sets the Kotlin language version. */
		public fun version(version: String)

		/** Disables explicit API mode for this module. */
		public fun withoutExplicitApi()
	}


	/** Base DSL for configuring a compilation target. */
	@Dsl
	public interface TargetDsl<out Dependencies : DependenciesDsl, out Custom> {

		/** Provides direct access to the underlying Kotlin target for custom configuration. */
		public fun custom(configure: Custom.() -> Unit)

		/** Configures main source set dependencies. */
		public fun dependencies(configure: Dependencies.() -> Unit)

		/** Configures test source set dependencies. */
		public fun testDependencies(configure: Dependencies.() -> Unit)

		/** Disables the default BOM that enforces the same version for all Kotlin dependencies. */
		public fun withoutEnforcingSameVersionForAllKotlinDependencies()
	}


	/** DSL for declaring which compilation targets to enable. */
	@Dsl
	public interface TargetsDsl {

		/** Configures the common (metadata) target. */
		public fun common(configure: CommonTargetDsl.() -> Unit = {})

		/** Enables and configures the Kotlin/JS target. */
		@Suppress("DEPRECATION")
		public fun js(configure: JsTargetDsl.() -> Unit = {})

		/** Enables and configures the Kotlin/JVM target. */
		public fun jvm(configure: JvmTargetDsl.() -> Unit = {})
	}
}


/** Entry point for configuring a library module. Must be called in each subproject's `build.gradle.kts`. */
public fun Project.fluidLibraryModule(
	description: String,
	configure: LibraryModuleDsl.() -> Unit,
) {
	LibraryModuleConfigurator(
		configuration = LibraryModuleConfigurationBuilder(description = description).apply(configure).build(),
		project = project
	).configure()
}
