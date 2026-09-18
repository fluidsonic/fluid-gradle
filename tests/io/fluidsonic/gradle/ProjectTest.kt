package io.fluidsonic.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.parallel.ResourceLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// ProjectBuilder initializes process-global Gradle services; its users stay off each other's threads under concurrent tests.
@ResourceLock("ProjectBuilder")
class ProjectTest {

	@Test
	fun kotlin_withoutMultiplatformPlugin_throwsWithHelpfulMessage() {
		val project = ProjectBuilder.builder().build()

		val exception = assertFailsWith<IllegalStateException> {
			project.kotlin
		}
		assertEquals(
			actual = exception.message,
			expected = "The Kotlin multiplatform plugin must be applied before accessing project.kotlin.",
		)
	}
}
