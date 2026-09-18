package io.fluidsonic.gradle

import org.gradle.api.logging.Logging
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

/**
 * Logs one lifecycle-level summary line per test task once its root suite completes, so that a quiet green run still
 * reports that tests actually ran — e.g. `:module:jvmTest — 50 tests, all passed`.
 *
 * [taskPath] is the path of the test task the summary is reported for. It is the only state captured, which keeps the
 * listener compatible with the configuration cache.
 */
internal class TestSummaryLogger(private val taskPath: String) : TestListener {

	override fun afterSuite(suite: TestDescriptor, result: TestResult) {
		// Every test class and engine reports its own suite. Only the root suite (the one without a parent) aggregates
		// the entire task, so all others are skipped to keep the output at one line per task.
		if (suite.parent != null) return

		logger.lifecycle("$taskPath — ${summarize(result)}")
	}

	override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit

	override fun beforeSuite(suite: TestDescriptor) = Unit

	override fun beforeTest(testDescriptor: TestDescriptor) = Unit

	private fun summarize(result: TestResult): String {
		val total = result.testCount
		val failed = result.failedTestCount
		val skipped = result.skippedTestCount
		val passed = total - failed - skipped

		return when {
			total == 0L -> "no tests ran"

			failed == 0L && skipped == 0L -> "$total tests, all passed"

			else -> buildString {
				append(total)
				append(" tests, ")
				append(passed)
				append(" passed")

				if (failed > 0L) {
					append(", ")
					append(failed)
					append(" failed")
				}

				if (skipped > 0L) {
					append(", ")
					append(skipped)
					append(" skipped")
				}
			}
		}
	}

	private companion object {

		// Obtained statically rather than from the task or project so that no Gradle model object is captured.
		val logger = Logging.getLogger(TestSummaryLogger::class.java)
	}
}
