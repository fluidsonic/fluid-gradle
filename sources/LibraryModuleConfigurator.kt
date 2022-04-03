package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.attributes.java.*
import org.gradle.api.publish.maven.*
import org.gradle.api.publish.maven.plugins.*
import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.jvm.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.dokka.gradle.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.internal.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.testing.*
import org.jetbrains.kotlinx.serialization.gradle.*


internal class LibraryModuleConfigurator(
	configuration: LibraryModuleConfiguration,
	private val project: Project,
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
				sourceSets.all {
					languageSettings.apply {
						languageVersion = configuration.language.version ?: LibraryModuleConfiguration.Language.default.version

						configuration.language.experimentalApisToUse.forEach { optIn(it) }
						configuration.language.languageFeaturesToEnable.forEach { enableLanguageFeature(it) }
						configuration.language.customConfigurations.forEach { it() }
					}
				}

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
				configureSourceSetBasics(pathSuffix = "", dependencies = targetConfiguration.dependencies)
			}

			named(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME) {
				configureSourceSetBasics(pathSuffix = "", dependencies = targetConfiguration.testDependencies)

				dependencies {
					implementation(kotlin("test-common"))
					implementation(kotlin("test-annotations-common"))
				}
			}
		}

		metadata {
			configureTargetBasics(targetConfiguration)

			targetConfiguration.customConfigurations.forEach { it() }
		}
	}


	private fun KotlinMultiplatformExtension.configureDarwinTargets() {
		val targetConfiguration = configuration.targets.darwin
			?.takeIf {
				!it.noIosArm32 ||
					!it.noIosArm64 ||
					!it.noIosSimulatorArm64 ||
					!it.noIosX64 ||
					!it.noMacosArm64 ||
					!it.noMacosX64 ||
					!it.noTvosArm64 ||
					!it.noTvosSimulatorArm64 ||
					!it.noTvosX64 ||
					!it.noWatchosArm32 ||
					!it.noWatchosArm64 ||
					!it.noWatchosSimulatorArm64 ||
					!it.noWatchosX64 ||
					!it.noWatchosX86
			}
			?: return

		if (!targetConfiguration.noIosArm32)
			iosArm32 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-ios-arm32")
			}

		if (!targetConfiguration.noIosArm64)
			iosArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-ios-arm64")
			}

		if (!targetConfiguration.noIosSimulatorArm64)
			iosSimulatorArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-ios-simulator-arm64")
			}

		if (!targetConfiguration.noIosX64)
			iosX64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-ios-x64")
			}

		if (!targetConfiguration.noMacosArm64)
			macosArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-macos-arm64")
			}

		if (!targetConfiguration.noMacosX64)
			macosX64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-macos-x64")
			}

		if (!targetConfiguration.noTvosArm64)
			tvosArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-tvos-arm64")
			}

		if (!targetConfiguration.noTvosSimulatorArm64)
			tvosSimulatorArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-tvos-simulator-arm64")
			}

		if (!targetConfiguration.noTvosX64)
			tvosX64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-tvos-x64")
			}

		if (!targetConfiguration.noWatchosArm32)
			watchosArm32 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-watchos-arm32")
			}

		if (!targetConfiguration.noWatchosArm64)
			watchosArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-watchos-arm64")
			}

		if (!targetConfiguration.noWatchosSimulatorArm64)
			watchosSimulatorArm64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-watchos-simulator-arm64")
			}

		if (!targetConfiguration.noWatchosX64)
			watchosX64 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-watchos-x64")
			}

		if (!targetConfiguration.noWatchosX86)
			watchosX86 {
				configureTarget(targetConfiguration = targetConfiguration, pathSuffix = "-watchos-x86")
			}

		sourceSets {
			val commonMain by getting
			val commonTest by getting

			val darwinMain by creating {
				dependsOn(commonMain)

				configureSourceSetBasics(pathSuffix = "-darwin", dependencies = targetConfiguration.dependencies)
			}

			val darwinTest by creating {
				dependsOn(commonTest)

				configureSourceSetBasics(pathSuffix = "-darwin", dependencies = targetConfiguration.testDependencies)
			}

			if (!targetConfiguration.noIosArm32 ||
				!targetConfiguration.noIosArm64 ||
				!targetConfiguration.noIosSimulatorArm64 ||
				!targetConfiguration.noIosX64
			) {
				// TODO Enable once Commonizer is no longer limited to one level in the hierarchy.
				//      https://github.com/JetBrains/kotlin/blob/1.4-M2/native/commonizer/README.md
//				val iosMain by creating {
//					dependsOn(darwinMain)
//
//					kotlin.setSrcDirs(listOf("sources/ios"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}
//
//				val iosTest by creating {
//					dependsOn(darwinTest)
//
//					kotlin.setSrcDirs(listOf("tests/ios"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}

				if (!targetConfiguration.noIosArm32) {
					getByName("iosArm32Main") {
						dependsOn(darwinMain)
					}

					getByName("iosArm32Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noIosArm64) {
					getByName("iosArm64Main") {
						dependsOn(darwinMain)
					}

					getByName("iosArm64Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noIosSimulatorArm64) {
					getByName("iosSimulatorArm64Main") {
						dependsOn(darwinMain)
					}

					getByName("iosSimulatorArm64Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noIosX64) {
					getByName("iosX64Main") {
						dependsOn(darwinMain)
					}

					getByName("iosX64Test") {
						dependsOn(darwinTest)
					}
				}
			}

			if (!targetConfiguration.noMacosArm64) {
				getByName("macosArm64Main") {
					dependsOn(darwinMain)
				}

				getByName("macosArm64Test") {
					dependsOn(darwinTest)
				}
			}

			if (!targetConfiguration.noMacosX64) {
				getByName("macosX64Main") {
					dependsOn(darwinMain)
				}

				getByName("macosX64Test") {
					dependsOn(darwinTest)
				}
			}

			if (!targetConfiguration.noTvosArm64 || !targetConfiguration.noTvosSimulatorArm64 || !targetConfiguration.noTvosX64) {
				// TODO Enable once Commonizer is no longer limited to one level in the hierarchy.
				//      https://github.com/JetBrains/kotlin/blob/1.4-M2/native/commonizer/README.md
//				val tvosMain by creating {
//					dependsOn(darwinMain)
//
//					kotlin.setSrcDirs(listOf("sources/tvos"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}
//
//				val tvosTest by creating {
//					dependsOn(darwinTest)
//
//					kotlin.setSrcDirs(listOf("tests/tvos"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}

				if (!targetConfiguration.noTvosArm64) {
					getByName("tvosArm64Main") {
						dependsOn(darwinMain)
					}

					getByName("tvosArm64Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noTvosSimulatorArm64) {
					getByName("tvosSimulatorArm64Main") {
						dependsOn(darwinMain)
					}

					getByName("tvosSimulatorArm64Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noTvosX64) {
					getByName("tvosX64Main") {
						dependsOn(darwinMain)
					}

					getByName("tvosX64Test") {
						dependsOn(darwinTest)
					}
				}
			}

			if (
				!targetConfiguration.noWatchosArm32
				|| !targetConfiguration.noWatchosArm64
				|| !targetConfiguration.noWatchosSimulatorArm64
				|| !targetConfiguration.noWatchosX64
				|| !targetConfiguration.noWatchosX86
			) {
				// TODO Enable once Commonizer is no longer limited to one level in the hierarchy.
				//      https://github.com/JetBrains/kotlin/blob/1.4-M2/native/commonizer/README.md
//				val watchosMain by creating {
//					dependsOn(darwinMain)
//
//					kotlin.setSrcDirs(listOf("sources/watchos"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}
//
//				val watchosTest by creating {
//					dependsOn(darwinTest)
//
//					kotlin.setSrcDirs(listOf("tests/watchos"))
//					resources.setSrcDirs(emptyList<Any>())
//
//					configureSourceSet()
//				}

				if (!targetConfiguration.noWatchosArm32) {
					getByName("watchosArm32Main") {
						dependsOn(darwinMain)
					}

					getByName("watchosArm32Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noWatchosArm64) {
					getByName("watchosArm64Main") {
						dependsOn(darwinMain)
					}

					getByName("watchosArm64Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noWatchosSimulatorArm64) {
					getByName("watchosSimulatorArm64Main") {
						dependsOn(darwinMain)
					}

					getByName("watchosSimulatorArm64Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noWatchosX64) {
					getByName("watchosX64Main") {
						dependsOn(darwinMain)
					}

					getByName("watchosX64Test") {
						dependsOn(darwinTest)
					}
				}

				if (!targetConfiguration.noWatchosX86) {
					getByName("watchosX86Main") {
						dependsOn(darwinMain)
					}

					getByName("watchosX86Test") {
						dependsOn(darwinTest)
					}
				}
			}
		}
	}


	private fun KotlinMultiplatformExtension.configureJsTargets() {
		val targetConfiguration = configuration.targets.js ?: return

		js(targetConfiguration.compiler ?: KotlinJsCompilerType.IR) {
			configureTargetBasics(targetConfiguration)

			useCommonJs()

			compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
				defaultSourceSet {
					configureSourceSetBasics(pathSuffix = "-js", dependencies = targetConfiguration.dependencies)
				}
			}

			compilations.named(KotlinCompilation.TEST_COMPILATION_NAME) {
				defaultSourceSet {
					configureSourceSetBasics(pathSuffix = "-js", dependencies = targetConfiguration.testDependencies)

					dependencies {
						api(kotlin("test-js"))
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


	private fun KotlinMultiplatformExtension.configureJvmTarget(
		jdkVersion: JdkVersion,
		targetConfiguration: LibraryModuleConfiguration.Target.Jvm,
		targetName: String,
		pathSuffix: String,
	) {
		require(jdkVersion >= JdkVersion.v8)

		if (targetConfiguration.dependencies.kaptConfigurations.isNotEmpty() && !targetConfiguration.includesJava)
			error("withJava() must be used in target '$targetName' when using kapt() dependencies.")

		jvm(targetName) {
			if (targetConfiguration.includesJava)
				withJava()

			configureTargetBasics(targetConfiguration)

			compilations.forEach { compilation ->
				compilation.kotlinOptions {
					jvmTarget = jdkVersion.kotlinJvmTargetValue
					useOldBackend = targetConfiguration.noIR
				}
			}

			compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
				attributes {
					attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jdkVersion.code)
				}

				defaultSourceSet {
					configureSourceSetBasics(pathSuffix = pathSuffix, dependencies = targetConfiguration.dependencies)
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
					configureSourceSetBasics(pathSuffix = pathSuffix, dependencies = targetConfiguration.testDependencies)

					dependencies {
						implementation(kotlin("test-junit5"))
						implementation("org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}")

						runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
						runtimeOnly("org.junit.platform:junit-platform-runner:${Versions.junitPlatform}")
					}
				}
			}

			testRuns.all {
				executionTask {
					useJUnitPlatform {
						includeEngines("junit-jupiter")
					}

					configureTestTask()
				}
			}

			targetConfiguration.customConfigurations.forEach { it() }
		}

		if (targetConfiguration.dependencies.kaptConfigurations.isNotEmpty()) {
			project.apply<Kapt3GradleSubplugin>()
			project.dependencies {
				targetConfiguration.dependencies.kaptConfigurations.forEach { it() }
			}
		}
	}


	private fun KotlinMultiplatformExtension.configureJvmTargets() {
		val jvmConfiguration = configuration.targets.jvm ?: return

		configureJvmTarget(
			jdkVersion = JdkVersion.v8,
			targetConfiguration = jvmConfiguration,
			targetName = "jvm",
			pathSuffix = "-jvm"
		)
	}


	private fun KotlinNativeTarget.configureTarget(
		targetConfiguration: LibraryModuleConfiguration.Target.Darwin,
		pathSuffix: String,
	) {
		configureTargetBasics(targetConfiguration)

		compilations[KotlinCompilation.MAIN_COMPILATION_NAME].defaultSourceSet {
			configureSourceSetBasics(pathSuffix = pathSuffix, dependencies = null)
		}

		compilations[KotlinCompilation.TEST_COMPILATION_NAME].defaultSourceSet {
			configureSourceSetBasics(pathSuffix = pathSuffix, dependencies = null)
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


	private fun KotlinSourceSet.configureSourceSetBasics(pathSuffix: String, dependencies: LibraryModuleConfiguration.Dependencies?) {
		when {
			name.endsWith("Test") -> {
				kotlin.setSrcDirs(listOf("tests$pathSuffix"))
				resources.setSrcDirs(listOf("test-resources$pathSuffix"))
			}
			else -> {
				kotlin.setSrcDirs(listOf("sources$pathSuffix"))
				resources.setSrcDirs(listOf("resources$pathSuffix"))
			}
		}

		if (dependencies != null)
			dependencies {
				dependencies.configurations.forEach { it() }
			}
	}


	private fun KotlinTarget.configureTargetBasics(targetConfiguration: LibraryModuleConfiguration.Target) {
		if (targetConfiguration.enforcesSameVersionForAllKotlinDependencies) {
			val kotlinVersion = project.getKotlinPluginVersion()

			compilations.all {
				kotlinOptions.freeCompilerArgs += "-Xinline-classes"

				project.configurations.getByName(compileDependencyConfigurationName) {
					resolutionStrategy.eachDependency {
						if (requested.group == "org.jetbrains.kotlin") {
							useVersion(kotlinVersion)
							because("All Kotlin modules must have the same version.")
						}
					}
				}
			}
		}
	}


	private fun Project.configureBasics() {
		apply<DokkaPlugin>()
		apply<KotlinMultiplatformPluginWrapper>()
		apply<SerializationGradleSubplugin>()

		group = "io.fluidsonic.${libraryConfiguration.name}"
		version = libraryConfiguration.version
		description = configuration.description?.ifEmpty { null }

		repositories {
			mavenCentral()
			maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers/")
		}

		kotlin {
			if (!configuration.language.noExplicitApi)
				explicitApi()

			configureCommonTarget()
			configureDarwinTargets()
			configureJsTargets()
			configureJvmTargets()
		}
	}


	private fun Project.configurePublishing() {
		val githubActor: String? = System.getenv("GITHUB_ACTOR")
		val githubToken: String? = System.getenv("GITHUB_TOKEN")
		val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
		val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")

		apply<MavenPublishPlugin>()
		apply<SigningPlugin>()

		publishing {
			repositories {
				if (ossrhUsername != null && ossrhPassword != null)
					maven {
						name = "OSSRH"
						setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
						credentials {
							username = ossrhUsername
							password = ossrhPassword
						}
					}

				if (githubActor != null && githubToken != null)
					maven {
						name = "GitHubPackages"
						setUrl("https://maven.pkg.github.com/fluidsonic/${libraryConfiguration.fullName}")
						credentials {
							username = githubActor
							password = githubToken
						}
					}
			}

			publications.withType<MavenPublication> {
				pom {
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

		signing {
			sign(publishing.publications)
		}

		if (configuration.targets.jvm != null) {
			val javadocJar by tasks.registering(Jar::class) {
				from(tasks.named("dokkaHtml")) // https://github.com/Kotlin/dokka/issues/1753
				archiveClassifier.set("javadoc")
			}

			publishing.publications
				.filterIsInstance<MavenPublication>()
				.single { it.name == "jvm" }
				.artifact(javadocJar)
		}
	}
}
