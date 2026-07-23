package io.fluidsonic.gradle

import org.gradle.api.artifacts.repositories.*
import org.gradle.testfixtures.*
import kotlin.test.*


class RepositoryHandlerTest {

	@Test
	fun jitpack_registersJitPackMavenRepository() {
		val project = ProjectBuilder.builder().build()

		project.repositories.jitpack()

		val jitpackUrls = project.repositories
			.filterIsInstance<MavenArtifactRepository>()
			.map { it.url.toString() }

		assertTrue(
			actual = jitpackUrls.contains("https://jitpack.io"),
			message = "expected a Maven repository with URL https://jitpack.io, but found $jitpackUrls",
		)
	}
}
