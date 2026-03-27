package io.fluidsonic.gradle

import kotlin.test.*


class IsUnstableVersionTest {

	@Test
	fun stableVersions_returnFalse() {
		assertFalse(isUnstableVersion("1.0.0"))
		assertFalse(isUnstableVersion("2.3.4"))
		assertFalse(isUnstableVersion("1.0"))
	}

	@Test
	fun alphaVersions_returnTrue() {
		assertTrue(isUnstableVersion("1.0.0-alpha"))
		assertTrue(isUnstableVersion("1.0.0-alpha1"))
		assertTrue(isUnstableVersion("1.0.0-ALPHA"))
	}

	@Test
	fun betaVersions_returnTrue() {
		assertTrue(isUnstableVersion("1.0.0-beta"))
		assertTrue(isUnstableVersion("1.0.0-beta2"))
		assertTrue(isUnstableVersion("1.0.0-BETA"))
	}

	@Test
	fun devVersions_returnTrue() {
		assertTrue(isUnstableVersion("1.0.0-dev"))
		assertTrue(isUnstableVersion("1.0.0-DEV"))
	}

	@Test
	fun eapVersions_returnTrue() {
		assertTrue(isUnstableVersion("1.0.0-eap"))
		assertTrue(isUnstableVersion("1.0.0-EAP"))
	}

	@Test
	fun rcVersions_returnTrue() {
		assertTrue(isUnstableVersion("1.0.0-rc"))
		assertTrue(isUnstableVersion("1.0.0-rc1"))
		assertTrue(isUnstableVersion("1.0.0-RC"))
	}

	@Test
	fun snapshotVersions_returnTrue() {
		assertTrue(isUnstableVersion("1.0.0-SNAPSHOT"))
		assertTrue(isUnstableVersion("1.0.0-snapshot"))
	}

	@Test
	fun mVersions_returnTrue() {
		assertTrue(isUnstableVersion("1.0.0-m1"))
		assertTrue(isUnstableVersion("1.0.0-M1"))
	}
}
