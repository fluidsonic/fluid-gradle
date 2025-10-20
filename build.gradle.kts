import com.github.benmanes.gradle.versions.updates.*
import org.jetbrains.kotlin.gradle.dsl.*

description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
group = "io.fluidsonic.gradle"
version = "2.0.2"

plugins {
	`java-gradle-plugin`
	kotlin("jvm") version "2.2.20"
	`kotlin-dsl`
	`maven-publish`
	signing
	id("com.github.ben-manes.versions") version "0.53.0"
	id("com.gradle.plugin-publish") version "2.0.0"
	id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

dependencies {
	implementation(platform(kotlin("bom", "2.2.20")))
	implementation(kotlin("gradle-plugin", "2.2.20"))
	implementation(kotlin("serialization", "2.2.20"))
	implementation("com.github.ben-manes:gradle-versions-plugin:0.53.0")
	implementation("io.github.gradle-nexus:publish-plugin:2.0.0")
	implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.0.0")
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
	compilerOptions {
		apiVersion.set(KotlinVersion.KOTLIN_2_2)
		languageVersion.set(KotlinVersion.KOTLIN_2_2)
	}
	explicitApi()
	jvmToolchain(21)
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

tasks.withType<Wrapper> {
	distributionType = Wrapper.DistributionType.ALL
	gradleVersion = "9.1.0"
}

dependencyUpdates {
	gradleReleaseChannel = "current"

	rejectVersionIf {
		isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
	}
}

val sonatypeUsername: String? = System.getenv("SONATYPE_USERNAME")
val sonatypePassword: String? = System.getenv("SONATYPE_PASSWORD")
if (sonatypeUsername != null && sonatypePassword != null) {
	nexusPublishing {
		packageGroup = "io.fluidsonic"

		repositories {
			sonatype {
				username = sonatypeUsername
				password = sonatypePassword

				nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
				snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
			}
		}
	}
}

val githubToken: String? = System.getenv("GITHUB_PACKAGES_AUTH_TOKEN") ?: findProperty("GITHUB_PACKAGES_AUTH_TOKEN") as String?
if (githubToken != null) {
	publishing {
		repositories {
			maven {
				name = "github"
				url = uri("https://maven.pkg.github.com/fluidsonic/fluid-gradle")

				credentials {
					username = "unused"
					password = githubToken
				}
			}
		}
	}
}

if ((sonatypeUsername != null && sonatypePassword != null) || githubToken != null) {
	publishing.publications.withType<MavenPublication> {
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

	signing {
		sign(publishing.publications)
	}
}


fun dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit) =
	tasks.withType(configuration)


fun isUnstableVersion(version: String) =
	Regex("\\b(alpha|beta|dev|eap|m|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE).containsMatchIn(version)
