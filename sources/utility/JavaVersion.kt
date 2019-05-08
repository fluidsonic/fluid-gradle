package com.github.fluidsonic.fluid.library

import org.gradle.api.*


@Suppress("EnumEntryName")
enum class JDK {

	v1_7,
	v1_8;


	override fun toString() =
		when (this) {
			v1_7 -> "1.7"
			v1_8 -> "1.8"
		}
}


val JDK.moduleId
	get() = when (this) {
		JDK.v1_7 -> "jdk7"
		JDK.v1_8 -> "jdk8"
	}


fun JDK.toGradle() =
	when (this) {
		JDK.v1_7 -> JavaVersion.VERSION_1_7
		JDK.v1_8 -> JavaVersion.VERSION_1_8
	}


fun JDK.toKotlinTarget() =
	when (this) {
		JDK.v1_7 -> "1.6"
		JDK.v1_8 -> "1.8"
	}
