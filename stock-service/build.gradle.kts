plugins {
	java
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	id("banka4.test-conventions")
	id("banka4.code-style-conventions")
	id("banka4.migration-generator")
}

group = "rs.banka4"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":common"))

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter")
	val hibernateVer = dependencyManagement.importedProperties["hibernate.version"]
	annotationProcessor("org.hibernate:hibernate-jpamodelgen:${hibernateVer}")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.5")
	implementation("org.springdoc:springdoc-openapi-starter-common:2.8.5")

	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	implementation("com.squareup.retrofit2:retrofit:2.11.0")
	implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
	implementation("com.squareup.retrofit2:converter-jackson:2.11.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Local Variables:
// mode: prog
// indent-tabs-mode: t
// End:
