package io.fluidsonic.gradle

import io.fluidsonic.gradle.FaultInjectingMavenServer.Companion.HTTP_CONFLICT
import io.fluidsonic.gradle.FaultInjectingMavenServer.Companion.HTTP_CREATED
import io.fluidsonic.gradle.FaultInjectingMavenServer.Companion.HTTP_NOT_FOUND
import io.fluidsonic.gradle.FaultInjectingMavenServer.Companion.HTTP_OK
import io.fluidsonic.gradle.FaultInjectingMavenServer.Companion.HTTP_SERVICE_UNAVAILABLE
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Exercises the plugin's GitHub Packages snapshot retry against [FaultInjectingMavenServer], which reproduces the real
 * failure: a lost `PUT` response makes Gradle's own upload retry re-send the file, and the repository answers `409`.
 */
class GithubPackagesPublishRetryFunctionalTest {

	@Test
	fun snapshotPublish_republishesWithNewBuildNumberAfterLostPutResponse() {
		FaultInjectingMavenServer(faultPredicate = ::isSourcesJar).use { server ->
			val projectDirectory = createLibraryProject(version = "1.0.0-SNAPSHOT", serverUrl = server.url, withConsumerDoLast = true)

			val result = runGradle(projectDirectory, GITHUB_PUBLISH_TASK)
			val output = result.output

			assertEquals(
				actual = output.occurrenceCount("attempt 1 of 3 failed"),
				expected = 1,
				message = "expected exactly one failed-attempt line but got:\n$output",
			)
			assertEquals(
				actual = output.occurrenceCount(REPUBLISHING_MESSAGE),
				expected = 1,
				message = "expected exactly one republishing line but got:\n$output",
			)
			// A consumer's `doLast` added after the plugin configured the module survives the wrapper and runs once, after it.
			assertEquals(
				actual = output.occurrenceCount(CONSUMER_DO_LAST_MARKER),
				expected = 1,
				message = "expected the consumer's doLast to run exactly once but got:\n$output",
			)
			assertTrue(
				actual = output.lastIndexOf(REPUBLISHING_MESSAGE) < output.indexOf(CONSUMER_DO_LAST_MARKER),
				message = "expected the consumer's doLast to run after the retried publish but got:\n$output",
			)

			val faultedPath = assertNotNull(server.faultedPath, "expected the fault to have fired (${server.requests})")
			assertTrue(
				actual = faultedPath.endsWith("-1-sources.jar"),
				message = "expected the build-1 sources jar to be faulted but got $faultedPath",
			)
			assertEquals(
				actual = server.statusesFor(faultedPath),
				expected = listOf(HTTP_SERVICE_UNAVAILABLE, HTTP_CONFLICT),
				message = "expected Gradle's own retry to reproduce the 409 on $faultedPath (${server.requests})",
			)

			val metadataPath = server.storedPaths.single { it.endsWith("/1.0.0-SNAPSHOT/maven-metadata.xml") }
			// Attempt 1 finds no metadata (build 1); attempt 2 reads the server-generated metadata of the partial build 1
			// (build 2) and finally uploads its own metadata.
			assertEquals(
				actual = server.requestsFor(metadataPath),
				expected = listOf("GET" to HTTP_NOT_FOUND, "GET" to HTTP_OK, "PUT" to HTTP_CREATED),
				message = "expected the snapshot metadata to be read once per attempt and written once (${server.requests})",
			)

			// The stored body is Gradle's own upload; the generated one is what the server reports from the stored files.
			val metadata = assertNotNull(server.storedText(metadataPath))
			assertTrue(
				actual = metadata.contains("<buildNumber>2</buildNumber>"),
				message = "expected build number 2 in Gradle's uploaded snapshot metadata but got:\n$metadata",
			)
			val generatedMetadata = assertNotNull(server.generatedSnapshotMetadata(metadataPath))
			assertTrue(
				actual = generatedMetadata.contains("<buildNumber>2</buildNumber>"),
				message = "expected build number 2 in the server-generated snapshot metadata but got:\n$generatedMetadata",
			)

			assertBuildTwoIsComplete(server = server, metadata = metadata, metadataPath = metadataPath)
		}
	}

