package com.github.fluidsonic.fluid.library

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension


internal val Project.fluidLibrary
	get() = rootProject.the<FluidLibraryPluginExtension>()


fun Project.fluidLibrary(configure: FluidLibraryConfiguration.() -> Unit = {}) {
	FluidLibraryConfiguration.apply(project = this, configure = configure)
}


fun Project.fluidLibraryVariant(configure: FluidLibraryVariantConfiguration.() -> Unit = {}) {
	FluidLibraryVariantConfiguration.apply(project = this, configure = configure)
}


internal fun Project.java(configuration: JavaPluginConvention.() -> Unit) =
	configure(configuration)


internal fun Project.publishing(configuration: PublishingExtension.() -> Unit) =
	configure(configuration)


internal val Project.signing
	get() = the<SigningExtension>()


internal fun Project.signing(configuration: SigningExtension.() -> Unit) =
	configure(configuration)


internal val Project.sourceSets
	get() = the<SourceSetContainer>()
