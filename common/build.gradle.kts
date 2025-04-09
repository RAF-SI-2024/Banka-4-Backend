plugins {
	`java-library`
	id("io.spring.dependency-management")
	id("banka4.test-conventions")
	id("banka4.code-style-conventions")
}

group = "rs.banka4"
version = "0.0.1-SNAPSHOT"


dependencyManagement {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	api("org.springframework.security:spring-security-core")
	api("org.springframework:spring-web")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.5")


	val jjwtVer = "0.12.6"
	api("io.jsonwebtoken:jjwt-api:${jjwtVer}")
	api("io.jsonwebtoken:jjwt-impl:${jjwtVer}")
	api("io.jsonwebtoken:jjwt-jackson:${jjwtVer}")

	testImplementation("org.assertj:assertj-core")
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
