package com.github.fluidsonic.fluid.library

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin


@Suppress("unused")
class FluidLibraryPlugin : Plugin<Project> {

	override fun apply(project: Project): Unit = project.run {
		apply<KotlinPlatformJvmPlugin>()
		apply<JavaLibraryPlugin>()
	}
}
