package io.fluidsonic.gradle


enum class JvmTarget {

	jdk7,
	jdk8;


	val jvmVersionCode
		get() = when (this) {
			jdk7 -> 7
			jdk8 -> 8
		}
}
