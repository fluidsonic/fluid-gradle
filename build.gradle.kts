import com.github.benmanes.gradle.versions.updates.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*

description = "Gradle plugin for simplifying the configuration of io.fluidsonic.* Kotlin libraries"
group = "io.fluidsonic.gradle"
version = "1.0.7"

plugins {
	`java-gradle-plugin`
	kotlin("jvm") version "1.3.61"
	`kotlin-dsl`
	`maven-publish`
	signing
	id("com.github.ben-manes.versions") version "0.27.0"
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("gradle-plugin"))
	implementation(kotlin("serialization"))
	implementation("com.github.ben-manes:gradle-versions-plugin:0.27.0")
}

gradlePlugin {
	plugins {
		register("io.fluidsonic.gradle") {
			displayName = "fluidsonic library gradle configurator"
			description = "Optionated plugin to unify & simplify configuration of all the io.fluidsonic.* Kotlin libraries."
			id = "io.fluidsonic.gradle"
			implementationClass = "io.fluidsonic.gradle.FluidsonicPlugin"
		}
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_7
	targetCompatibility = JavaVersion.VERSION_1_7
}

kotlinDslPluginOptions {
	experimentalWarning.set(false)
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

tasks {
	withType<KotlinCompile> {
		sourceCompatibility = "1.7"
		targetCompatibility = "1.7"

		kotlinOptions.jvmTarget = "1.6"
	}

	withType<Wrapper> {
		distributionType = Wrapper.DistributionType.ALL
		gradleVersion = "6.1.1"
	}
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
	// https://github.com/gradle/gradle/issues/11412#issuecomment-555413327
	System.setProperty("org.gradle.internal.publish.checksums.insecure", "true")

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
				setUrl("https://api.bintray.com/maven/fluidsonic/kotlin/gradle/;publish=1")
				credentials {
					username = bintrayUser
					password = bintrayKey
				}
			}
		}

		publications {
			create<MavenPublication>("pluginMaven") {
				artifact(javadocJar)
				artifact(sourcesJar)
			}

			filterIsInstance<MavenPublication>().forEach { publication ->
				publication.pom {
					name.set(project.name)
					description.set(project.description)
					packaging = "jar"
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
}


fun RepositoryHandler.bintray(name: String) =
	maven("https://dl.bintray.com/$name")


val SourceSet.kotlin
	get() = withConvention(KotlinSourceSet::class) { kotlin }


fun dependencyUpdates(configuration: DependencyUpdatesTask.() -> Unit) =
	tasks.withType(configuration)


fun isUnstableVersion(version: String) =
	Regex("\\b(alpha|beta|eap|rc|snapshot)\\d*\\b", RegexOption.IGNORE_CASE).containsMatchIn(version)
