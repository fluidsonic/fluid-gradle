package com.github.fluidsonic.fluid.library

import com.jfrog.bintray.gradle.*
import com.jfrog.bintray.gradle.tasks.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.internal.artifact.*
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
	var jdk = JDK.v1_7


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
			api(kotlin("stdlib-${jdk.moduleId}"))
		}

		java {
			sourceCompatibility = jdk.toGradle()
			targetCompatibility = jdk.toGradle()
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

				kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.contracts.ExperimentalContracts")
				kotlinOptions.jvmTarget = jdk.toKotlinTarget()
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


	private fun Project.configureBintrayPublishing() {
		val bintrayUser = findProperty("bintrayUser") as String? ?: return
		val bintrayKey = findProperty("bintrayApiKey") as String? ?: return

		val library = fluidLibrary

		apply<BintrayPlugin>()
		configure<BintrayExtension> {
			user = bintrayUser
			key = bintrayKey

			setPublications("default")

			pkg.apply {
				repo = "maven"
				issueTrackerUrl = "https://github.com/fluidsonic/${library.name}/issues"
				name = library.name
				publicDownloadNumbers = true
				publish = true
				vcsUrl = "https://github.com/fluidsonic/${library.name}"
				websiteUrl = "https://github.com/fluidsonic/${library.name}"
				setLicenses("Apache-2.0")

				version.apply {
					name = library.version
					vcsTag = library.version
				}

				afterEvaluate {
					setPublications(*publishing.publications.names.toTypedArray())
				}
			}
		}

		tasks.withType<BintrayUploadTask> {
			doFirst {
				publishing.publications
					.filterIsInstance<MavenPublication>()
					.forEach { publication ->
						val moduleFile = buildDir.resolve("publications/${publication.name}/module.json")
						if (moduleFile.exists()) {
							publication.artifact(object : FileBasedMavenArtifact(moduleFile) {
								override fun getDefaultExtension() = "module"
							})
						}
					}
			}
		}
	}


	private fun Project.configureSonatypePublishing() {
		val sonatypeUserName = findProperty("sonatypeUserName") as String? ?: return
		val sonatypePassword = findProperty("sonatypePassword") as String? ?: return

		val library = fluidLibrary

		signing {
			sign(publishing.publications)
		}

		publishing {
			repositories {
				maven {
					setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
					credentials {
						username = sonatypeUserName
						password = sonatypePassword
					}
				}
			}

			publications
				.filterIsInstance<MavenPublication>()
				.forEach { publication ->
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


	private fun configureProject(): Unit = project.run {
		configureBasics()

		if (this@FluidJvmLibraryVariantConfiguration.publishing)
			configurePublishing()
	}


	private fun Project.configurePublishing() {
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
			publications {
				create<MavenPublication>("default") {
					from(components["java"])
					artifact(javadocJar)
					artifact(sourcesJar)
				}
			}
		}

		configureBintrayPublishing()
		configureSonatypePublishing()
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
	}
}