	/** Asserts that every build-2 file Gradle's uploaded [metadata] names is stored together with its four checksums. */
	private fun assertBuildTwoIsComplete(server: FaultInjectingMavenServer, metadata: String, metadataPath: String) {
		val buildTwoFiles = snapshotVersionFiles(metadata = metadata, directory = metadataPath.substringBeforeLast('/'))
			.filter { it.value.endsWith("-2") }
		assertEquals(
			actual = buildTwoFiles.size,
			expected = 5,
			message = "expected jar, sources jar, javadoc jar, pom and module for build 2 but got ${buildTwoFiles.map { it.fileName }}:\n$metadata",
		)
		for (file in buildTwoFiles) {
			for (suffix in listOf("", ".md5", ".sha1", ".sha256", ".sha512")) {
				assertTrue(
					actual = "${file.path}$suffix" in server.storedPaths,
					message = "expected ${file.path}$suffix to be stored but the store holds:\n${server.storedPaths.sorted().joinToString("\n")}",
				)
			}
		}
	}

	@Test
	fun releasePublish_isNotRetried() {
		FaultInjectingMavenServer(faultPredicate = ::isSourcesJar).use { server ->
			val projectDirectory = createLibraryProject(version = "1.0.0", serverUrl = server.url)

			val result = runGradleAndFail(projectDirectory, GITHUB_PUBLISH_TASK)
			val output = result.output

			assertTrue(
				actual = output.contains("409"),
				message = "expected the 409 to surface but got:\n$output",
			)
			assertFalse(
				actual = output.contains("republishing"),
				message = "expected no retry for a release version but got:\n$output",
			)

			val faultedPath = assertNotNull(server.faultedPath, "expected the fault to have fired (${server.requests})")
			assertEquals(
				actual = server.statusesFor(faultedPath),
				expected = listOf(HTTP_SERVICE_UNAVAILABLE, HTTP_CONFLICT),
				message = "expected only Gradle's own retry on $faultedPath (${server.requests})",
			)
		}
	}

	@Test
	fun nonGithubRepository_isNotRetried() {
		FaultInjectingMavenServer(faultPredicate = ::isSourcesJar).use { server ->
			val projectDirectory = createLibraryProject(version = "1.0.0-SNAPSHOT", serverUrl = server.url, withOtherRepository = true)

			val result = runGradleAndFail(projectDirectory, ":module:publishJvmPublicationToOtherRepository")
			val output = result.output

			assertTrue(
				actual = output.contains("409"),
				message = "expected the 409 to surface but got:\n$output",
			)
			assertFalse(
				actual = output.contains("republishing"),
				message = "expected no retry for a non-GitHub repository but got:\n$output",
			)

			val faultedPath = assertNotNull(server.faultedPath, "expected the fault to have fired (${server.requests})")
			assertEquals(
				actual = server.statusesFor(faultedPath),
				expected = listOf(HTTP_SERVICE_UNAVAILABLE, HTTP_CONFLICT),
				message = "expected only Gradle's own retry on $faultedPath (${server.requests})",
			)
			assertTrue(
				actual = server.storedPaths.none { buildNumberRegex(2).containsMatchIn(it) },
				message = "expected no build-2 files but the store holds:\n${server.storedPaths.sorted().joinToString("\n")}",
			)
		}
	}

