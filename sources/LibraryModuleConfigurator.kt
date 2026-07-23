package io.fluidsonic.gradle

import org.gradle.api.*
import org.gradle.api.attributes.java.*
import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.internal.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlinx.serialization.gradle.*


/**
 * Applies a module's configuration to its [project]: sets up the Kotlin multiplatform and serialization plugins, the
 * requested targets, language settings, dependencies, test tasks, and (when enabled) publishing.
 *
 * The [configuration] passed in is merged onto the library-wide `allModules {}` default before being applied.
 */
internal class LibraryModuleConfigurator(
	configuration: LibraryModuleConfiguration,
	private val project: Project,
) {

	private val libraryConfiguration = project.fluidLibrary.configuration
	private val configuration = libraryConfiguration.defaultModuleConfiguration.mergeWith(
		other = configuration,
		addTargetsAutomatically = false
	)


	/** Applies the merged module configuration to the project. */
	fun configure() {
		with(project) {
			configureBasics()

			if (configuration.isPublishingEnabled)
				PublishingConfigurator(
					project = project,
					configuration = configuration,
					libraryConfiguration = libraryConfiguration,
				).configure()

			kotlin {
				sourceSets.all {
					languageSettings.apply {
						languageVersion = configuration.language.version ?: LibraryModuleConfiguration.Language.default.version

						configuration.language.experimentalApisToUse.forEach { optIn(it) }
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

				addKotlinTestDependency()
			}
		}

		metadata {
			configureTargetBasics(targetConfiguration)

			targetConfiguration.customConfigurations.forEach { it() }
		}
	}


	private fun KotlinMultiplatformExtension.configureJsTargets() {
		val targetConfiguration = configuration.targets.js ?: return

		js {
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

					addKotlinTestDependency()
				}
			}

			if (targetConfiguration.isBrowserEnabled)
				browser {
					testTask {
						configureTestTask()
					}
				}

			if (targetConfiguration.isNodeJsEnabled)
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
		require(jdkVersion >= JdkVersion.v21)

		jvm(targetName) {
			configureTargetBasics(targetConfiguration)

			compilations.forEach { compilation ->
				compilation.compileTaskProvider.configure {
					compilerOptions {
						jvmTarget.set(jdkVersion.kotlinJvmTargetValue)
					}
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

				compileTaskProvider.configure {
					compilerOptions {
						jvmTarget.set(jdkVersion.kotlinJvmTargetValue)
					}
				}

				defaultSourceSet {
					configureSourceSetBasics(pathSuffix = pathSuffix, dependencies = targetConfiguration.testDependencies)

					addJvmTestDependencies()
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

		if (targetConfiguration.dependencies.kspConfigurations.isNotEmpty()) {
			project.pluginManager.apply("com.google.devtools.ksp")
			project.dependencies {
				targetConfiguration.dependencies.kspConfigurations.forEach { it() }
			}
		}
	}


	private fun KotlinMultiplatformExtension.configureJvmTargets() {
		val jvmConfiguration = configuration.targets.jvm ?: return

		configureJvmTarget(
			jdkVersion = jvmConfiguration.jdkVersion,
			targetConfiguration = jvmConfiguration,
			targetName = "jvm",
			pathSuffix = "-jvm"
		)
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


	private fun KotlinSourceSet.addKotlinTestDependency() {
		dependencies {
			implementation(kotlin("test"))
		}
	}


	private fun KotlinSourceSet.addJvmTestDependencies() {
		dependencies {
			implementation(kotlin("test-junit5"))
			implementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")

			runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
		}
	}


	private fun KotlinTarget.configureTargetBasics(targetConfiguration: LibraryModuleConfiguration.Target) {
		if (targetConfiguration.enforcesSameVersionForAllKotlinDependencies) {
			val kotlinVersion = project.getKotlinPluginVersion()

			compilations.all {
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
		if (configuration.isDokkaEnabled)
			pluginManager.apply("org.jetbrains.dokka")

		apply<KotlinMultiplatformPluginWrapper>()
		apply<SerializationGradleSubplugin>()

		group = "io.fluidsonic.${libraryConfiguration.name}"
		version = libraryConfiguration.version
		description = configuration.description?.ifEmpty { null }

		useMavenCentralRepository()

		kotlin {
			if (configuration.language.isExplicitApiEnabled)
				explicitApi()

			configureCommonTarget()
			configureJsTargets()
			configureJvmTargets()
		}
	}


}
