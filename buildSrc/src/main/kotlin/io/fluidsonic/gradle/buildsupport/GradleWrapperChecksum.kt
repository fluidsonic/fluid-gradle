package io.fluidsonic.gradle.buildsupport

import org.gradle.api.GradleException
import java.net.URI


private val sha256ChecksumRegex = Regex("[0-9a-f]{64}")


/**
 * Parses [raw] as a Gradle distribution SHA-256 checksum.
 *
 * Trims surrounding whitespace and lowercases the result. Returns the normalized value if it consists of exactly 64
 * hexadecimal characters, otherwise `null`.
 */
internal fun parseSha256Checksum(raw: String): String? = raw.trim().lowercase().takeIf { sha256ChecksumRegex.matches(it) }


/**
 * Downloads the SHA-256 checksum published alongside the Gradle distribution at [distributionUrl].
 *
 * Fetches `"$distributionUrl.sha256"` with 10-second connect and read timeouts and parses the response as a checksum.
 *
 * @return the normalized 64-character lowercase hexadecimal checksum.
 * @throws GradleException if the response exceeds 1024 bytes or the fetched content is not a valid SHA-256 checksum.
 */
public fun fetchGradleDistributionSha256Sum(distributionUrl: String): String {
	val checksumUrl = "$distributionUrl.sha256"
	val raw = URI(checksumUrl).toURL().openConnection().run {
		connectTimeout = 10_000
		readTimeout = 10_000

		getInputStream().use { input ->
			val bytes = input.readNBytes(1025)
			if (bytes.size > 1024) {
				throw GradleException("Downloaded Gradle distribution checksum from $checksumUrl exceeds the maximum size of 1024 bytes.")
			}

			bytes.toString(Charsets.UTF_8)
		}
	}

	val diagnostic = if (raw.length > 200) raw.take(199) + "…" else raw

	return parseSha256Checksum(raw)
		?: throw GradleException("Downloaded Gradle distribution checksum from $checksumUrl is not a valid SHA-256 sum: $diagnostic")
}
