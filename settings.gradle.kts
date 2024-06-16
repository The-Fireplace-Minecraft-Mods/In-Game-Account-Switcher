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
include("1.18.2", "1.18.2-fabric", "1.18.2-quilt", "1.18.2-forge")
include("1.19.2", "1.19.2-fabric", "1.19.2-quilt", "1.19.2-forge")
include("1.19.4", "1.19.4-fabric", "1.19.4-quilt", "1.19.4-forge")
include("1.20.1", "1.20.1-fabric", "1.20.1-quilt", "1.20.1-forge", "1.20.1-neoforge")
include("1.20.2", "1.20.2-fabric", "1.20.2-quilt", "1.20.2-forge", "1.20.2-neoforge")
include("1.20.4", "1.20.4-fabric", "1.20.4-quilt", "1.20.4-forge", "1.20.4-neoforge")
// TODO include("1.20.6", "1.20.6-fabric", "1.20.6-quilt", "1.20.6-forge", "1.20.6-neoforge")
include("1.20.6", "1.20.6-fabric", "1.20.6-quilt", "1.20.6-neoforge")
include("1.21", "1.21-fabric", "1.21-quilt", "1.21-forge", "1.21-neoforge")
