package io.fluidsonic.gradle

import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.parallel.ResourceLock
import kotlin.test.Test
import kotlin.test.assertTrue

// ProjectBuilder initializes process-global Gradle services; its users stay off each other's threads under concurrent tests.
@ResourceLock("ProjectBuilder")
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
