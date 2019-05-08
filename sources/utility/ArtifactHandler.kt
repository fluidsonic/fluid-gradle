package com.github.fluidsonic.fluid.library

import org.gradle.api.artifacts.dsl.*


internal fun ArtifactHandler.archives(artifactNotation: Any) =
	add("archives", artifactNotation)
