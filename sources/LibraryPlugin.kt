package io.fluidsonic.gradle

import org.gradle.api.*


/** Marker plugin for fluid-gradle. Actual configuration happens via [fluidLibrary] and [fluidLibraryModule]. */
public class LibraryPlugin : Plugin<Project> {

	override fun apply(project: Project) {}
}
