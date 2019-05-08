package com.github.fluidsonic.fluid.library

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.kotlin.gradle.dsl.*


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


val Project.kotlin: KotlinMultiplatformExtension
	get() = extensions.findByType(KotlinMultiplatformExtension::class)!!


fun Project.kotlin(configure: Action<KotlinMultiplatformExtension>) =
	extensions.configure(KotlinMultiplatformExtension::class, configure)


internal val Project.publishing
	get() = (this as ExtensionAware).extensions.getByName("publishing") as PublishingExtension


internal fun Project.publishing(configuration: PublishingExtension.() -> Unit) =
	configure(configuration)


internal val Project.signing
	get() = the<SigningExtension>()


internal fun Project.signing(configuration: SigningExtension.() -> Unit) =
	configure(configuration)


internal val Project.sourceSets
	get() = the<SourceSetContainer>()
