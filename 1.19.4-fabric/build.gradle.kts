plugins {
    id("dev.architectury.loom") version "1.6-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.ias"
base.archivesName = "IAS-Fabric-1.19.4"
evaluationDependsOn(":1.19.4")
val shared = project(":1.19.4")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases/")
}

loom {
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-Xmx2G", "-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=fatjar", "-Dfabric.debug.disableClassPathIsolation=true")
    }
    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName = "ias.mixins.refmap.json"
    }
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:1.19.4")
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.87.2+1.19.4")
    modImplementation("com.terraformersmc:modmenu:6.3.1")

    // Root
    compileOnly(shared)
}

tasks.withType<JavaCompile> {
    source(rootProject.sourceSets.main.get().java)
    source(shared.sourceSets.main.get().java)
    options.encoding = "UTF-8"
    options.release.set(17)
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
    manifest {
        attributes(
                "Specification-Title" to "In-Game Account Switcher",
                "Specification-Version" to project.version,
                "Specification-Vendor" to "VidTu",
                "Implementation-Title" to "IAS-Fabric-1.19.4",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "VidTu"
        )
    }
}
