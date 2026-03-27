package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.updates.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.kotlin.gradle.dsl.*


/** Configures all [DependencyUpdatesTask] instances in this project. */
public fun Project.dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit): DomainObjectCollection<DependencyUpdatesTask> =
	tasks.withType(configuration)


/** The [KotlinMultiplatformExtension] registered on this project. */
public val Project.kotlin: KotlinMultiplatformExtension
	get() = extensions.findByType(KotlinMultiplatformExtension::class)!!


/** Configures the [KotlinMultiplatformExtension] on this project. */
public fun Project.kotlin(configure: Action<KotlinMultiplatformExtension>) {
	extensions.configure(KotlinMultiplatformExtension::class, configure)
}


internal val Project.publishing: PublishingExtension
	get() = (this as ExtensionAware).extensions.getByName("publishing") as PublishingExtension


internal fun Project.publishing(configuration: PublishingExtension.() -> Unit) =
	configure(configuration)


internal val Project.signing: SigningExtension
	get() = the()


internal fun Project.signing(configuration: SigningExtension.() -> Unit) {
	configure(configuration)
}


/** Configures all [Wrapper] tasks in this project. */
public fun Project.wrapper(configuration: Wrapper.() -> Unit): DomainObjectCollection<Wrapper> =
	tasks.withType(configuration)


/** Returns `true` if the given [version] string looks like a pre-release (alpha, beta, RC, etc.). */
public fun isUnstableVersion(version: String): Boolean =
	Regex("\\b(alpha|beta|dev|eap|m|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE).containsMatchIn(version)
