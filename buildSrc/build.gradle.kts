plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

dependencies {
	// JUnit coordinates are hardcoded here because buildSrc does not share the root
	// project's version catalog by default. Keep the version in sync with `junit` in
	// `gradle/libs.versions.toml`.
	testImplementation(kotlin("test"))
	testImplementation(kotlin("test-junit5"))
	testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.3")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
