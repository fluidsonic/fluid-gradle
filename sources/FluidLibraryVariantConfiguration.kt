package com.github.fluidsonic.fluid.library

import com.jfrog.bintray.gradle.*
import com.jfrog.bintray.gradle.tasks.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.internal.artifact.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.api.publish.plugins.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*


class FluidLibraryVariantConfiguration private constructor(
	private val project: Project
) {

	var enforcesSameVersionForAllKotlinDependencies = true
	var publishing = true
	var jdk = JDK.v1_7


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


	private fun Project.configureBasics() {
		apply<KotlinMultiplatformPluginWrapper>()

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

		kotlin {
			jvm()

			sourceSets {
				commonMain {
					kotlin.setSrcDirs(listOf("sources/common"))
					resources.setSrcDirs(emptyList())

					dependencies {
						api(kotlin("stdlib-common"))
					}
				}

				commonTest {
					kotlin.setSrcDirs(listOf("sources/commonTest"))
					resources.setSrcDirs(emptyList())

					dependencies {
						implementation(kotlin("test-common"))
						implementation(kotlin("test-annotations-common"))
					}
				}

				jvmMain {
					kotlin.setSrcDirs(listOf("sources/jvm"))
					resources.setSrcDirs(emptyList())

					dependencies {
						api(kotlin("stdlib-${jdk.moduleId}"))
					}
				}

				jvmTest {
					kotlin.setSrcDirs(listOf("sources/jvmTest"))
					resources.setSrcDirs(emptyList())

					dependencies {
						implementation(kotlin("test-junit5"))
						implementation("org.junit.jupiter:junit-jupiter-api:5.4.0")

						runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
						runtimeOnly("org.junit.platform:junit-platform-runner:1.4.0")
					}
				}
			}
		}

		java {
			sourceCompatibility = jdk.toGradle()
			targetCompatibility = jdk.toGradle()
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


	private fun Project.configureSonatypePublishing() {
		val sonatypeUserName = findProperty("sonatypeUserName") as String? ?: return
		val sonatypePassword = findProperty("sonatypePassword") as String? ?: return

		val library = fluidLibrary

		signing {
			sign(configurations.archives.get())
		}

		tasks.getByName<Upload>("uploadArchives") {
			repositories {
				withConvention(MavenRepositoryHandlerConvention::class) {
					mavenDeployer {
						beforeDeployment {
							signing.signPom(this)
						}

						withGroovyBuilder {
							"repository"("url" to "https://oss.sonatype.org/service/local/staging/deploy/maven2") {
								"authentication"("userName" to sonatypeUserName, "password" to sonatypePassword)
							}

							"snapshotRepository"("url" to "https://oss.sonatype.org/content/repositories/snapshots") {
								"authentication"("userName" to sonatypeUserName, "password" to sonatypePassword)
							}
						}

						pom.project {
							withGroovyBuilder {
								"name"(project.name)
								"description"(project.description)
								"packaging"("jar")
								"url"("https://github.com/fluidsonic/${library.name}")
								"developers" {
									"developer" {
										"id"("fluidsonic")
										"name"("Marc Knaup")
										"email"("marc@knaup.io")
									}
								}
								"licenses" {
									"license" {
										"name"("Apache License 2.0")
										"url"("https://github.com/fluidsonic/${library.name}/blob/master/LICENSE")
									}
								}
								"scm" {
									"connection"("scm:git:https://github.com/fluidsonic/${library.name}.git")
									"developerConnection"("scm:git:git@github.com:fluidsonic/${library.name}.git")
									"url"("https://github.com/fluidsonic/${library.name}")
								}
							}
						}
					}
				}
			}
		}
	}


	private fun configureProject(): Unit = project.run {
		configureBasics()

		if (this@FluidLibraryVariantConfiguration.publishing)
			configurePublishing()
	}


	private fun Project.configurePublishing() {
		apply<MavenPlugin>()
		apply<MavenPublishPlugin>()
		apply<PublishingPlugin>()
		apply<SigningPlugin>()

		tasks.getByName<org.gradle.jvm.tasks.Jar>("jvmSourcesJar") {
			from(file("build/generated/source/kaptKotlin/main"))
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

		internal fun applyTo(project: Project, configure: FluidLibraryVariantConfiguration.() -> Unit = {}) {
			FluidLibraryVariantConfiguration(project = project).apply(configure).configureProject()
		}
	}
}
