package io.fluidsonic.gradle.buildsupport


private val unstableVersionRegex = Regex("\\b(alpha|beta|dev|eap|m|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE)


/**
 * Returns `true` if [version] looks like a pre-release/unstable version.
 *
 * A version is considered unstable when it contains any of the qualifiers
 * `alpha`, `beta`, `dev`, `eap`, `m`, `rc` or `snapshot` (case-insensitive),
 * optionally followed by a number (e.g. `1.0.0-rc1`, `1.0-SNAPSHOT`).
 */
public fun isUnstableVersion(version: String): Boolean =
	unstableVersionRegex.containsMatchIn(version)
