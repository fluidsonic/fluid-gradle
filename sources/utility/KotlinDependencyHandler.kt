package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.plugin.*


@Suppress("unused")
fun KotlinDependencyHandler.fluid(name: String, version: String) =
	"io.fluidsonic.${name.substringBefore('-')}:fluid-$name:$version"


@Suppress("unused")
fun KotlinDependencyHandler.kotlinx(name: String, version: String) =
	"org.jetbrains.kotlinx:kotlinx-$name:$version"
