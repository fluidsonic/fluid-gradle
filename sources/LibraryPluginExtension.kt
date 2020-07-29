package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*


internal data class LibraryPluginExtension(
	val configuration: LibraryConfiguration
)


internal val Project.fluidLibrary
	get() = rootProject.the<LibraryPluginExtension>()
