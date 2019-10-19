package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.*
import org.gradle.kotlin.dsl.*


fun RepositoryHandler.bintray(path: String) =
	maven("https://dl.bintray.com/$path")
