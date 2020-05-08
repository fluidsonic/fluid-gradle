package io.fluidsonic.gradle

import com.github.benmanes.gradle.versions.updates.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.wrapper.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.kotlin.gradle.dsl.*


fun Project.dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit) =
	tasks.withType(configuration)


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


fun Project.test(configuration: Test.() -> Unit) =
	tasks.withType(configuration)


fun Project.wrapper(configuration: Wrapper.() -> Unit) =
	tasks.withType(configuration)


fun isUnstableVersion(version: String) =
	Regex("\\b(alpha|beta|eap|m|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE).containsMatchIn(version)
