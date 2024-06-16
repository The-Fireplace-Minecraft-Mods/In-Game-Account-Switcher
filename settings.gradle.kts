pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.quiltmc.org/repository/release/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "In-Game Account Switcher"
val types = listOf("root", "fabric", "quilt", "forge", "neoforge")
val versions = listOf("1.18.2", "1.19.2", "1.19.4", "1.20.1", "1.20.2", "1.20.4", "1.20.6", "1.21")
for (version in versions) {
    for (type in types) {
        include("$version-$type")
        findProject(":$version-$type")?.projectDir = file("$version/$type")
    }
}
