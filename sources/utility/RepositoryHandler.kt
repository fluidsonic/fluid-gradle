package com.github.fluidsonic.fluid.library

import org.gradle.api.artifacts.dsl.*
import org.gradle.kotlin.dsl.*


fun RepositoryHandler.bintray(path: String) =
	maven("https://dl.bintray.com/$path")
