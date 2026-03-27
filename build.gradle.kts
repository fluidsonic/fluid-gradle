import com.github.benmanes.gradle.versions.updates.*
import org.jetbrains.kotlin.gradle.dsl.*

description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
group = "io.fluidsonic.gradle"
version = "3.0.0"

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
	`maven-publish`
	signing
	alias(libs.plugins.gradle.nexus.publish)
	alias(libs.plugins.gradle.plugin.publish)
	alias(libs.plugins.gradle.versions)
	alias(libs.plugins.kotlin.jvm)
}

dependencies {
	implementation(platform(libs.kotlin.bom))
	implementation(libs.dokka.gradle.plugin)
	implementation(libs.gradle.nexus.publish.plugin)
	implementation(libs.gradle.versions.plugin)
	implementation(libs.kotlin.gradle.plugin)
	implementation(libs.kotlin.serialization.plugin)
	implementation(libs.ksp.gradle.plugin)

	testImplementation(kotlin("test"))
	testImplementation(kotlin("test-junit5"))
	testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.3")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.3")
	testImplementation(gradleTestKit())
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
		apiVersion.set(KotlinVersion.KOTLIN_2_3)
		languageVersion.set(KotlinVersion.KOTLIN_2_3)
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
	getByName("test") {
		kotlin.srcDirs(listOf("tests"))
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<Wrapper> {
	distributionType = Wrapper.DistributionType.ALL
	gradleVersion = "9.4.1"
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
