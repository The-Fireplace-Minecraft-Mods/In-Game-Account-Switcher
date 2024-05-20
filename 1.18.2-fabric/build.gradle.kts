plugins {
    id("dev.architectury.loom") version "1.6-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.ias"
base.archivesName = "IAS-Fabric-1.18.2"
evaluationDependsOn(":1.18.2")
val shared = project(":1.18.2")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://api.modrinth.com/maven/")
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
    // Minecraft
    minecraft("com.mojang:minecraft:1.18.2")
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.77.0+1.18.2")
    modImplementation("com.terraformersmc:modmenu:3.2.5")

    // Root
    compileOnly(shared)

    // Testing
    runtimeOnly("maven.modrinth:lazydfu:0.1.2")
}

tasks.withType<JavaCompile> {
    source(rootProject.sourceSets.main.get().java)
    source(shared.sourceSets.main.get().java)
    options.encoding = "UTF-8"
    options.release = 17
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
            "Implementation-Title" to "IAS-Fabric-1.18.2",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu"
        )
    }
}
