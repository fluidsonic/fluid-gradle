package io.fluidsonic.gradle

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.maven

/**
 * Adds the JitPack repository (`https://jitpack.io`) to this project's repositories.
 *
 * JitPack builds artifacts on demand from arbitrary Git refs, so resolve only immutable coordinates — a release
 * tag or commit hash — and never a `-SNAPSHOT` or branch coordinate, whose contents can change between builds.
 *
 * JitPack artifacts carry weaker supply-chain guarantees than Maven Central: they are neither signed, immutable, nor
 * independently reviewed. Prefer Maven Central when a dependency is published there.
 */
public fun RepositoryHandler.jitpack(): MavenArtifactRepository = maven("https://jitpack.io")
