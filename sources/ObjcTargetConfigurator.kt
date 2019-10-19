package io.fluidsonic.gradle

import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*


class ObjcTargetConfigurator private constructor(
	private val target: KotlinNativeTarget
) {

	fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
		target.compilations["main"].defaultSourceSet.dependencies(configure)
	}


	fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) {
		target.compilations["test"].defaultSourceSet.dependencies(configure)
	}


	companion object {

		internal fun applyTo(target: KotlinNativeTarget, configure: ObjcTargetConfigurator.() -> Unit = {}) {
			ObjcTargetConfigurator(target = target).apply(configure)
		}
	}
}
