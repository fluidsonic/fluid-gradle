package com.github.fluidsonic.fluid.library

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.kotlin.dsl.*


internal val NamedDomainObjectContainer<Configuration>.archives
	get() = named<Configuration>("archives")
