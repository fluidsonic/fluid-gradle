package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.*
import org.gradle.api.artifacts.repositories.*
import org.gradle.kotlin.dsl.*


/** Adds the JitPack Maven repository. */
public fun RepositoryHandler.jitpack(): MavenArtifactRepository =
	maven("https://jitpack.io")