	@Test
	fun githubPublishTask_isConfigurationCacheCompatible() {
		FaultInjectingMavenServer(faultPredicate = ::isSourcesJar).use { server ->
			// Gradle marks PublishToMavenRepository as incompatible with the configuration cache as soon as the repository
			// carries explicit `credentials { username/password }`, which the plugin's real `github` repository does. A
			// credential-less repository of the same name isolates what this test is about: the retry wrapper's own action.
			val projectDirectory = createLibraryProject(
				version = "1.0.0-SNAPSHOT",
				serverUrl = server.url,
				githubRepository = GithubRepository.RegisteredByTestWithoutCredentials,
			)

			// Not a dry run: the wrapper action is stored, reloaded from the cache on the second run, and executed both times.
			val firstRun = runGradle(projectDirectory, GITHUB_PUBLISH_TASK, "--configuration-cache")

			assertTrue(
				actual = firstRun.output.contains("Configuration cache entry stored"),
				message = "expected a configuration cache entry to be stored but got:\n${firstRun.output}",
			)
			assertFalse(
				actual = CONFIGURATION_CACHE_PROBLEMS_REGEX.containsMatchIn(firstRun.output),
				message = "expected no configuration cache problems but got:\n${firstRun.output}",
			)
			assertEquals(
				actual = firstRun.output.occurrenceCount(REPUBLISHING_MESSAGE),
				expected = 1,
				message = "expected the wrapper to retry once under the configuration cache but got:\n${firstRun.output}",
			)

			val secondRun = runGradle(projectDirectory, GITHUB_PUBLISH_TASK, "--configuration-cache")

			assertTrue(
				actual = secondRun.output.contains("Reusing configuration cache"),
				message = "expected the configuration cache to be reused but got:\n${secondRun.output}",
			)
			// The first run left builds 1 (partial) and 2, so only the reloaded action running again can produce build 3.
			assertTrue(
				actual = server.storedPaths.any { buildNumberRegex(BUILD_NUMBER_AFTER_RELOADED_RUN).containsMatchIn(it) },
				message = "expected the reloaded wrapper action to publish build 3 but the store holds:\n${server.storedPaths.sorted().joinToString("\n")}",
			)
		}
	}

	private class SnapshotVersionFile(val value: String, val fileName: String, directory: String) {

		val path = "$directory/$fileName"
	}

	/** Parses the `<snapshotVersion>` entries of a versioned `maven-metadata.xml` into the artifact files they name. */
	private fun snapshotVersionFiles(metadata: String, directory: String): List<SnapshotVersionFile> {
		val artifactId = assertNotNull(ARTIFACT_ID_REGEX.find(metadata), "expected an artifactId in:\n$metadata").groupValues[1]

		return SNAPSHOT_VERSION_REGEX.findAll(metadata).map { match ->
			val entry = match.groupValues[1]
			val classifier = CLASSIFIER_REGEX.find(entry)?.groupValues?.get(1)
			val extension = assertNotNull(EXTENSION_REGEX.find(entry), "expected an extension in:\n$entry").groupValues[1]
			val value = assertNotNull(VALUE_REGEX.find(entry), "expected a value in:\n$entry").groupValues[1]
			val fileName = buildString {
				append(artifactId)
				append('-')
				append(value)
				if (classifier != null) {
					append('-')
					append(classifier)
				}
				append('.')
				append(extension)
			}

			SnapshotVersionFile(value = value, fileName = fileName, directory = directory)
		}.toList()
	}

	private enum class GithubRepository {

		/** The plugin registers `github` (with explicit credentials) because `GITHUB_PACKAGES_AUTH_TOKEN` is set. */
		RegisteredByPlugin,

		/** No token is set; the module script registers a credential-less `github` repository itself. */
		RegisteredByTestWithoutCredentials,
	}

	private fun createLibraryProject(
		version: String,
		serverUrl: String,
		withOtherRepository: Boolean = false,
		withConsumerDoLast: Boolean = false,
		githubRepository: GithubRepository = GithubRepository.RegisteredByPlugin,
	): File {
		val projectDirectory = createFunctionalTestDirectory()

		projectDirectory.resolve("settings.gradle.kts").writeText(
			"""
				rootProject.name = "fluid-test"
				include(":module")
			""".trimIndent(),
		)
		projectDirectory.resolve("build.gradle.kts").writeText(
			"""
				import io.fluidsonic.gradle.fluidLibrary

				plugins {
					id("io.fluidsonic.gradle")
				}

				fluidLibrary(name = "test", version = "$version")
			""".trimIndent(),
		)
		if (githubRepository == GithubRepository.RegisteredByPlugin) {
			// The token makes the plugin register the `github` repository; the module script then repoints it at the fake server.
			projectDirectory.resolve("gradle.properties").writeText("GITHUB_PACKAGES_AUTH_TOKEN=dummy\n")
		}

		val moduleDirectory = projectDirectory.resolve("module")
		moduleDirectory.mkdirs()
		moduleDirectory.resolve("build.gradle.kts").writeText(
			moduleBuildScript(
				repositories = listOfNotNull(
					githubRepositoryScript(githubRepository = githubRepository, serverUrl = serverUrl),
					otherRepositoryScript(serverUrl = serverUrl).takeIf { withOtherRepository },
				),
				withConsumerDoLast = withConsumerDoLast,
			),
		)

		return projectDirectory
	}

