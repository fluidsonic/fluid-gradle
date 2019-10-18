import com.github.benmanes.gradle.versions.updates.*
import com.jfrog.bintray.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

description = "Gradle plugin used to simplify configuration of all com.github.fluidsonic.* Kotlin libraries"
group = "com.github.fluidsonic.fluid-library"
version = "0.9.33"

plugins {
	`java-gradle-plugin`
	kotlin("jvm") version "1.3.50"
	`kotlin-dsl`
	`maven-publish`
	id("com.github.ben-manes.versions") version "0.27.0"
	id("com.gradle.plugin-publish") version "0.10.1"
	id("com.jfrog.bintray") version "1.8.4"
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	implementation("com.github.ben-manes:gradle-versions-plugin:0.27.0")
}

gradlePlugin {
	plugins {
		register("com.github.fluidsonic.fluid-library.plugin") {
			displayName = "fluid-* library configuration"
			description = "Very optionated plugin to unify & simplify configuration of all the com.github.fluidsonic.* Kotlin libraries."
			id = "com.github.fluidsonic.fluid-library"
			implementationClass = "com.github.fluidsonic.fluid.library.FluidLibraryPlugin"
		}
	}
}

kotlinDslPluginOptions {
	experimentalWarning.set(false)
}

pluginBundle {
	tags = listOf("project-configuration")
	website = "https://github.com/fluidsonic/fluid-library"
	vcsUrl = "https://github.com/fluidsonic/fluid-library.git"
}

repositories {
	bintray("kotlin/kotlin-eap")
	jcenter()
	mavenCentral()
}

sourceSets {
	getByName("main") {
		kotlin.srcDirs(listOf("sources"))
	}
}

tasks.withType<Wrapper> {
	distributionType = Wrapper.DistributionType.ALL
	gradleVersion = "5.6.3"
}

dependencyUpdates {
	outputFormatter = null

	rejectVersionIf {
		isUnstableVersion(candidate.version) && !isUnstableVersion(currentVersion)
	}
}


val bintrayUser = findProperty("bintrayUser") as String?
val bintrayKey = findProperty("bintrayApiKey") as String?
if (bintrayUser != null && bintrayKey != null) {
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

	configure<PublishingExtension> {
		publications {
			create<MavenPublication>("default") {
				artifactId = "com.github.fluidsonic.fluid-library.gradle.plugin"

				from(components["java"])
				artifact(sourcesJar)
			}
		}
	}

	configure<BintrayExtension> {
		user = bintrayUser
		key = bintrayKey

		setPublications("default")

		pkg.apply {
			repo = "maven"
			issueTrackerUrl = "https://github.com/fluidsonic/${project.name}/issues"
			name = project.name
			publicDownloadNumbers = true
			publish = true
			vcsUrl = "https://github.com/fluidsonic/${project.name}"
			websiteUrl = "https://github.com/fluidsonic/${project.name}"
			setLicenses("Apache-2.0")

			version.apply {
				name = project.version.toString()
				vcsTag = project.version.toString()
			}
		}
	}
}


fun RepositoryHandler.bintray(name: String) =
	maven("https://dl.bintray.com/$name")


val SourceSet.kotlin
	get() = withConvention(KotlinSourceSet::class) { kotlin }


fun dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit) =
	tasks.withType(configuration)


fun isUnstableVersion(version: String) =
	Regex("\\b(alpha|beta|eap|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE).containsMatchIn(version)
