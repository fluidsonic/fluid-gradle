package io.fluidsonic.gradle

import org.jetbrains.kotlin.gradle.dsl.*
import kotlin.test.*


class JdkVersionTest {

	@Test
	fun code_returnsCorrectInteger() {
		assertEquals(actual = JdkVersion.v21.code, expected = 21)
		assertEquals(actual = JdkVersion.v22.code, expected = 22)
		assertEquals(actual = JdkVersion.v23.code, expected = 23)
		assertEquals(actual = JdkVersion.v24.code, expected = 24)
	}

	@Test
	fun kotlinJvmTargetValue_mapsCorrectly() {
		assertEquals(actual = JdkVersion.v21.kotlinJvmTargetValue, expected = JvmTarget.JVM_21)
		assertEquals(actual = JdkVersion.v22.kotlinJvmTargetValue, expected = JvmTarget.JVM_22)
		assertEquals(actual = JdkVersion.v23.kotlinJvmTargetValue, expected = JvmTarget.JVM_23)
		assertEquals(actual = JdkVersion.v24.kotlinJvmTargetValue, expected = JvmTarget.JVM_24)
	}

	@Test
	fun toString_returnsCodeAsString() {
		assertEquals(actual = JdkVersion.v21.toString(), expected = "21")
		assertEquals(actual = JdkVersion.v22.toString(), expected = "22")
		assertEquals(actual = JdkVersion.v23.toString(), expected = "23")
		assertEquals(actual = JdkVersion.v24.toString(), expected = "24")
	}

	@Test
	fun values_containsExpectedVersions() {
		val values = JdkVersion.entries
		assertEquals(actual = values.size, expected = 4)
		assertTrue(values.contains(JdkVersion.v21))
		assertTrue(values.contains(JdkVersion.v22))
		assertTrue(values.contains(JdkVersion.v23))
		assertTrue(values.contains(JdkVersion.v24))
	}
}
