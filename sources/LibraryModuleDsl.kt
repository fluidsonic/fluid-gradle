package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.jvm.*
import java.io.*


@Dsl
public interface LibraryModuleDsl {

	@Dsl
	public fun custom(configure: KotlinMultiplatformExtension.() -> Unit)

	@Dsl
	public fun language(configure: LanguageDsl.() -> Unit)

	@Dsl
	public fun noDokka()

	@Dsl
	public fun targets(configure: TargetsDsl.() -> Unit)

	@Dsl
	public fun withoutPublishing()


	@Dsl
	public interface CommonTargetDsl : TargetDsl<DependenciesDsl, KotlinOnlyTarget<KotlinMetadataCompilation<*>>>


	@Dsl
	public interface DependenciesDsl {

		@Dsl
		public fun api(notation: Any)

		@Dsl
		public fun api(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)

		@Dsl
		public fun compileOnly(notation: Any)

		@Dsl
		public fun compileOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)

		@Dsl
		public fun custom(configure: KotlinDependencyHandler.() -> Unit)

		@Dsl
		public fun implementation(notation: Any)

		@Dsl
		public fun implementation(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)

		@Dsl
		public fun runtimeOnly(notation: Any)

		@Dsl
		public fun runtimeOnly(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)


		@Dsl
		public fun fluid(simpleModuleName: String, version: String, usePrefix: Boolean = true): Any

		@Dsl
		public fun kotlin(simpleModuleName: String, version: String? = null): Any

		@Dsl
		public fun kotlinx(simpleModuleName: String, version: String, usePrefix: Boolean = true): Any

		@Dsl
		public fun project(path: String, configuration: String? = null): Any =
			project(mapOf("configuration" to configuration, "path" to path))

		@Dsl
		public fun project(notation: Map<String, Any?>): Any
	}


	@Dsl
	public interface JsDependenciesDsl : DependenciesDsl {

		@Dsl
		public fun devNpm(name: String, version: String): Any

		@Dsl
		public fun devNpm(name: String, directory: File): Any

		@Dsl
		public fun devNpm(directory: File): Any

		@Dsl
		public fun npm(name: String, version: String, generateExternals: Boolean = false): Any

		@Dsl
		public fun npm(name: String, directory: File, generateExternals: Boolean = false): Any

		@Dsl
		public fun npm(directory: File, generateExternals: Boolean = false): Any

		@Dsl
		public fun optionalNpm(name: String, version: String, generateExternals: Boolean = false): Any

		@Dsl
		public fun optionalNpm(name: String, directory: File, generateExternals: Boolean = false): Any

		@Dsl
		public fun optionalNpm(directory: File, generateExternals: Boolean = false): Any

		@Dsl
		public fun peerNpm(name: String, version: String): Any
	}


	@Dsl
	public interface JsTargetDsl : TargetDsl<JsDependenciesDsl, KotlinJsTargetDsl> {

		@Dsl
		public fun withoutBrowser()

		@Dsl
		public fun withoutNodeJs()
	}


	@Dsl
	public interface JvmDependenciesDsl : DependenciesDsl {

		@Dsl
		public fun kapt(notation: Any)

		@Dsl
		public fun kapt(dependencyNotation: String, configure: ExternalModuleDependency.() -> Unit)
	}


	@Dsl
	public interface JvmTargetDsl : TargetDsl<JvmDependenciesDsl, KotlinJvmTarget> {

		@Dsl
		public fun withJava()
	}


	@Dsl
	public interface LanguageDsl {

		@Dsl
		public fun custom(configure: LanguageSettingsBuilder.() -> Unit)

		@Dsl
		public fun withExperimentalApi(name: String)

		@Dsl
		public fun withLanguageFeature(name: String)

		@Dsl
		public fun version(version: String)

		@Dsl
		public fun withoutExplicitApi()
	}


	@Dsl
	public interface DarwinTargetDsl : TargetDsl<DependenciesDsl, KotlinNativeTarget> {

		@Dsl
		public fun withoutIosArm32()

		@Dsl
		public fun withoutIosArm64()

		@Dsl
		public fun withoutIosSimulatorArm64()

		@Dsl
		public fun withoutIosX64()

		@Dsl
		public fun withoutMacosArm64()

		@Dsl
		public fun withoutMacosX64()

		@Dsl
		public fun withoutTvosArm64()

		@Dsl
		public fun withoutTvosSimulatorArm64()

		@Dsl
		public fun withoutTvosX64()

		@Dsl
		public fun withoutWatchosArm32()

		@Dsl
		public fun withoutWatchosArm64()

		@Dsl
		public fun withoutWatchosSimulatorArm64()

		@Dsl
		public fun withoutWatchosX64()

		@Dsl
		public fun withoutWatchosX86()
	}


	@Dsl
	public interface TargetDsl<out Dependencies : DependenciesDsl, out Custom> {

		@Dsl
		public fun custom(configure: Custom.() -> Unit)

		@Dsl
		public fun dependencies(configure: Dependencies.() -> Unit)

		@Dsl
		public fun testDependencies(configure: Dependencies.() -> Unit)

		@Dsl
		public fun withoutEnforcingSameVersionForAllKotlinDependencies()
	}


	@Dsl
	public interface TargetsDsl {

		@Dsl
		public fun common(configure: CommonTargetDsl.() -> Unit = {})

		@Dsl
		public fun darwin(configure: DarwinTargetDsl.() -> Unit = {})

		@Dsl
		public fun js(compiler: KotlinJsCompilerType? = null, configure: JsTargetDsl.() -> Unit = {})

		@Dsl
		public fun jvm(configure: JvmTargetDsl.() -> Unit = {})
	}
}


@Dsl
public fun Project.fluidLibraryModule(
	description: String,
	configure: LibraryModuleDsl.() -> Unit,
) {
	LibraryModuleConfigurator(
		configuration = LibraryModuleConfigurationBuilder(description = description).apply(configure).build(),
		project = project
	).configure()
}
