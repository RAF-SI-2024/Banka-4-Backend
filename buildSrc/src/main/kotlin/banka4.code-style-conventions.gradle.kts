import com.diffplug.gradle.spotless.FormatExtension

plugins {
	id("com.diffplug.spotless")
}

spotless {
	fun FormatExtension.commonStyle() {
		trimTrailingWhitespace()
		endWithNewline()
	}
	java {
		commonStyle()

		importOrder()
		removeUnusedImports()

		eclipse()
			.configFile(rootProject.file("common/java-code-style.xml"))
	}
}
