import com.github.benmanes.gradle.versions.updates.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*

description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
group = "io.fluidsonic.gradle"
version = "1.1.26"

plugins {
	`java-gradle-plugin`
	kotlin("jvm") version "1.6.20"
	`kotlin-dsl`
	`maven-publish`
	signing
	id("com.github.ben-manes.versions") version "0.42.0"
	id("com.gradle.plugin-publish") version "0.21.0"
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
	implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.6.10")
}

gradlePlugin {
	plugins {
		register("io.fluidsonic.gradle") {
			displayName = "fluidsonic library gradle configurator"
			description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
			id = "io.fluidsonic.gradle"
			implementationClass = "io.fluidsonic.gradle.LibraryPlugin"
		}
	}
}

pluginBundle {
	website = "https://github.com/fluidsonic/fluid-gradle"
	vcsUrl = "https://github.com/fluidsonic/fluid-gradle.git"
	description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
	tags = listOf("fluid-libraries")

	plugins {
		named("io.fluidsonic.gradle") {
			displayName = "fluidsonic library gradle configurator"
		}
	}
}

kotlin {
	explicitApi()

	// https://kotlinlang.slack.com/archives/C19FD9681/p1649021339757969
	target.compilations.all {
		languageSettings.apply {
			apiVersion = "1.6"
			languageVersion = "1.6"
		}
		kotlinOptions {
			apiVersion = "1.6"
			jvmTarget = "17"
			languageVersion = "1.6"
		}
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

sourceSets {
	getByName("main") {
		kotlin.srcDirs(listOf("sources"))
	}
}

tasks {
	// https://kotlinlang.slack.com/archives/C19FD9681/p1649021339757969
	withType<KotlinCompile> {
		sourceCompatibility = "17"
		targetCompatibility = "17"

		kotlinOptions {
			apiVersion = "1.6"
			jvmTarget = "17"
			languageVersion = "1.6"
		}
	}

	withType<Wrapper> {
		distributionType = Wrapper.DistributionType.ALL
		gradleVersion = "7.4.2"
	}
}

dependencyUpdates {
	gradleReleaseChannel = "current"
	outputFormatter = null

	rejectVersionIf {
		isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
	}
}


val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
if (ossrhUsername != null && ossrhPassword != null) {
	val javadocJar by tasks.creating(Jar::class) {
		archiveClassifier.set("javadoc")
		from(tasks["javadoc"])
	}

	val sourcesJar by tasks.creating(Jar::class) {
		archiveClassifier.set("sources")
		from(sourceSets["main"].allSource)
	}

	artifacts {
		archives(javadocJar)
		archives(sourcesJar)
	}

	publishing {
		repositories {
			maven {
				setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
				credentials {
					username = ossrhUsername
					password = ossrhPassword
				}
			}
		}

		publications {
			create<MavenPublication>("pluginMaven") {
				artifact(javadocJar)
				artifact(sourcesJar)
			}

			withType<MavenPublication> {
				pom {
					name.set(project.name)
					description.set(project.description)
					url.set("https://github.com/fluidsonic/${project.name}")
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
							url.set("https://github.com/fluidsonic/${project.name}/blob/master/LICENSE")
						}
					}
					scm {
						connection.set("scm:git:https://github.com/fluidsonic/${project.name}.git")
						developerConnection.set("scm:git:git@github.com:fluidsonic/${project.name}.git")
						url.set("https://github.com/fluidsonic/${project.name}")
					}
				}
			}
		}
	}

	signing {
		sign(publishing.publications)
	}
}


val SourceSet.kotlin
	get() = withConvention(KotlinSourceSet::class) { kotlin }


fun dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit) =
	tasks.withType(configuration)


fun isUnstableVersion(version: String) =
	Regex("\\b(alpha|beta|dev|eap|m|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE).containsMatchIn(version)
