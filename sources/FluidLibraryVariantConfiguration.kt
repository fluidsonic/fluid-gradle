package com.github.fluidsonic.fluid.library

import com.jfrog.bintray.gradle.*
import com.jfrog.bintray.gradle.tasks.*
import org.gradle.api.*
import org.gradle.api.attributes.java.*
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

	private val commonConfigurations: MutableList<CommonTargetConfigurator.() -> Unit> = mutableListOf()
	private val jvmTargets: MutableMap<JvmTarget, MutableList<JvmTargetConfigurator.() -> Unit>> = mutableMapOf()
	private val objcTargets: MutableMap<ObjcTarget, MutableList<ObjcTargetConfigurator.() -> Unit>> = mutableMapOf()


	fun common(configure: CommonTargetConfigurator.() -> Unit = {}) {
		commonConfigurations += configure
	}


	fun jvm(target: JvmTarget, configure: JvmTargetConfigurator.() -> Unit = {}) {
		jvmTargets.getOrPut(target) { mutableListOf() } += configure
	}


	fun objc(target: ObjcTarget, configure: ObjcTargetConfigurator.() -> Unit = {}) {
		objcTargets.getOrPut(target) { mutableListOf() } += configure
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
				named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
					kotlin.setSrcDirs(listOf("sources/common"))
					resources.setSrcDirs(emptyList<Any>())

					dependencies {
						api(kotlin("stdlib-common"))
					}
				}

				named(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME) {
					kotlin.setSrcDirs(listOf("sources/common-test"))
					resources.setSrcDirs(emptyList<Any>())

					dependencies {
						implementation(kotlin("test-common"))
						implementation(kotlin("test-annotations-common"))
					}
				}

				for (configuration in commonConfigurations)
					CommonTargetConfigurator.applyTo(this, configuration)
			}
		}

		configureJvmTargets()
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


	private fun Project.configureJvmTargets() {
		kotlin {
			for ((jdk, configurations) in jvmTargets) {
				jvm(jdk.kotlinTargetName) {
					compilations.forEach { compilation ->
						compilation.kotlinOptions {
							freeCompilerArgs = listOf(
								"-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
								"-XXLanguage:+InlineClasses"
							)

							jvmTarget = jdk.kotlinJvmTargetVersion
						}
					}

					compilations["main"].defaultSourceSet {
						kotlin.setSrcDirs(listOf("sources/${jdk.kotlinSourceDirectoryName}"))
						resources.setSrcDirs(emptyList<Any>())

						dependencies {
							api(kotlin("stdlib-${jdk.kotlinStdlibVariant}"))
						}
					}

					compilations["test"].defaultSourceSet {
						kotlin.setSrcDirs(listOf("sources/${jdk.kotlinSourceDirectoryName}-test"))
						resources.setSrcDirs(emptyList<Any>())

						dependencies {
							implementation(kotlin("test-junit5"))
							implementation("org.junit.jupiter:junit-jupiter-api:5.5.2")

							runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
							runtimeOnly("org.junit.platform:junit-platform-runner:1.5.2")
						}
					}

					attributes {
						attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jdk.jvmVersionCode)
					}

					for (configuration in configurations)
						JvmTargetConfigurator.applyTo(this, configuration)
				}
			}
		}
	}


	private fun Project.configureObjcTargets() {
		if (objcTargets.isEmpty()) return

		kotlin {
			for ((objc, configurations) in objcTargets) {
				val target = when (objc) {
					ObjcTarget.iosArm64 -> iosArm64()
					ObjcTarget.iosX64 -> iosX64()
					ObjcTarget.macosX64 -> macosX64()
				}

				target.compilations["main"].defaultSourceSet {
					kotlin.setSrcDirs(listOf("sources/${objc.kotlinSourceDirectoryName}"))
					resources.setSrcDirs(emptyList<Any>())
				}

				target.compilations["test"].defaultSourceSet {
					kotlin.setSrcDirs(listOf("sources/${objc.kotlinSourceDirectoryName}-test"))
					resources.setSrcDirs(emptyList<Any>())
				}

				when {
					target.name.startsWith("iosX") -> {
						val binary = target.binaries.first()

						tasks.create<Task>("${target.name}Test") {
							tasks.maybeCreate("iosTest").dependsOn(this)

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
						tasks.maybeCreate("macosTest").dependsOn("${target.name}Test")
				}

				for (configuration in configurations)
					ObjcTargetConfigurator.applyTo(target, configuration)
			}
		}

		val objcTest by tasks.creating {
			tasks.findByName("iosTest")?.let { dependsOn(it) }
			tasks.findByName("macosTest")?.let { dependsOn(it) }
		}

		tasks.named("check") {
			dependsOn(objcTest)
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


		val JvmTarget.jvmVersionCode
			get() = when (this) {
				JvmTarget.jdk7 -> 7
				JvmTarget.jdk8 -> 8
			}


		val JvmTarget.kotlinJvmTargetVersion
			get() = when (this) {
				JvmTarget.jdk7 -> "1.6"
				JvmTarget.jdk8 -> "1.8"
			}


		val JvmTarget.kotlinSourceDirectoryName
			get() = "jvm-$kotlinTargetName"


		val JvmTarget.kotlinStdlibVariant
			get() = when (this) {
				JvmTarget.jdk7 -> "jdk7"
				JvmTarget.jdk8 -> "jdk8"
			}


		val JvmTarget.kotlinTargetName
			get() = when (this) {
				JvmTarget.jdk7 -> "jdk7"
				JvmTarget.jdk8 -> "jdk8"
			}


		val ObjcTarget.kotlinSourceDirectoryName
			get() = "objc-${name.toLowerCase()}"
	}
}
