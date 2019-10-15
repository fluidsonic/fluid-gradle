package com.github.fluidsonic.fluid.library

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.api.tasks.bundling.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*


class FluidJvmLibraryVariantConfiguration private constructor(
	private val project: Project
) {

	var enforcesSameVersionForAllKotlinDependencies = true
	var publishing = true
	var jdk = JvmTarget.jdk7


	private fun Project.configureBasics() {
		apply<KotlinPlatformJvmPlugin>()
		apply<JavaLibraryPlugin>()

		group = "com.github.fluidsonic"
		version = fluidLibrary.version

		if (enforcesSameVersionForAllKotlinDependencies)
			configurations {
				all {
					resolutionStrategy.eachDependency {
						if (requested.group == "org.jetbrains.kotlin") {
							useVersion(getKotlinPluginVersion()!!)
							because("All Kotlin modules must have the same version.")
						}
					}
				}
			}

		dependencies {
			api(platform(kotlin("bom")))
			api(kotlin("stdlib-${jdk.kotlinStdlibVariant}"))
		}

		java {
			sourceCompatibility = jdk.gradleJavaVersion
			targetCompatibility = jdk.gradleJavaVersion
		}

		sourceSets {
			getByName("main") {
				kotlin.setSrcDirs(listOf("sources"))
				resources.setSrcDirs(listOf("resources"))
			}

			getByName("test") {
				kotlin.setSrcDirs(listOf("tests/sources"))
				resources.setSrcDirs(listOf("tests/resources"))
			}
		}

		tasks {
			withType<KotlinCompile> {
				sourceCompatibility = jdk.toString()
				targetCompatibility = jdk.toString()

				kotlinOptions.freeCompilerArgs = listOf(
					"-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
					"-XXLanguage:+InlineClasses"
				)
				kotlinOptions.jvmTarget = jdk.kotlinJvmTargetVersion
			}
		}

		repositories {
			mavenCentral()
			jcenter()
			bintray("fluidsonic/maven")
			bintray("kotlin/kotlin-eap")
			bintray("kotlin/kotlinx")
		}
	}


	private fun configureProject(): Unit = project.run {
		configureBasics()

		if (this@FluidJvmLibraryVariantConfiguration.publishing)
			configurePublishing()
	}


	private fun Project.configurePublishing() {
		val bintrayUser = findProperty("bintrayUser") as String? ?: return
		val bintrayKey = findProperty("bintrayApiKey") as String? ?: return
		val library = fluidLibrary

		apply<MavenPublishPlugin>()
		apply<SigningPlugin>()

		val javadocJar by tasks.creating(Jar::class) {
			archiveClassifier.set("javadoc")
			from(tasks["javadoc"])
		}

		val sourcesJar by tasks.creating(Jar::class) {
			archiveClassifier.set("sources")
			from(sourceSets["main"].allSource, file("build/generated/source/kaptKotlin/main"))
		}

		publishing {
			repositories {
				maven {
					setUrl("https://api.bintray.com/maven/fluidsonic/maven/${library.name}/")
					credentials {
						username = bintrayUser
						password = bintrayKey
					}
				}
			}

			publications {
				create<MavenPublication>("default") {
					from(components["java"])
					artifact(javadocJar)
					artifact(sourcesJar)
				}

				filterIsInstance<MavenPublication>().forEach { publication ->
					publication.pom {
						name.set(project.name)
						description.set(project.description)
						packaging = "jar"
						url.set("https://github.com/fluidsonic/${library.name}")
						developers {
							developer {
								id.set("fluidsonic")
								name.set("Marc Knaup")
								email.set("marc@knaup.io")
							}
						}
						licenses {
							license {
								name.set("Apache License 2.0")
								url.set("https://github.com/fluidsonic/${library.name}/blob/master/LICENSE")
							}
						}
						scm {
							connection.set("scm:git:https://github.com/fluidsonic/${library.name}.git")
							developerConnection.set("scm:git:git@github.com:fluidsonic/${library.name}.git")
							url.set("https://github.com/fluidsonic/${library.name}")
						}
					}
				}
			}
		}

		signing {
			sign(publishing.publications)
		}
	}


	var description
		get() = project.description
		set(value) {
			project.description = value
		}


	companion object {

		internal fun applyTo(project: Project, configure: FluidJvmLibraryVariantConfiguration.() -> Unit = {}) {
			FluidJvmLibraryVariantConfiguration(project = project).apply(configure).configureProject()
		}


		val JvmTarget.gradleJavaVersion
			get() = when (this) {
				JvmTarget.jdk7 -> JavaVersion.VERSION_1_7
				JvmTarget.jdk8 -> JavaVersion.VERSION_1_8
			}


		val JvmTarget.kotlinStdlibVariant
			get() = when (this) {
				JvmTarget.jdk7 -> "jdk7"
				JvmTarget.jdk8 -> "jdk8"
			}


		val JvmTarget.kotlinJvmTargetVersion
			get() = when (this) {
				JvmTarget.jdk7 -> "1.6"
				JvmTarget.jdk8 -> "1.8"
			}

	}
}
