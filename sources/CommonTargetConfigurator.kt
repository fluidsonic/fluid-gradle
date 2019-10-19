package io.fluidsonic.gradle

import org.gradle.api.*
import org.jetbrains.kotlin.gradle.plugin.*


class CommonTargetConfigurator private constructor(
	private val sourceSets: NamedDomainObjectContainer<KotlinSourceSet>
) {

	fun dependencies(configure: KotlinDependencyHandler.() -> Unit) {
		sourceSets.named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
			dependencies(configure)
		}
	}


	fun testDependencies(configure: KotlinDependencyHandler.() -> Unit) {
		sourceSets.named(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME) {
			dependencies(configure)
		}
	}


	companion object {

		internal fun applyTo(sourceSets: NamedDomainObjectContainer<KotlinSourceSet>, configure: CommonTargetConfigurator.() -> Unit = {}) {
			CommonTargetConfigurator(sourceSets = sourceSets).apply(configure)
		}
	}
}
