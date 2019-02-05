import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

description = "Gradle plugin used to simplify configuration of all com.github.fluidsonic.* Kotlin libraries"
group = "com.github.fluidsonic.fluid-library"
version = "0.9.1"

plugins {
	`java-gradle-plugin`
	kotlin("jvm") version "1.3.20"
	`kotlin-dsl`
	`maven-publish`
	id("com.github.ben-manes.versions") version "0.20.0"
	id("com.gradle.plugin-publish") version "0.10.1"
	id("com.jfrog.bintray") version "1.8.4"
}

dependencies {
	implementation(kotlin("gradle-plugin"))

	implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
	implementation("com.github.ben-manes:gradle-versions-plugin:0.20.0")
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
	jcenter()
}

sourceSets {
	getByName("main") {
		kotlin.srcDirs(listOf("sources"))
	}
}

tasks.withType<Wrapper> {
	distributionType = Wrapper.DistributionType.ALL
	gradleVersion = "5.2"
}


val bintrayUser = findProperty("bintrayUser") as String?
val bintrayKey = findProperty("bintrayApiKey") as String??
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


val SourceSet.kotlin
	get() = withConvention(KotlinSourceSet::class) { kotlin }
