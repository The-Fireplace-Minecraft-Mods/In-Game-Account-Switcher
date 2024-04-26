plugins {
    id("dev.architectury.loom") version "1.6-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
java.toolchain.languageVersion = JavaLanguageVersion.of(21)
group = "ru.vidtu.ias"
base.archivesName = "IAS-NeoForge-1.20.5"
evaluationDependsOn(":1.20.5")
val shared = project(":1.20.5")

loom {
    silentMojangMappingsLicense()
    neoForge {
         // Empty
    }
    runs.named("client") {
        vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-Xmx2G", "-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=fatjar", "-Dfabric.debug.disableClassPathIsolation=true")
        programArgs("--mixin", "ias.mixins.json")
    }
    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp = true
        defaultRefmapName = "ias.mixins.refmap.json"
    }
}

repositories {
    mavenCentral()
    maven("https://maven.architectury.dev/")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:1.20.5")
    mappings(loom.officialMojangMappings())

    // NeoForge
    neoForge("net.neoforged:neoforge:20.5.5-beta")

    // Root
    compileOnly(shared)
}

tasks.withType<JavaCompile> {
    source(rootProject.sourceSets.main.get().java)
    source(shared.sourceSets.main.get().java)
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<ProcessResources> {
    from(rootProject.sourceSets.main.get().resources)
    from(shared.sourceSets.main.get().resources)
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
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
                "Implementation-Title" to "IAS-NeoForge-1.20.5",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "VidTu"
        )
    }
}
