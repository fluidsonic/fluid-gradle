package io.fluidsonic.gradle.buildsupport

import kotlin.test.Test
import kotlin.test.assertEquals


class VersionStabilityTest {

	@Test
	fun flagsUnstableVersions() {
		for (version in listOf("1.2.0-alpha", "2.0.0-rc1", "1.0-SNAPSHOT", "3.0.0-beta2", "1.0.0-eap", "1.0.0-m1"))
			assertEquals(actual = isUnstableVersion(version), expected = true, message = version)
	}

	@Test
	fun passesStableVersions() {
		for (version in listOf("1.2.0", "3.0.0"))
			assertEquals(actual = isUnstableVersion(version), expected = false, message = version)
	}
}
