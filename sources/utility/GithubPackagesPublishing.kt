package io.fluidsonic.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.withType

private const val MAX_ATTEMPTS = 3
private const val MAX_CAUSE_DEPTH = 20
private const val RETRY_DELAY_MILLIS = 5_000L

/**
 * Makes every `publish…ToGithubRepository` task republish a SNAPSHOT publication when a publish attempt fails.
 *
 * GitHub Packages answers a repeated `PUT` of an already stored path with `409 Conflict`. When the response to a
 * successful upload is lost, Gradle's own upload retry re-sends the same `PUT`, receives the `409`, and fails the task
 * even though the file is already there. Republishing a SNAPSHOT is safe: the retry re-reads the remote
 * `maven-metadata.xml`, which GitHub Packages generates from the files already stored (including the partial build of
 * the failed attempt), and takes the next build number, so no path is uploaded twice. Release versions have fixed paths
 * and are therefore never retried. Gradle logs a "published multiple times to the same location" warning on the
 * re-publish; that is expected and harmless.
 *
 * Only tasks of type [PublishToMavenRepository] whose name ends in `ToGithubRepository` are wrapped: the Sonatype tasks
 * (`…ToSonatypeRepository`) and `publishToMavenLocal` (a different task type) are untouched. The wrapper replaces the
 * task's action list, so a `doFirst`/`doLast` added after this configuration has run still attaches around the wrapper.
 */
internal fun Project.retryGithubSnapshotPublications() {
	tasks.withType<PublishToMavenRepository>()
		.named { it.endsWith("ToGithubRepository") }
		.configureEach {
			// Only the task and a provider of the version are captured, which keeps the action compatible with the
			// configuration cache. The task's `publication` is discarded when the task is serialized, so it must not be read
			// at execution time.
			val task = this
			val publicationVersion = project.provider { publication.version }
			setActions(listOf(Action<Task> { task.publishWithSnapshotRetry(isSnapshot = publicationVersion.get().endsWith("-SNAPSHOT")) }))
		}
}

// A retry wrapper must re-run on whatever `publish()` threw; the failure type is not known in advance.
@Suppress("TooGenericExceptionCaught")
private fun PublishToMavenRepository.publishWithSnapshotRetry(isSnapshot: Boolean) {
	for (attempt in 1..MAX_ATTEMPTS) {
		try {
			publish()
			return
		} catch (e: Exception) {
			if (!isSnapshot || attempt >= MAX_ATTEMPTS) {
				throw e
			}

			logger.warn(
				"{}: attempt {} of {} failed ({}); republishing with a new snapshot build number.",
				path,
				attempt,
				MAX_ATTEMPTS,
				e.rootCauseMessage(),
			)
			sleepBeforeRetry(millis = RETRY_DELAY_MILLIS * attempt, failure = e)
		}
	}
}

// A build cancelled during the delay must surface the publish failure, not the interruption, and keep the interrupt flag.
private fun sleepBeforeRetry(millis: Long, failure: Exception) {
	try {
		Thread.sleep(millis)
	} catch (interruption: InterruptedException) {
		Thread.currentThread().interrupt()
		failure.addSuppressed(interruption)
		throw failure
	}
}

/** The message of the deepest cause, bounded so a cyclic cause chain cannot hang the build; falls back to the class name. */
internal fun Throwable.rootCauseMessage(): String {
	val rootCause = generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH).last()

	return rootCause.message ?: rootCause.javaClass.name
}
