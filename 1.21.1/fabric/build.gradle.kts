plugins {
    id("dev.architectury.loom") version "1.7-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
java.toolchain.languageVersion = JavaLanguageVersion.of(21)
group = "ru.vidtu.ias"
base.archivesName = "IAS-Fabric-1.21.1"
description = "This mod allows you to change your logged in account in-game, without restarting Minecraft."
evaluationDependsOn(":1.21.1-root")
val shared = project(":1.21.1-root")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
}

loom {
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs(
            "-XX:+IgnoreUnrecognizedVMOptions",
            "-Xmx2G",
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:HotswapAgent=fatjar",
            "-Dfabric.debug.disableClassPathIsolation=true"
        )
    }
    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName = "ias.mixins.refmap.json"
    }
}

dependencies {
    // Annotations
    compileOnlyApi(libs.jetbrains.annotations)
    compileOnlyApi(libs.error.prone.annotations)

    // Minecraft
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.1+1.21.1")
    modImplementation("com.terraformersmc:modmenu:11.0.1")

    // Root
    compileOnly(shared)
}

tasks.withType<JavaCompile> {
    source(rootProject.sourceSets.main.get().java)
    source(shared.sourceSets.main.get().java)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 21
}

tasks.withType<ProcessResources> {
    from(rootProject.sourceSets.main.get().resources)
    from(shared.sourceSets.main.get().resources)
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("GPL"))
    from(rootDir.resolve("NOTICE"))
    manifest {
        attributes(
            "Specification-Title" to "In-Game Account Switcher",
            "Specification-Version" to project.version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "IAS-Fabric-1.21.1",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu"
        )
    }
}
