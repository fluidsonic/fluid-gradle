package io.fluidsonic.gradle

import kotlin.test.Test
import kotlin.test.assertEquals

class RootCauseMessageTest {

	@Test
	fun linearChain_returnsTheDeepestMessage() {
		val root = IllegalStateException("Received status code 409 from server: Conflict")
		val wrapped = RuntimeException("Could not PUT", RuntimeException("Failed to publish", root))

		assertEquals(actual = wrapped.rootCauseMessage(), expected = "Received status code 409 from server: Conflict")
	}

	// A message-less exception is the subject of this test.
	@Suppress("ThrowingExceptionsWithoutMessageOrCause")
	@Test
	fun missingMessage_fallsBackToTheClassName() {
		val wrapped = RuntimeException("outer", IllegalStateException())

		assertEquals(actual = wrapped.rootCauseMessage(), expected = "java.lang.IllegalStateException")
	}

	@Test
	fun cyclicChain_terminates() {
		val first = RuntimeException("first")
		val second = RuntimeException("second", first)
		first.initCause(second)

		assertEquals(actual = first.rootCauseMessage(), expected = "second")
	}
}
