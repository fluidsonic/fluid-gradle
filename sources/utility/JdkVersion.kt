package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.dsl.*


internal enum class JdkVersion(
	val code: Int,
	val kotlinJvmTargetValue: JvmTarget
) {

	v8(code = 8, kotlinJvmTargetValue = JvmTarget.JVM_1_8),
	v9(code = 9, kotlinJvmTargetValue = JvmTarget.JVM_9),
	v10(code = 10, kotlinJvmTargetValue = JvmTarget.JVM_10),
	v11(code = 11, kotlinJvmTargetValue = JvmTarget.JVM_11),
	v12(code = 12, kotlinJvmTargetValue = JvmTarget.JVM_12),
	v13(code = 13, kotlinJvmTargetValue = JvmTarget.JVM_13),
	v14(code = 14, kotlinJvmTargetValue = JvmTarget.JVM_14),
	v15(code = 15, kotlinJvmTargetValue = JvmTarget.JVM_15),
	v16(code = 16, kotlinJvmTargetValue = JvmTarget.JVM_16),
	v17(code = 17, kotlinJvmTargetValue = JvmTarget.JVM_17),
	v18(code = 18, kotlinJvmTargetValue = JvmTarget.JVM_18),
	v19(code = 19, kotlinJvmTargetValue = JvmTarget.JVM_19),
	v20(code = 20, kotlinJvmTargetValue = JvmTarget.JVM_20),
	v21(code = 21, kotlinJvmTargetValue = JvmTarget.JVM_21),
	v22(code = 22, kotlinJvmTargetValue = JvmTarget.JVM_22);


	override fun toString() =
		code.toString()
}
