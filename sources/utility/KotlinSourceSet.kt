package com.github.fluidsonic.fluid.library

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*


val NamedDomainObjectContainer<KotlinSourceSet>.commonMain
	get() = named<KotlinSourceSet>("commonMain")


val NamedDomainObjectContainer<KotlinSourceSet>.commonTest
	get() = named<KotlinSourceSet>("commonTest")


val NamedDomainObjectContainer<KotlinSourceSet>.jvmMain
	get() = named<KotlinSourceSet>("jvmMain")


val NamedDomainObjectContainer<KotlinSourceSet>.jvmTest
	get() = named<KotlinSourceSet>("jvmTest")
