package io.fluidsonic.gradle

import kotlin.test.Test
import kotlin.test.assertTrue

class VersionsTest {

	private companion object {

		// MAJOR.MINOR.PATCH with independently-optional pre-release and build-metadata suffixes
		// (e.g. 1.2.3, 1.2.3-rc.1, 1.2.3+build.5, 1.2.3-rc.1+build.5).
		val semanticVersion = Regex("""^\d+\.\d+\.\d+(?:-[0-9A-Za-z][0-9A-Za-z.-]*)?(?:\+[0-9A-Za-z][0-9A-Za-z.-]*)?$""")
	}

	@Test
	fun junit_isSemanticVersion() {
		assertTrue(
			actual = Versions.junit.matches(semanticVersion),
			message = "expected a semantic version, but got '${Versions.junit}'",
		)
	}

	@Test
	fun gradle_isSemanticVersion() {
		assertTrue(
			actual = Versions.gradle.matches(semanticVersion),
			message = "expected a semantic version, but got '${Versions.gradle}'",
		)
	}
}
