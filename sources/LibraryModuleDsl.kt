package io.fluidsonic.gradle

import org.gradle.api.*
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
	public interface CommonTargetDsl : TargetDsl<KotlinOnlyTarget<AbstractKotlinCompilation<*>>>


	@Dsl
	public interface DependencyContainer {

		@Dsl
		public fun dependencies(configure: KotlinDependencyHandler.() -> Unit)

		@Dsl
		public fun testDependencies(configure: KotlinDependencyHandler.() -> Unit)
	}


	@Dsl
	public interface JsTargetDsl : TargetDsl<KotlinJsTargetDsl> {

		@Dsl
		public fun withoutBrowser()

		@Dsl
		public fun withoutNodeJs()
	}


	@Dsl
	public interface JvmTargetDsl : TargetDsl<KotlinJvmTarget> {

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
	public interface NativeDarwinTargetDsl : TargetDsl<KotlinNativeTarget> {

		@Dsl
		public fun withoutIosArm64()

		@Dsl
		public fun withoutIosX64()

		@Dsl
		public fun withoutMacosX64()
	}


	@Dsl
	public interface TargetDsl<Custom> : DependencyContainer {

		@Dsl
		public fun custom(configure: Custom.() -> Unit)
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
