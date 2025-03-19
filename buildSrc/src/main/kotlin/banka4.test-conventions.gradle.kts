plugins {
	id("jacoco")
	java
}

dependencies {
	testImplementation(project(":testlib"))
}

tasks.test {
	useJUnitPlatform {
		excludeTags("integration")
	}
}

val test by testing.suites.existing(JvmTestSuite::class)
val integrationTest = tasks.register<Test>("integrationTest") {
	group = "verification"
	description =
		"Runs tests marked as integration tests.  These are slower, so, they are separate."
	useJUnitPlatform {
		includeTags("integration")
		// Don't include untagged stuff.
		excludeTags("none()")
	}
	shouldRunAfter("test")

	testClassesDirs = files(test.map { it.sources.output.classesDirs })
	classpath = files(test.map { it.sources.runtimeClasspath })
}

tasks.jacocoTestReport {
	// Sync the path up with below.
	reports {
		xml.required = true
		csv.required = true
		html.outputLocation = layout.buildDirectory.dir("reports/jacocoTest")
	}
}

val integrationTestReport = tasks.register<JacocoReport>("jacocoIntegrationTestReport") {
	// For some reason, the dumb thing misdetects this path if I pass it
	// 'integrationTest' directly.
	group = "verification"
	executionData(layout.buildDirectory.file("jacoco/integrationTest.exec"))
	sourceSets(sourceSets.main.get())
	reports {
		xml.required = true
		csv.required = true
		html.outputLocation =
			layout.buildDirectory.dir("reports/jacocoIntegrationTest")
	}
	dependsOn(integrationTest)
}

tasks.check {
	dependsOn(tasks.test, integrationTest)
}

tasks.register("generateAllTestReports") {
	group = "verification"
	description = "Convenience task to generate all test and coverage reports"
	dependsOn(tasks.test, integrationTest, tasks.jacocoTestReport, integrationTestReport)
}
