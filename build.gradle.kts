import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.fluidsonic.gradle.buildsupport.PublishingMetadata
import io.fluidsonic.gradle.buildsupport.fetchGradleDistributionSha256Sum
import io.fluidsonic.gradle.buildsupport.isUnstableVersion
import io.fluidsonic.gradle.buildsupport.retryGithubSnapshotPublications
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
group = "io.fluidsonic.gradle"
version = "4.2.0"

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
	`maven-publish`
	signing
	alias(libs.plugins.detekt)
	alias(libs.plugins.gradle.nexus.publish)
	alias(libs.plugins.gradle.plugin.publish)
	alias(libs.plugins.gradle.versions)
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.ktlint)
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
	testImplementation(libs.junit.jupiter.api)
	testRuntimeOnly(libs.junit.jupiter.engine)
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
		apiVersion.set(KotlinVersion.KOTLIN_2_4)
		languageVersion.set(KotlinVersion.KOTLIN_2_4)
	}
	explicitApi()
	jvmToolchain(21)
}

detekt {
	buildUponDefaultConfig = true
	config.setFrom(files("config/detekt/detekt.yml"))
	source.setFrom(files("sources", "tests"))
}

ktlint {
	// ktlint-gradle bundles a default ktlint-cli version; the catalog pins it so `dependencyUpdates` and Dependabot see it.
	version.set(libs.versions.ktlint.cli)
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

	// The functional tests each drive a nested Gradle build through TestKit. Running them concurrently overlaps the nested
	// builds; the cap keeps the number of TestKit daemons small enough that each is reused across builds — one daemon per
	// test instead thrashes the machine with cold JVM start-ups and takes longer than running serially.
	systemProperty("junit.jupiter.execution.parallel.enabled", "true")
	systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
	systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
	systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "4")

	// TestKit's default directory is the test JVM's temp dir under `build/`, so `clean` throws away the nested builds'
	// Gradle user home — including their ~570 MB dependency cache, which every clean gate then downloads again. Keep it
	// next to the real Gradle user home instead (see `FunctionalTestSupport.kt`).
	systemProperty("io.fluidsonic.gradle.testKitDirectory", gradle.gradleUserHomeDir.resolve("testkit/io.fluidsonic.gradle").absolutePath)
}

tasks.withType<Wrapper> {
	distributionType = Wrapper.DistributionType.ALL
	gradleVersion = "9.7.1"

	// Fetch and pin the distribution checksum at execution time so a Gradle version bump does not require passing
	// `--gradle-distribution-sha256-sum` by hand. Logged at lifecycle level for cross-checking against
	// https://gradle.org/release-checksums/ before committing.
	doFirst {
		val checksumUrl = "$distributionUrl.sha256"
		val sum = fetchGradleDistributionSha256Sum(distributionUrl)

		logger.lifecycle("Fetched Gradle distribution SHA-256 sum from $checksumUrl: $sum")
		logger.lifecycle("Verify it against https://gradle.org/release-checksums/ before committing.")

		distributionSha256Sum = sum
	}
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
		packageGroup = PublishingMetadata.packageGroup

		repositories {
			sonatype {
				username = sonatypeUsername
				password = sonatypePassword

				nexusUrl.set(uri(PublishingMetadata.sonatypeNexusUrl))
				snapshotRepositoryUrl.set(uri(PublishingMetadata.sonatypeSnapshotRepositoryUrl))
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

	retryGithubSnapshotPublications()
}

if ((sonatypeUsername != null && sonatypePassword != null) || githubToken != null) {
	publishing.publications.withType<MavenPublication> {
		pom {
			name.set(project.name)
			description.set(project.description)
			url.set(PublishingMetadata.projectUrl(project.name))
			developers {
				developer {
					id.set(PublishingMetadata.developerId)
					name.set(PublishingMetadata.developerName)
					email.set(PublishingMetadata.developerEmail)
				}
			}
			licenses {
				license {
					name.set(PublishingMetadata.licenseName)
					url.set(PublishingMetadata.licenseUrl(project.name))
				}
			}
			scm {
				connection.set(PublishingMetadata.scmConnection(project.name))
				developerConnection.set(PublishingMetadata.scmDeveloperConnection(project.name))
				url.set(PublishingMetadata.projectUrl(project.name))
			}
		}
	}

	signing {
		sign(publishing.publications)
	}
}

fun dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit) = tasks.withType(configuration)
