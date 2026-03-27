package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.dsl.*


internal enum class JdkVersion(
	val code: Int,
	val kotlinJvmTargetValue: JvmTarget
) {

	v21(code = 21, kotlinJvmTargetValue = JvmTarget.JVM_21),
	v22(code = 22, kotlinJvmTargetValue = JvmTarget.JVM_22),
	v23(code = 23, kotlinJvmTargetValue = JvmTarget.JVM_23),
	v24(code = 24, kotlinJvmTargetValue = JvmTarget.JVM_24);


	override fun toString() =
		code.toString()
}
