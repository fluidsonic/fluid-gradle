package io.fluidsonic.gradle

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LibraryDslTest {

	@Test
	fun fluidLibrary_withBlankName_throws() {
		val project = ProjectBuilder.builder().withName("fluid-test").build()

		val exception = assertFailsWith<IllegalArgumentException> {
			project.fluidLibrary(name = "", version = "1.0.0")
		}
		assertEquals(actual = exception.message, expected = "'name' must not be empty.")
	}

	@Test
	fun fluidLibrary_withBlankVersion_throws() {
		val project = ProjectBuilder.builder().withName("fluid-test").build()

		val exception = assertFailsWith<IllegalArgumentException> {
			project.fluidLibrary(name = "test", version = "")
		}
		assertEquals(actual = exception.message, expected = "'version' must not be empty.")
	}

	@Test
	fun fluidLibrary_inChildProject_throws() {
		val rootProject = ProjectBuilder.builder().withName("fluid-test").build()
		val childProject = ProjectBuilder.builder().withParent(rootProject).withName("child").build()

		val exception = assertFailsWith<IllegalStateException> {
			childProject.fluidLibrary(name = "test", version = "1.0.0")
		}
		assertEquals(
			actual = exception.message,
			expected = "fluidLibrary(…) {} must only be used in the root project",
		)
	}

	@Test
	fun fluidLibrary_usedTwice_throws() {
		val project = ProjectBuilder.builder().withName("fluid-test").build()
		project.extensions.add(
			"io.fluidsonic.gradle",
			LibraryPluginExtension(
				configuration = LibraryConfigurationBuilder(
					fullName = "fluid-test",
					name = "test",
					version = "1.0.0",
				).build(),
			),
		)

		val exception = assertFailsWith<IllegalStateException> {
			project.fluidLibrary(name = "test", version = "1.0.0")
		}
		assertEquals(
			actual = exception.message,
			expected = "fluidLibrary/fluidJvmLibrary(…) {} must only be used once",
		)
	}

	@Test
	fun fluidLibrary_withNameDifferingFromLibraryName_throws() {
		val project = ProjectBuilder.builder().withName("wrong").build()

		val exception = assertFailsWith<IllegalStateException> {
			project.fluidLibrary(name = "test", version = "1.0.0")
		}
		assertEquals(
			actual = exception.message,
			expected = "Project name 'wrong' must not differ from library name 'fluid-test' in fluidLibrary(…)",
		)
	}
}
