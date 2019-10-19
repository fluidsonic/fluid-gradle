package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.kotlin.dsl.*


internal data class FluidsonicPluginExtension(
	val name: String,
	val version: String
)


internal val FluidsonicPluginExtension.fullName
	get() = "fluid-$name"


internal val Project.fluidLibrary
	get() = rootProject.the<FluidsonicPluginExtension>()
