package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.plugin.*


@Suppress("unused")
fun KotlinDependencyHandler.fluid(name: String, version: String, prefixName: Boolean = true) =
	if (prefixName) "io.fluidsonic.${name.substringBefore('-')}:fluid-$name:$version"
	else "io.fluidsonic.${name.substringBefore('-')}:$name:$version"


@Suppress("unused")
fun KotlinDependencyHandler.kotlinx(name: String, version: String, prefixName: Boolean = true) =
	if (prefixName) "org.jetbrains.kotlinx:kotlinx-$name:$version"
	else "org.jetbrains.kotlinx:$name:$version"