	private fun githubRepositoryScript(githubRepository: GithubRepository, serverUrl: String) = when (githubRepository) {
		GithubRepository.RegisteredByPlugin ->
			"""
				repositories.named<MavenArtifactRepository>("github") {
					setUrl("$serverUrl")
					isAllowInsecureProtocol = true
				}
			"""

		GithubRepository.RegisteredByTestWithoutCredentials ->
			"""
				repositories.maven {
					name = "github"
					setUrl("$serverUrl")
					isAllowInsecureProtocol = true
				}
			"""
	}

	private fun otherRepositoryScript(serverUrl: String) = """
		repositories.maven {
			name = "other"
			setUrl("$serverUrl")
			isAllowInsecureProtocol = true
		}
	"""

	private fun consumerDoLastScript() = """
		tasks.named("publishJvmPublicationToGithubRepository") {
			doLast {
				println("$CONSUMER_DO_LAST_MARKER")
			}
		}
	"""

	private fun moduleBuildScript(repositories: List<String>, withConsumerDoLast: Boolean) = """
		import io.fluidsonic.gradle.fluidLibraryModule
		import org.gradle.api.artifacts.repositories.MavenArtifactRepository
		import org.gradle.api.publish.PublishingExtension
		import org.gradle.plugins.signing.SigningExtension

		plugins {
			id("io.fluidsonic.gradle")
		}

		fluidLibraryModule(description = "Test module") {
			disableDokka()

			targets {
				jvm()
			}
		}

		extensions.configure<PublishingExtension> {
			${repositories.joinToString("\n")}
		}

		// No signatory in the test: Sign is skipped and its derived .asc artifacts are not published.
		extensions.configure<SigningExtension> {
			setRequired(false)
		}

		${if (withConsumerDoLast) consumerDoLastScript() else ""}
	""".trimIndent()

	private companion object {

		/** Builds 1 (partial) and 2 come from the first run's retry, so the second run must publish build 3. */
		const val BUILD_NUMBER_AFTER_RELOADED_RUN = 3
		const val CONSUMER_DO_LAST_MARKER = "CONSUMER_DO_LAST_RAN"
		const val GITHUB_PUBLISH_TASK = ":module:publishJvmPublicationToGithubRepository"
		const val REPUBLISHING_MESSAGE = "republishing with a new snapshot build number"

		val ARTIFACT_ID_REGEX = Regex("<artifactId>(.*?)</artifactId>")
		val CLASSIFIER_REGEX = Regex("<classifier>(.*?)</classifier>")
		val CONFIGURATION_CACHE_PROBLEMS_REGEX = Regex("problems? (was|were) found")
		val EXTENSION_REGEX = Regex("<extension>(.*?)</extension>")
		val SNAPSHOT_VERSION_REGEX = Regex("<snapshotVersion>(.*?)</snapshotVersion>", RegexOption.DOT_MATCHES_ALL)
		val VALUE_REGEX = Regex("<value>(.*?)</value>")

		/** Matches a stored path of a timestamped snapshot artifact of the given build number. */
		fun buildNumberRegex(buildNumber: Int) = Regex("-\\d{8}\\.\\d{6}-$buildNumber[.-]")

		fun isSourcesJar(path: String) = path.endsWith("-sources.jar")
	}
}
