package com.github.fluidsonic.fluid.library

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*


val NamedDomainObjectContainer<KotlinSourceSet>.commonMain
	get() = named<KotlinSourceSet>("commonMain")


val NamedDomainObjectContainer<KotlinSourceSet>.commonTest
	get() = named<KotlinSourceSet>("commonTest")


val NamedDomainObjectContainer<KotlinSourceSet>.iosArm64Main
	get() = named<KotlinSourceSet>("iosArm64Main")


val NamedDomainObjectContainer<KotlinSourceSet>.iosArm64Test
	get() = named<KotlinSourceSet>("iosArm64Test")


val NamedDomainObjectContainer<KotlinSourceSet>.iosX64Main
	get() = named<KotlinSourceSet>("iosX64Main")


val NamedDomainObjectContainer<KotlinSourceSet>.iosX64Test
	get() = named<KotlinSourceSet>("iosX64Test")


val NamedDomainObjectContainer<KotlinSourceSet>.jvmMain
	get() = named<KotlinSourceSet>("jvmMain")


val NamedDomainObjectContainer<KotlinSourceSet>.jvmTest
	get() = named<KotlinSourceSet>("jvmTest")


val NamedDomainObjectContainer<KotlinSourceSet>.macosX64Main
	get() = named<KotlinSourceSet>("macosX64Main")


val NamedDomainObjectContainer<KotlinSourceSet>.macosX64Test
	get() = named<KotlinSourceSet>("macosX64Test")


val NamedDomainObjectContainer<KotlinSourceSet>.objcMain
	get() = named<KotlinSourceSet>("objcMain")


val NamedDomainObjectContainer<KotlinSourceSet>.objcTest
	get() = named<KotlinSourceSet>("objcTest")
