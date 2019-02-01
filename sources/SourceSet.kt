package com.github.fluidsonic.fluid.library

import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.*


internal val SourceSet.kotlin
	get() = withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) { kotlin }
