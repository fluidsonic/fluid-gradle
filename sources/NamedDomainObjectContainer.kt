package com.github.fluidsonic.fluid.library

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.named


internal val NamedDomainObjectContainer<Configuration>.archives
	get() = named<Configuration>("archives")
