rootProject.name = "QRCodeGenerator"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    
    plugins {
        kotlin("jvm") version "1.9.20"
        id("org.jetbrains.compose") version "1.5.11"
        kotlin("plugin.serialization") version "1.9.20"
    }
}
