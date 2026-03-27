package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*


/** DSL for configuring root-level library settings. */
@Dsl
public interface LibraryDsl {

	/** Applies [configure] to every library module. */
	public fun allModules(configure: LibraryModuleDsl.() -> Unit)

	/** Includes unstable versions when checking for dependency updates. */
	public fun dependencyUpdatesIncludeUnstableVersions()

	/** Sets the Gradle wrapper version for this project. */
	public fun gradleVersion(version: String)
}


/** Entry point for configuring a fluidsonic library project. Must be called in the root project's `build.gradle.kts`. */
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
						withExperimentalApi("kotlin.contracts.ExperimentalContracts")
						withExperimentalApi("kotlin.experimental.ExperimentalTypeInference")
					}
				}
			}
			.apply(configure)
			.build(),
		project = project
	).configure()
}
