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
import org.jetbrains.kotlinx.serialization.gradle.*


class FluidLibraryVariantConfiguration private constructor(
	private val project: Project
) {

	var enforcesSameVersionForAllKotlinDependencies = true
	var publishing = true

	private var jdk: JDK? = null
	private var objcEnabled = false


	fun jvm(jdk: JDK) {
		check(this.jdk == null) { "Cannot call jvm(…) multiple times" }
		this.jdk = jdk
	}


	fun objc() {
		check(!objcEnabled) { "Cannot call objc() multiple times" }
		objcEnabled = true
	}


	private fun Project.configureBasics() {
		apply<KotlinMultiplatformPluginWrapper>()
		apply<SerializationGradleSubplugin>()

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
			metadata {
				compilations.forEach { compilation ->
					compilation.kotlinOptions {
						freeCompilerArgs = listOf(
							"-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
							"-XXLanguage:+InlineClasses"
						)
					}
				}
			}

			sourceSets {
				commonMain {
					kotlin.setSrcDirs(listOf("sources/common"))
					resources.setSrcDirs(emptyList<Any>())

					dependencies {
						api(kotlin("stdlib-common"))
					}
				}

				commonTest {
					kotlin.setSrcDirs(listOf("sources/commonTest"))
					resources.setSrcDirs(emptyList<Any>())

					dependencies {
						implementation(kotlin("test-common"))
						implementation(kotlin("test-annotations-common"))
					}
				}
			}
		}

		configureJvmTarget()
		configureObjcTargets()

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
			publish = true

			setPublications("default")

			pkg.apply {
				repo = "maven"
				issueTrackerUrl = "https://github.com/fluidsonic/${library.name}/issues"
				name = library.name
				publicDownloadNumbers = true
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


	private fun Project.configureJvmTarget() {
		val jdk = jdk ?: return

		kotlin {
			jvm {
				compilations.forEach { compilation ->
					compilation.kotlinOptions {
						freeCompilerArgs = listOf(
							"-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
							"-XXLanguage:+InlineClasses"
						)

						jvmTarget = jdk.toKotlinTarget()
					}
				}
			}

			sourceSets {
				jvmMain {
					kotlin.setSrcDirs(listOf("sources/jvm"))
					resources.setSrcDirs(emptyList<Any>())

					dependencies {
						api(kotlin("stdlib-${jdk.moduleId}"))
					}
				}

				jvmTest {
					kotlin.setSrcDirs(listOf("sources/jvmTest"))
					resources.setSrcDirs(emptyList<Any>())

					dependencies {
						implementation(kotlin("test-junit5"))
						implementation("org.junit.jupiter:junit-jupiter-api:5.5.0")

						runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0")
						runtimeOnly("org.junit.platform:junit-platform-runner:1.5.0")
					}
				}
			}
		}

		java {
			sourceCompatibility = jdk.toGradle()
			targetCompatibility = jdk.toGradle()
		}
	}


	private fun Project.configureObjcTargets() {
		if (!objcEnabled) return

		kotlin {
			val iosTest = tasks.create("iosTest")
			val macosTest = tasks.create("macosTest")
			val objcTest by tasks.creating {
				dependsOn(iosTest)
				dependsOn(macosTest)
			}
			tasks.named("check") {
				dependsOn(objcTest)
			}

			val objcMainSourceSet = sourceSets.create("objcMain") {
				kotlin.setSrcDirs(emptyList<Any>())
				resources.setSrcDirs(emptyList<Any>())
			}

			val objcTestSourceSet = sourceSets.create("objcTest") {
				kotlin.setSrcDirs(emptyList<Any>())
				resources.setSrcDirs(emptyList<Any>())
			}

			listOf(
				iosArm64(),
				iosX64(),
				macosX64()
			).forEach { target ->
				target.compilations.forEach { compilation ->
					when (compilation.compilationName) {
						"main" ->
							compilation.defaultSourceSet {
								kotlin.setSrcDirs(listOf("sources/${target.name}"))
								resources.setSrcDirs(emptyList<Any>())

								dependsOn(objcMainSourceSet)
							}

						"test" ->
							compilation.defaultSourceSet {
								kotlin.setSrcDirs(listOf("sources/$name"))
								resources.setSrcDirs(emptyList<Any>())

								dependsOn(objcTestSourceSet)
							}
					}

					compilation.kotlinOptions.freeCompilerArgs = listOf(
						"-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
						"-XXLanguage:+InlineClasses"
					)
				}

				when {
					target.name.startsWith("iosX") -> {
						val binary = target.binaries.first()

						tasks.create<Task>("${target.name}Test") {
							iosTest.dependsOn(this)

							dependsOn(binary.linkTask)
							group = JavaBasePlugin.VERIFICATION_GROUP

							doLast {
								if (binary.outputFile.exists()) {
									val outputPath = binary.outputFile.absolutePath
									exec {
										val device = findProperty("iosTest.device")?.toString() ?: "iPhone X"

										println("$ xcrun simctl spawn \"$device\" \"$outputPath\"")
										commandLine("xcrun", "simctl", "spawn", device, outputPath)
									}
								}
							}
						}
					}

					target.name.startsWith("macos") ->
						macosTest.dependsOn("${target.name}Test")
				}
			}
		}
	}


	private fun Project.configureSonatypePublishing() {
		val sonatypeUserName = findProperty("sonatypeUserName") as String? ?: return
		val sonatypePassword = findProperty("sonatypePassword") as String? ?: return

		val library = fluidLibrary
		val emptyJar by tasks.creating(Jar::class)

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

			publications.getByName<MavenPublication>("kotlinMultiplatform") {
				artifact(emptyJar)
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

		afterEvaluate {
			publishing.publications
				.filterIsInstance<MavenPublication>()
				.filter { it.name != "kotlinMultiplatform" }
				.filter { it.artifacts.none { artifact -> artifact.classifier == "javadoc" } }
				.forEach { it.artifact(emptyJar) { classifier = "javadoc" } }
		}
	}


	private fun configureProject(): Unit = project.run {
		configureBasics()

		if (this@FluidLibraryVariantConfiguration.publishing)
			configurePublishing()
	}


	private fun Project.configurePublishing() {
		apply<MavenPublishPlugin>()
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
