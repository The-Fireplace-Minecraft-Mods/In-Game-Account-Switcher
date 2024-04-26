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
include("1.19.4", "1.19.4-fabric", "1.19.4-quilt", "1.19.4-forge")
include("1.20.1", "1.20.1-fabric", "1.20.1-quilt", "1.20.1-forge", "1.20.1-neoforge")
include("1.20.4", "1.20.4-fabric", "1.20.4-quilt", "1.20.4-forge", "1.20.4-neoforge")
include("1.20.5", "1.20.5-fabric", "1.20.5-quilt", "1.20.5-neoforge")
