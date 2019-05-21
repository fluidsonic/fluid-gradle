package com.github.fluidsonic.fluid.library

import org.jetbrains.kotlin.gradle.plugin.*


fun KotlinDependencyHandler.fluid(name: String, version: String) =
	"com.github.fluidsonic:fluid-$name:$version"


fun KotlinDependencyHandler.kotlinx(name: String, version: String) =
	"org.jetbrains.kotlinx:kotlinx-$name:$version"
