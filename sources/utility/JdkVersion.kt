package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * A supported JVM toolchain version, ordered so that a later entry is "higher" than an earlier one.
 *
 * @property code The numeric JDK major version (e.g. `21`).
 * @property kotlinJvmTargetValue The matching Kotlin compiler [JvmTarget].
 */
internal enum class JdkVersion(val code: Int, val kotlinJvmTargetValue: JvmTarget) {

	v21(code = 21, kotlinJvmTargetValue = JvmTarget.JVM_21),
	v22(code = 22, kotlinJvmTargetValue = JvmTarget.JVM_22),
	v23(code = 23, kotlinJvmTargetValue = JvmTarget.JVM_23),
	v24(code = 24, kotlinJvmTargetValue = JvmTarget.JVM_24),
	;

	override fun toString() = code.toString()
}
