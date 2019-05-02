package com.github.fluidsonic.fluid.library

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension


internal val Project.fluidLibrary
	get() = rootProject.the<FluidLibraryPluginExtension>()


fun Project.fluidJvmLibrary(configure: FluidJvmLibraryConfiguration.() -> Unit = {}) {
	FluidJvmLibraryConfiguration.applyTo(project = this, configure = configure)
}


fun Project.fluidJvmLibraryVariant(configure: FluidJvmLibraryVariantConfiguration.() -> Unit = {}) {
	FluidJvmLibraryVariantConfiguration.applyTo(project = this, configure = configure)
}


fun Project.fluidLibrary(configure: FluidLibraryConfiguration.() -> Unit = {}) {
	FluidLibraryConfiguration.applyTo(project = this, configure = configure)
}


fun Project.fluidLibraryVariant(configure: FluidLibraryVariantConfiguration.() -> Unit = {}) {
	FluidLibraryVariantConfiguration.applyTo(project = this, configure = configure)
}


internal fun Project.java(configuration: JavaPluginConvention.() -> Unit) =
	configure(configuration)


internal val Project.kotlin: KotlinMultiplatformExtension
	get() = extensions.findByType(KotlinMultiplatformExtension::class)!!


internal fun Project.kotlin(configure: Action<KotlinMultiplatformExtension>) =
	extensions.configure(KotlinMultiplatformExtension::class, configure)


internal fun Project.publishing(configuration: PublishingExtension.() -> Unit) =
	configure(configuration)


internal val Project.signing
	get() = the<SigningExtension>()


internal fun Project.signing(configuration: SigningExtension.() -> Unit) =
	configure(configuration)


internal val Project.sourceSets
	get() = the<SourceSetContainer>()
