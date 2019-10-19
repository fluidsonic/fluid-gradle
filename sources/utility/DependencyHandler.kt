package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.*
import org.gradle.kotlin.dsl.accessors.runtime.*


fun DependencyHandler.api(dependencyNotation: Any) =
	add("api", dependencyNotation)


fun DependencyHandler.api(
	dependencyNotation: String,
	dependencyConfiguration: Action<ExternalModuleDependency>
): ExternalModuleDependency = addDependencyTo(
	this, "api", dependencyNotation, dependencyConfiguration
)


@Suppress("unused")
fun DependencyHandler.fluid(name: String, version: String) =
	"io.fluidsonic.${name.substringBefore('-')}:fluid-$name:$version"


fun DependencyHandler.implementation(dependencyNotation: Any) =
	add("implementation", dependencyNotation)


fun DependencyHandler.implementation(
	dependencyNotation: String,
	dependencyConfiguration: Action<ExternalModuleDependency>
): ExternalModuleDependency = addDependencyTo(
	this, "implementation", dependencyNotation, dependencyConfiguration
)


@Suppress("unused")
fun DependencyHandler.kotlinx(name: String, version: String) =
	"org.jetbrains.kotlinx:kotlinx-$name:$version"


fun DependencyHandler.runtimeOnly(dependencyNotation: Any) =
	add("runtimeOnly", dependencyNotation)


fun DependencyHandler.runtimeOnly(
	dependencyNotation: String,
	dependencyConfiguration: Action<ExternalModuleDependency>
): ExternalModuleDependency = addDependencyTo(
	this, "runtimeOnly", dependencyNotation, dependencyConfiguration
)


fun DependencyHandler.testApi(dependencyNotation: Any) =
	add("testApi", dependencyNotation)


fun DependencyHandler.testApi(
	dependencyNotation: String,
	dependencyConfiguration: Action<ExternalModuleDependency>
): ExternalModuleDependency = addDependencyTo(
	this, "testApi", dependencyNotation, dependencyConfiguration
)


fun DependencyHandler.testImplementation(dependencyNotation: Any) =
	add("testImplementation", dependencyNotation)


fun DependencyHandler.testImplementation(
	dependencyNotation: String,
	dependencyConfiguration: Action<ExternalModuleDependency>
): ExternalModuleDependency = addDependencyTo(
	this, "testImplementation", dependencyNotation, dependencyConfiguration
)


fun DependencyHandler.testRuntimeOnly(dependencyNotation: Any) =
	add("testRuntimeOnly", dependencyNotation)


fun DependencyHandler.testRuntimeOnly(
	dependencyNotation: String,
	dependencyConfiguration: Action<ExternalModuleDependency>
): ExternalModuleDependency = addDependencyTo(
	this, "testRuntimeOnly", dependencyNotation, dependencyConfiguration
)
