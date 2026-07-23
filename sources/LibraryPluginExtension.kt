package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*


/** Gradle extension registered on the root project to carry the resolved [LibraryConfiguration] to each module. */
internal data class LibraryPluginExtension(
	val configuration: LibraryConfiguration
)


/**
 * The [LibraryPluginExtension] registered on the root project, regardless of which project this is called on.
 *
 * @throws org.gradle.api.UnknownDomainObjectException if `fluidLibrary(…) {}` has not been applied on the root project.
 */
internal val Project.fluidLibrary
	get() = rootProject.the<LibraryPluginExtension>()
