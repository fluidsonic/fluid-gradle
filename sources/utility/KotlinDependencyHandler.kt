package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.plugin.*


@Suppress("unused")
public fun KotlinDependencyHandler.fluid(name: String, version: String, prefixName: Boolean = true): String =
	if (prefixName) "io.fluidsonic.${name.substringBefore('-')}:fluid-$name:$version"
	else "io.fluidsonic.${name.substringBefore('-')}:$name:$version"


@Suppress("unused")
public fun KotlinDependencyHandler.kotlinx(name: String, version: String, prefixName: Boolean = true): String =
	if (prefixName) "org.jetbrains.kotlinx:kotlinx-$name:$version"
	else "org.jetbrains.kotlinx:$name:$version"
