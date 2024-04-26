plugins {
    id("dev.architectury.loom") version "1.6-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.ias"
base.archivesName = "IAS-1.19.4"

loom {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:1.19.4")
    mappings(loom.officialMojangMappings())

    // Mixin
    compileOnly(libs.mixin)

    // Root
    compileOnlyApi(rootProject)
}

tasks.withType<JavaCompile> {
    source(rootProject.sourceSets.main.get().java)
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<ProcessResources> {
    from(rootProject.sourceSets.main.get().resources)
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("GPL"))
    manifest {
        attributes(
                "Specification-Title" to "In-Game Account Switcher",
                "Specification-Version" to project.version,
                "Specification-Vendor" to "VidTu",
                "Implementation-Title" to "IAS-1.19.4",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "VidTu"
        )
    }
}
