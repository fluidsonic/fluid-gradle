package io.fluidsonic.gradle

import org.gradle.api.*


@Suppress("unused")
class FluidsonicPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		// https://github.com/gradle/gradle/issues/11412#issuecomment-555413327
		System.setProperty("org.gradle.internal.publish.checksums.insecure", "true")
	}
}
