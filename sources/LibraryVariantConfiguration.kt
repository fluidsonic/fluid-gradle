package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.attributes.java.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.api.tasks.bundling.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.*
import org.jetbrains.kotlinx.serialization.gradle.*


class LibraryVariantConfiguration internal constructor(
	private val project: Project
) {

	var enforcesSameVersionForAllKotlinDependencies = true
	var publishing = true
	var usesNewInference = true

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


	private fun configureBasics(): Unit = project.run {
		val library = fluidLibrary

		apply<KotlinMultiplatformPluginWrapper>()
		apply<SerializationGradleSubplugin>()

		group = "io.fluidsonic.${library.name}"
		version = library.version

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
						freeCompilerArgs = listOfNotNull(
							"-Xopt-in=kotlin.ExperimentalUnsignedTypes",
							"-Xopt-in=kotlin.RequiresOptIn",
							"-Xopt-in=kotlin.contracts.ExperimentalContracts",
							"-Xopt-in=kotlin.experimental.ExperimentalTypeInference",
							"-XXLanguage:+InlineClasses",
							if (usesNewInference) "-Xnew-inference" else null
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

		test {
			testLogging {
				events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
				exceptionFormat = TestExceptionFormat.FULL
				showExceptions = true
				showStandardStreams = true
			}
		}

		repositories {
			mavenCentral()
			jcenter()
			bintray("fluidsonic/kotlin")
			bintray("kotlin/kotlin-eap")
			bintray("kotlin/kotlinx")
		}
	}


	private fun Project.configureJvmTargets() {
		kotlin {
			for ((jdk, configurations) in jvmTargets) {
				jvm(jdk.kotlinTargetName) {
					compilations.forEach { compilation ->
						compilation.kotlinOptions {
							freeCompilerArgs = listOfNotNull(
								"-Xopt-in=kotlin.ExperimentalUnsignedTypes",
								"-Xopt-in=kotlin.RequiresOptIn",
								"-Xopt-in=kotlin.contracts.ExperimentalContracts",
								"-Xopt-in=kotlin.experimental.ExperimentalTypeInference",
								"-XXLanguage:+InlineClasses",
								if (usesNewInference) "-Xnew-inference" else null
							)

							jvmTarget = jdk.kotlinJvmTargetVersion
						}
					}

					compilations.named("main") {
						attributes {
							attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jdk.jvmVersionCode)
						}

						defaultSourceSet {
							kotlin.setSrcDirs(listOf("sources/${jdk.kotlinSourceDirectoryName}"))
							resources.setSrcDirs(emptyList<Any>())

							dependencies {
								api(kotlin("stdlib-${jdk.kotlinStdlibVariant}"))
							}
						}
					}

					compilations.named("test") {
						@Suppress("NAME_SHADOWING")
						val jdk = jdk.coerceAtLeast(JvmTarget.jdk8)

						attributes {
							attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jdk.jvmVersionCode)
						}

						kotlinOptions {
							jvmTarget = jdk.kotlinJvmTargetVersion
						}

						defaultSourceSet {
							kotlin.setSrcDirs(listOf("sources/${jdk.kotlinSourceDirectoryName}-test"))
							resources.setSrcDirs(emptyList<Any>())

							dependencies {
								implementation(kotlin("test-junit5"))
								implementation("org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}")

								runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
								runtimeOnly("org.junit.platform:junit-platform-runner:${Versions.junitPlatform}")
							}
						}
					}

					for (configuration in configurations)
						JvmTargetConfigurator.applyTo(this, configuration)
				}
			}
		}

		tasks.withType<KotlinJvmTest> {
			useJUnitPlatform {
				includeEngines("junit-jupiter")
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

				target.compilations.forEach { compilation ->
					compilation.kotlinOptions {
						freeCompilerArgs = listOfNotNull(
							"-Xopt-in=kotlin.ExperimentalUnsignedTypes",
							"-Xopt-in=kotlin.RequiresOptIn",
							"-Xopt-in=kotlin.contracts.ExperimentalContracts",
							"-Xopt-in=kotlin.experimental.ExperimentalTypeInference",
							"-XXLanguage:+InlineClasses",
							if (usesNewInference) "-Xnew-inference" else null
						)
					}
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

						tasks.named<Task>("${target.name}Test") {
							dependsOn(binary.linkTask)
							group = JavaBasePlugin.VERIFICATION_GROUP

							doLast {
								if (binary.outputFile.exists()) {
									val outputPath = binary.outputFile.absolutePath
									exec {
										val device = findProperty("iosTest.device")?.toString() ?: "iPhone 11"

										println("$ xcrun simctl spawn --standalone \"$device\" \"$outputPath\"")
										commandLine("xcrun", "simctl", "spawn", "--standalone", device, outputPath)
									}
								}
							}
						}

						tasks.maybeCreate("iosTest").dependsOn("${target.name}Test")
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


	internal fun configureProject() {
		configureBasics()

		if (publishing)
			configurePublishing()
	}


	private fun configurePublishing(): Unit = project.run {
		val bintrayUser = findProperty("bintrayUser") as String? ?: return
		val bintrayKey = findProperty("bintrayApiKey") as String? ?: return
		val library = fluidLibrary

		apply<MavenPublishPlugin>()
		apply<SigningPlugin>()

		val emptyJar by tasks.creating(Jar::class)

		publishing {
			repositories {
				maven {
					setUrl("https://api.bintray.com/maven/fluidsonic/kotlin/${library.name}/;publish=1")
					credentials {
						username = bintrayUser
						password = bintrayKey
					}
				}
			}

			publications {
				getByName<MavenPublication>("kotlinMultiplatform") {
					artifact(emptyJar)
					artifact(emptyJar) {
						classifier = "javadoc"
					}
					artifact(emptyJar) {
						classifier = "sources"
					}
				}


				filterIsInstance<MavenPublication>().forEach { publication ->
					publication.pom {
						name.set(library.fullName)
						description.set(project.description)
						url.set("https://github.com/fluidsonic/${library.fullName}")
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
								url.set("https://github.com/fluidsonic/${library.fullName}/blob/master/LICENSE")
							}
						}
						scm {
							connection.set("scm:git:https://github.com/fluidsonic/${library.fullName}.git")
							developerConnection.set("scm:git:git@github.com:fluidsonic/${library.fullName}.git")
							url.set("https://github.com/fluidsonic/${library.fullName}")
						}
					}
				}
			}
		}

		signing {
			sign(publishing.publications)
		}

		afterEvaluate {
			publishing.publications
				.filterIsInstance<MavenPublication>()
				.filter { it.name != "kotlinMultiplatform" }
				.forEach { publication ->
					if (publication.artifacts.none { it.classifier.isNullOrEmpty() && it.extension == "jar" })
						publication.artifact(emptyJar)

					if (publication.artifacts.none { it.classifier == "javadoc" })
						publication.artifact(emptyJar) { classifier = "javadoc" }
				}
		}
	}


	var description
		get() = project.description
		set(value) {
			project.description = value
		}


	companion object {

		private val JvmTarget.kotlinJvmTargetVersion
			get() = when (this) {
				JvmTarget.jdk7 -> "1.6"
				JvmTarget.jdk8 -> "1.8"
			}


		private val JvmTarget.kotlinSourceDirectoryName
			get() = "jvm-$kotlinTargetName"


		private val JvmTarget.kotlinStdlibVariant
			get() = when (this) {
				JvmTarget.jdk7 -> "jdk7"
				JvmTarget.jdk8 -> "jdk8"
			}


		private val JvmTarget.kotlinTargetName
			get() = when (this) {
				JvmTarget.jdk7 -> "jdk7"
				JvmTarget.jdk8 -> "jdk8"
			}


		private val ObjcTarget.kotlinSourceDirectoryName
			get() = "objc-${name.toLowerCase()}"
	}
}


fun Project.fluidLibraryVariant(configure: LibraryVariantConfiguration.() -> Unit = {}) {
	LibraryVariantConfiguration(project = project).apply(configure).configureProject()
}
