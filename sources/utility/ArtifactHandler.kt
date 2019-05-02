package com.github.fluidsonic.fluid.library

import org.gradle.api.artifacts.dsl.ArtifactHandler


internal fun ArtifactHandler.archives(artifactNotation: Any) =
	add("archives", artifactNotation)
