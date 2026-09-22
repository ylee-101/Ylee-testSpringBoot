pluginManagement {
	repositories {
		if (providers.gradleProperty("offlineMavenOnly").getOrElse("false").toBoolean()) {
			maven { url = uri("${rootDir}/offline-maven") }
		} else {
			gradlePluginPortal()
			mavenCentral()
		}
	}
}

rootProject.name = "demo"
