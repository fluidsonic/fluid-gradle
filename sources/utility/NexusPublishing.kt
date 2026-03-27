package io.fluidsonic.gradle

import io.github.gradlenexus.publishplugin.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.accessors.runtime.*


/** The [NexusPublishExtension] registered on this project. */
internal val Project.nexusPublishing: NexusPublishExtension
	get() = extensionOf(this, "nexusPublishing") as NexusPublishExtension


internal fun Project.nexusPublishing(action: NexusPublishExtension.() -> Unit) {
	extensions.configure("nexusPublishing", action)
}
