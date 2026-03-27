package io.fluidsonic.gradle

import kotlin.test.*


class VersionsTest {

	@Test
	fun junit_isSet() {
		assertTrue(Versions.junit.isNotEmpty())
	}

	@Test
	fun gradle_isSet() {
		assertTrue(Versions.gradle.isNotEmpty())
	}
}
