package io.fluidsonic.gradle


internal enum class JdkVersion(
	val code: Int,
	val kotlinJvmTargetValue: String
) {

	v7(code = 7, kotlinJvmTargetValue = "1.6"),
	v8(code = 8, kotlinJvmTargetValue = "1.8"),
	v9(code = 9, kotlinJvmTargetValue = "9"),
	v10(code = 10, kotlinJvmTargetValue = "10"),
	v11(code = 11, kotlinJvmTargetValue = "11"),
	v12(code = 12, kotlinJvmTargetValue = "12"),
	v13(code = 13, kotlinJvmTargetValue = "13"),
	v14(code = 14, kotlinJvmTargetValue = "14"),
	v15(code = 15, kotlinJvmTargetValue = "14");


	override fun toString() =
		code.toString()
}
