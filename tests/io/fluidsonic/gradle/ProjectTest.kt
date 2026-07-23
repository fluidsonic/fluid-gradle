package io.fluidsonic.gradle

import org.gradle.testfixtures.*
import kotlin.test.*


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
