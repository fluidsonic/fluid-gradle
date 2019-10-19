package io.fluidsonic.gradle

import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*


val SourceSet.kotlin
	get() = withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) { kotlin }
