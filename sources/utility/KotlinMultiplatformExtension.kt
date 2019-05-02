package com.github.fluidsonic.fluid.library

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


internal fun KotlinMultiplatformExtension.sourceSets(configure: Action<NamedDomainObjectContainer<KotlinSourceSet>>) =
	configure.execute(sourceSets)
