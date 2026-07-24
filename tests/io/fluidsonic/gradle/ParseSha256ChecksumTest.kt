package io.fluidsonic.gradle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParseSha256ChecksumTest {

	@Test
	fun validChecksum_returnsIt() {
		assertEquals(
			actual = parseSha256Checksum("61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83"),
			expected = "61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83",
		)
	}

	@Test
	fun surroundingWhitespaceAndNewline_areTrimmed() {
		assertEquals(
			actual = parseSha256Checksum("  61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83\t"),
			expected = "61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83",
		)
		assertEquals(
			actual = parseSha256Checksum("61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83\n"),
			expected = "61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83",
		)
	}

	@Test
	fun uppercaseHex_isLowercased() {
		assertEquals(
			actual = parseSha256Checksum("61BA77B3FF7167E60962763EB4BAE79DB7120C189B9544358D0ADE3C1E712A83"),
			expected = "61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83",
		)
	}

	@Test
	fun wrongLength_returnsNull() {
		assertNull(parseSha256Checksum("61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a8"))
		assertNull(parseSha256Checksum("61ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a833"))
	}

	@Test
	fun nonHexContent_returnsNull() {
		assertNull(parseSha256Checksum("not a checksum"))
		assertNull(parseSha256Checksum("g1ba77b3ff7167e60962763eb4bae79db7120c189b9544358d0ade3c1e712a83"))
	}

	@Test
	fun emptyString_returnsNull() {
		assertNull(parseSha256Checksum(""))
	}
}
