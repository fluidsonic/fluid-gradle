import com.github.benmanes.gradle.versions.updates.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*

description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
group = "io.fluidsonic.gradle"
version = "1.2.2"

plugins {
	`java-gradle-plugin`
	kotlin("jvm") version "1.8.22"
	`kotlin-dsl`
	`maven-publish`
	signing
	id("com.github.ben-manes.versions") version "0.47.0"
	id("com.gradle.plugin-publish") version "1.2.0"
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	implementation("com.github.ben-manes:gradle-versions-plugin:0.47.0")
	implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
}

gradlePlugin {
	plugins {
		register("io.fluidsonic.gradle") {
			description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
			displayName = "fluidsonic library gradle configurator"
			id = "io.fluidsonic.gradle"
			implementationClass = "io.fluidsonic.gradle.LibraryPlugin"
			tags.set(listOf("fluid-libraries"))
			vcsUrl.set("https://github.com/fluidsonic/fluid-gradle.git")
			website.set("https://github.com/fluidsonic/fluid-gradle")
		}
	}
}

kotlin {
	explicitApi()

	target.compilations.all {
		kotlinOptions {
			apiVersion = "1.8"
			jvmTarget = "17"
			languageVersion = "1.8"
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
		kotlinOptions {
			apiVersion = "1.8"
			jvmTarget = "17"
			languageVersion = "1.8"
		}
	}

	withType<Wrapper> {
		distributionType = Wrapper.DistributionType.ALL
		gradleVersion = "8.1.1"
	}
}

dependencyUpdates {
	gradleReleaseChannel = "current"

	rejectVersionIf {
		isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
	}
}


val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
if (ossrhUsername != null && ossrhPassword != null) {
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
