package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*


internal data class FluidsonicPluginExtension(
	val fullName: String,
	val name: String,
	val version: String
)


internal val Project.fluidLibrary
	get() = rootProject.the<FluidsonicPluginExtension>()
