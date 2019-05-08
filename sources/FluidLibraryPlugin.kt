package com.github.fluidsonic.fluid.library

import org.gradle.api.*


@Suppress("unused")
class FluidLibraryPlugin : Plugin<Project> {

	override fun apply(project: Project): Unit = project.run {
		// TODO do we still need this?
		// apply<KotlinPlatformJvmPlugin>()
		// apply<JavaLibraryPlugin>()
	}
}
