pluginManagement {
	repositories {
		bintray("kotlin/kotlin-eap")
		gradlePluginPortal()
		jcenter()
	}
}

rootProject.name = "fluid-gradle"


fun RepositoryHandler.bintray(name: String) =
	maven("https://dl.bintray.com/$name")
