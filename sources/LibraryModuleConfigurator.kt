package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.attributes.java.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.api.publish.maven.tasks.*
import org.gradle.api.tasks.bundling.*
import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.testing.*
import org.jetbrains.kotlinx.serialization.gradle.*


internal class LibraryModuleConfigurator(
	configuration: LibraryModuleConfiguration,
	private val project: Project
) {

	private val libraryConfiguration = project.fluidLibrary.configuration
	private val configuration = libraryConfiguration.defaultModuleConfiguration.mergeWith(
		other = configuration,
		addTargetsAutomatically = false
	)


	fun configure() {
		with(project) {
			configureBasics()

			if (configuration.isPublishingEnabled)
				configurePublishing()

			kotlin {
				configuration.customConfigurations.forEach { it() }
			}
		}
	}


	private fun AbstractTestTask.configureTestTask() {
		testLogging {
			events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
			exceptionFormat = TestExceptionFormat.FULL
			showExceptions = true
			showStandardStreams = true
		}
	}


	private fun KotlinMultiplatformExtension.configureCommonTarget() {
		val targetConfiguration = configuration.targets.common

		sourceSets {
			named(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME) {
				configureSourceSet(path = "common")

				dependencies {
					targetConfiguration.dependencyConfigurations.forEach { it() }
				}
			}

			named(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME) {
				configureSourceSet(path = "common")

				dependencies {
					implementation(kotlin("test-common"))
					implementation(kotlin("test-annotations-common"))

					targetConfiguration.testDependencyConfigurations.forEach { it() }
				}
			}
		}

		metadata {
			targetConfiguration.customConfigurations.forEach { it() }
		}
	}


	private fun KotlinMultiplatformExtension.configureJsTargets() {
		val targetConfiguration = configuration.targets.js ?: return

		js {
			compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
				defaultSourceSet {
					configureSourceSet(path = "js")

					dependencies {
						targetConfiguration.dependencyConfigurations.forEach { it() }
					}
				}
			}

			compilations.named(KotlinCompilation.TEST_COMPILATION_NAME) {
				defaultSourceSet {
					configureSourceSet(path = "js")

					dependencies {
						api(kotlin("test-js"))

						targetConfiguration.testDependencyConfigurations.forEach { it() }
					}
				}
			}

			if (!targetConfiguration.noBrowser)
				browser {
					testTask {
						configureTestTask()
					}
				}

			if (!targetConfiguration.noNodeJs)
				nodejs {
					testTask {
						configureTestTask()
					}
				}

			targetConfiguration.customConfigurations.forEach { it() }
		}
	}


	private fun KotlinMultiplatformExtension.configureJvmTargets() {
		configuration.targets.jvm?.let { targetConfiguration ->
			configureJvmTarget(
				jdkVersion = JdkVersion.v8,
				targetConfiguration = targetConfiguration,
				targetName = "jvm",
				path = "jvm"
			)
		}
		configuration.targets.jvmJdk7?.let { targetConfiguration ->
			configureJvmTarget(
				jdkVersion = JdkVersion.v7,
				targetConfiguration = targetConfiguration,
				targetName = "jvmJdk7",
				path = "jvm-jdk7"
			)
		}
	}


	private fun KotlinMultiplatformExtension.configureJvmTarget(
		jdkVersion: JdkVersion,
		targetConfiguration: LibraryModuleConfiguration.Targets.Jvm,
		targetName: String,
		path: String
	) {
		jvm(targetName) {
			if (targetConfiguration.includesJava)
				withJava()

			compilations.forEach { compilation ->
				compilation.kotlinOptions {
					jvmTarget = jdkVersion.kotlinJvmTargetValue
				}
			}

			compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
				attributes {
					attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jdkVersion.code)
				}

				defaultSourceSet {
					configureSourceSet(path = path)

					dependencies {
						targetConfiguration.dependencyConfigurations.forEach { it() }
					}
				}
			}

			compilations.named(KotlinCompilation.TEST_COMPILATION_NAME) {
				attributes {
					attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jdkVersion.code)
				}

				kotlinOptions {
					jvmTarget = jdkVersion.kotlinJvmTargetValue
				}

				defaultSourceSet {
					configureSourceSet(path = path)

					dependencies {
						if (jdkVersion >= JdkVersion.v8) {
							implementation(kotlin("test-junit5"))
							implementation("org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}")

							runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
							runtimeOnly("org.junit.platform:junit-platform-runner:${Versions.junitPlatform}")
						}
						else
							implementation(kotlin("test-junit"))

						targetConfiguration.testDependencyConfigurations.forEach { it() }
					}
				}
			}

			testRuns.all {
				executionTask {
					if (jdkVersion >= JdkVersion.v8)
						useJUnitPlatform {
							includeEngines("junit-jupiter")
						}

					configureTestTask()
				}
			}

			targetConfiguration.customConfigurations.forEach { it() }
		}
	}


	private fun KotlinMultiplatformExtension.configureNativeDarwinTargets() {
		val targetConfiguration = configuration.targets.nativeDarwin
			?.takeIf { !it.noIosArm64 || !it.noIosX64 || !it.noMacosX64 }
			?: return

		if (!targetConfiguration.noIosArm64)
			iosArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathName = "ios-arm64")
			}

		if (!targetConfiguration.noIosX64)
			iosX64 {
				configureTarget(targetConfiguration = targetConfiguration, pathName = "ios-x64")
			}

		if (!targetConfiguration.noMacosX64)
			macosX64 {
				configureTarget(targetConfiguration = targetConfiguration, pathName = "macos-x64")
			}

		sourceSets {
			val commonMain by getting
			val commonTest by getting

			val nativeDarwinMain by creating {
				dependsOn(commonMain)

				configureSourceSet(path = "native-darwin")
			}

			val nativeDarwinTest by creating {
				dependsOn(commonTest)

				configureSourceSet(path = "native-darwin")
			}

			if (!targetConfiguration.noIosArm64 || !targetConfiguration.noIosX64) {
				// TODO Enable once Commonizer is no longer limited to one level in the hierarchy.
				//      https://github.com/JetBrains/kotlin/blob/1.4-M2/native/commonizer/README.md
//				val iosMain by creating {
//					dependsOn(nativeDarwinMain)
//
//					kotlin.setSrcDirs(listOf("sources/ios"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}
//
//				val iosTest by creating {
//					dependsOn(nativeDarwinTest)
//
//					kotlin.setSrcDirs(listOf("tests/ios"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}

				if (!targetConfiguration.noIosArm64) {
					getByName("iosArm64Main") {
						dependsOn(nativeDarwinMain)
					}

					getByName("iosArm64Test") {
						dependsOn(nativeDarwinTest)
					}
				}

				if (!targetConfiguration.noIosX64) {
					getByName("iosX64Main") {
						dependsOn(nativeDarwinMain)
					}

					getByName("iosX64Test") {
						dependsOn(nativeDarwinTest)
					}
				}
			}

			if (!targetConfiguration.noMacosX64) {
				getByName("macosX64Main") {
					dependsOn(nativeDarwinMain)
				}

				getByName("macosX64Test") {
					dependsOn(nativeDarwinTest)
				}
			}
		}
	}


	private fun KotlinNativeTarget.configureTarget(
		targetConfiguration: LibraryModuleConfiguration.Targets.NativeDarwin,
		pathName: String
	) {
		compilations[KotlinCompilation.MAIN_COMPILATION_NAME].defaultSourceSet {
			configureSourceSet(path = pathName)
		}

		compilations[KotlinCompilation.TEST_COMPILATION_NAME].defaultSourceSet {
			configureSourceSet(path = pathName)
		}

		if (this is KotlinNativeTargetWithTests<*>)
			testRuns.all {
				this as KotlinTaskTestRun<*, *>

				executionTask {
					configureTestTask()
				}
			}

		targetConfiguration.customConfigurations.forEach { it() }
	}


	private fun KotlinSourceSet.configureSourceSet(path: String) {
		val firstLevelPath = if (name.endsWith("Test")) "tests" else "sources"

		kotlin.setSrcDirs(listOf("$firstLevelPath/$path"))
		resources.setSrcDirs(listOf("$firstLevelPath/$path-resources"))

		languageSettings.apply {
			configuration.language.experimentalApisToUse.forEach { useExperimentalAnnotation(it) }
			configuration.language.languageFeaturesToEnable.forEach { enableLanguageFeature(it) }
			configuration.language.customConfigurations.forEach { it() }

			if (!configuration.language.noNewInference)
				enableLanguageFeature("NewInference")
		}
	}


	private fun Project.configureBasics() {
		apply<KotlinMultiplatformPluginWrapper>()
		apply<SerializationGradleSubplugin>()

		group = "io.fluidsonic.${libraryConfiguration.name}"
		version = libraryConfiguration.version
		description = configuration.description?.ifEmpty { null }

		repositories {
			mavenCentral()
			jcenter()
			bintray("fluidsonic/kotlin")
			bintray("kotlin/kotlin-eap")
			bintray("kotlin/kotlinx")
		}

		kotlin {
			if (!configuration.language.noExplicitApi)
				explicitApi()

			configureCommonTarget()
			configureJsTargets()
			configureJvmTargets()
			configureNativeDarwinTargets()
		}
	}


	private fun Project.configurePublishing() {
		val bintrayUser = findProperty("bintrayUser") as String? ?: return
		val bintrayKey = findProperty("bintrayApiKey") as String? ?: return

		apply<MavenPublishPlugin>()
		apply<SigningPlugin>()

		val emptyJar by tasks.creating(Jar::class)

		publishing {
			repositories {
				maven {
					setUrl("https://api.bintray.com/maven/fluidsonic/kotlin/${libraryConfiguration.name}/;publish=1")
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
						name.set(libraryConfiguration.fullName)
						description.set(project.description)
						url.set("https://github.com/fluidsonic/${libraryConfiguration.fullName}")
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
								url.set("https://github.com/fluidsonic/${libraryConfiguration.fullName}/blob/master/LICENSE")
							}
						}
						scm {
							connection.set("scm:git:https://github.com/fluidsonic/${libraryConfiguration.fullName}.git")
							developerConnection.set("scm:git:git@github.com:fluidsonic/${libraryConfiguration.fullName}.git")
							url.set("https://github.com/fluidsonic/${libraryConfiguration.fullName}")
						}
					}
				}
			}
		}

		signing {
			sign(publishing.publications)
		}

		if (configuration.isPublishingSingleTargetAsModule) {
			val targets = kotlin.targets.filter { it.name != "metadata" }
			val singleTarget = targets.singleOrNull()
				?: error("'publishSingleTargetAsModule()' can only be used in modules with exactly one target: $targets")

			singleTarget.mavenPublication {
				val publication = this
				artifactId = project.name

				tasks.withType<AbstractPublishToMaven> {
					isEnabled = this.publication == publication
				}
			}
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
}
