package io.fluidsonic.gradle

import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*


public val SourceSet.kotlin: SourceDirectorySet
	get() =  withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) { kotlin }
