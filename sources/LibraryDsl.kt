package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*


@Dsl
public interface LibraryDsl {

	@Dsl
	public fun allModules(configure: LibraryModuleDsl.() -> Unit)

	@Dsl
	public fun dependencyUpdatesIncludeUnstableVersions()

	@Dsl
	public fun gradleVersion(version: String)
}


@Dsl
public fun Project.fluidLibrary(
	name: String,
	version: String,
	prefixName: Boolean = true,
	configure: LibraryDsl.() -> Unit = {}
) {
	require(name.isNotEmpty()) { "'name' must not be empty." }
	require(version.isNotEmpty()) { "'version' must not be empty." }

	val fullName = if (prefixName) "fluid-$name" else name

	check(project.parent == null) { "fluidLibrary(…) {} must only be used in the root project" }
	check(project.extensions.findByType<LibraryPluginExtension>() == null) { "fluidLibrary/fluidJvmLibrary(…) {} must only be used once" }
	check(project.name == fullName) { "Project name '${project.name}' must not differ from library name '$fullName' in fluidLibrary(…)" }

	LibraryConfigurator(
		configuration = LibraryConfigurationBuilder(
			fullName = fullName,
			name = name,
			version = version
		)
			.apply {
				allModules {
					language {
						withExperimentalApi("kotlin.ExperimentalUnsignedTypes")
						withExperimentalApi("kotlin.RequiresOptIn")
						withExperimentalApi("kotlin.contracts.ExperimentalContracts")
						withExperimentalApi("kotlin.experimental.ExperimentalTypeInference")

						withLanguageFeature("InlineClasses")
					}
				}
			}
			.apply(configure)
			.build(),
		project = project
	).configure()
}
