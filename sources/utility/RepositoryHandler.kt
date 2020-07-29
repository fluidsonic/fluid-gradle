package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.*
import org.gradle.api.artifacts.repositories.*
import org.gradle.kotlin.dsl.*


public fun RepositoryHandler.bintray(path: String): MavenArtifactRepository =
	maven("https://dl.bintray.com/$path")


public fun RepositoryHandler.jitpack(): MavenArtifactRepository =
	maven("https://jitpack.io")
