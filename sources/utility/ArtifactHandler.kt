package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.*


internal fun ArtifactHandler.archives(artifactNotation: Any) =
	add("archives", artifactNotation)
