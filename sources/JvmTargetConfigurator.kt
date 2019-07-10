package com.github.fluidsonic.fluid.library

import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.targets.jvm.*


class JvmTargetConfigurator private constructor(
	private val target: KotlinJvmTarget
) {

	fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
		target.compilations["main"].defaultSourceSet.dependencies(configure)
	}


	fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) {
		target.compilations["test"].defaultSourceSet.dependencies(configure)
	}


	companion object {

		internal fun applyTo(target: KotlinJvmTarget, configure: JvmTargetConfigurator.() -> Unit = {}) {
			JvmTargetConfigurator(target = target).apply(configure)
		}
	}
}
