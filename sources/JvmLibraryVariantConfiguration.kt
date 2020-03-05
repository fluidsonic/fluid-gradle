package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.api.tasks.bundling.*
import org.gradle.api.tasks.compile.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.gradle.testing.base.plugins.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*


class JvmLibraryVariantConfiguration internal constructor(
	var target: JvmTarget,
	private val project: Project
) {

	var enforcesSameVersionForAllKotlinDependencies = true
	var publishing = true
	var usesNewInference = true


	private fun configureBasics(): Unit = project.run {
		val library = fluidLibrary
		val testTarget = target.coerceAtLeast(JvmTarget.jdk8)

		apply<KotlinPlatformJvmPlugin>()
		apply<JavaLibraryPlugin>()
		apply<TestingBasePlugin>()

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

		dependencies {
			api(platform(kotlin("bom")))
			api(kotlin("stdlib-${target.kotlinStdlibVariant}"))

			testImplementation(kotlin("test-junit5"))
			testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}")

			testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
			testRuntimeOnly("org.junit.platform:junit-platform-runner:${Versions.junitPlatform}")
		}

		java {
			sourceCompatibility = target.gradleJavaVersion
			targetCompatibility = target.gradleJavaVersion
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
			withType<JavaCompile> {
				sourceCompatibility = target.gradleJavaVersion.toString()
				targetCompatibility = target.gradleJavaVersion.toString()
			}

			withType<KotlinCompile> {
				sourceCompatibility = target.toString()
				targetCompatibility = target.toString()

				kotlinOptions.freeCompilerArgs = listOfNotNull(
					"-Xopt-in=kotlin.ExperimentalUnsignedTypes",
					"-Xopt-in=kotlin.RequiresOptIn",
					"-Xopt-in=kotlin.contracts.ExperimentalContracts",
					"-Xopt-in=kotlin.experimental.ExperimentalTypeInference",
					"-XXLanguage:+InlineClasses",
					if (usesNewInference) "-Xnew-inference" else null
				)
				kotlinOptions.jvmTarget = target.kotlinJvmTargetVersion
			}

			named<JavaCompile>("compileTestJava") {
				sourceCompatibility = testTarget.gradleJavaVersion.toString()
				targetCompatibility = testTarget.gradleJavaVersion.toString()
			}

			named<KotlinCompile>("compileTestKotlin") {
				sourceCompatibility = testTarget.toString()
				targetCompatibility = testTarget.toString()

				kotlinOptions.jvmTarget = testTarget.kotlinJvmTargetVersion
			}
		}

		test {
			useJUnitPlatform {
				includeEngines("junit-jupiter")
			}

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
					setUrl("https://api.bintray.com/maven/fluidsonic/kotlin/${library.name}/;publish=1")
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
						name.set(library.fullName)
						description.set(project.description)
						packaging = "jar"
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
	}


	var description
		get() = project.description
		set(value) {
			project.description = value
		}


	companion object {

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


fun Project.fluidJvmLibraryVariant(target: JvmTarget, configure: JvmLibraryVariantConfiguration.() -> Unit = {}) {
	JvmLibraryVariantConfiguration(target = target, project = project).apply(configure).configureProject()
}
