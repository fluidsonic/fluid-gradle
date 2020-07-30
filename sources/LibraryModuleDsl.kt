package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.*
import org.jetbrains.kotlin.gradle.targets.jvm.*


@Dsl
public interface LibraryModuleDsl {

	@Dsl
	public fun custom(configure: KotlinMultiplatformExtension.() -> Unit)

	@Dsl
	public fun language(configure: LanguageDsl.() -> Unit)

	@Dsl
	public fun publishSingleTargetAsModule()

	@Dsl
	public fun targets(configure: TargetsDsl.() -> Unit)

	@Dsl
	public fun withoutPublishing()


	@Dsl
	public interface CommonTargetDsl : TargetDsl<DependenciesDsl, KotlinOnlyTarget<AbstractKotlinCompilation<*>>>


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
	public interface JsTargetDsl : TargetDsl<DependenciesDsl, KotlinJsTargetDsl> {

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
		public fun withoutExplicitApi()

		@Dsl
		public fun withoutNewInference()
	}


	@Dsl
	public interface NativeDarwinTargetDsl : TargetDsl<DependenciesDsl, KotlinNativeTarget> {

		@Dsl
		public fun withoutIosArm64()

		@Dsl
		public fun withoutIosX64()

		@Dsl
		public fun withoutMacosX64()
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
		public fun js(configure: JsTargetDsl.() -> Unit = {})

		@Dsl
		public fun jvm(configure: JvmTargetDsl.() -> Unit = {})

		@Dsl
		public fun jvmJdk7(configure: JvmTargetDsl.() -> Unit = {})

		@Dsl
		public fun nativeDarwin(configure: NativeDarwinTargetDsl.() -> Unit = {})
	}
}


@Dsl
public fun Project.fluidLibraryModule(
	description: String,
	configure: LibraryModuleDsl.() -> Unit
) {
	LibraryModuleConfigurator(
		configuration = LibraryModuleConfigurationBuilder(description = description).apply(configure).build(),
		project = project
	).configure()
}
