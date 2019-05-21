package com.github.fluidsonic.fluid.library

import org.gradle.api.artifacts.dsl.*


fun DependencyHandler.api(dependencyNotation: Any) =
	add("api", dependencyNotation)


fun DependencyHandler.testApi(dependencyNotation: Any) =
	add("testApi", dependencyNotation)


fun DependencyHandler.fluid(name: String, version: String) =
	"com.github.fluidsonic:fluid-$name:$version"


fun DependencyHandler.implementation(dependencyNotation: Any) =
	add("implementation", dependencyNotation)


fun DependencyHandler.kotlinx(name: String, version: String) =
	"org.jetbrains.kotlinx:kotlinx-$name:$version"


fun DependencyHandler.runtimeOnly(dependencyNotation: Any) =
	add("runtimeOnly", dependencyNotation)


fun DependencyHandler.testImplementation(dependencyNotation: Any) =
	add("testImplementation", dependencyNotation)


fun DependencyHandler.testRuntimeOnly(dependencyNotation: Any) =
	add("testRuntimeOnly", dependencyNotation)
