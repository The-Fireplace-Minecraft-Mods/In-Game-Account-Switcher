plugins {
    id("fabric-loom") version "1.5-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.ias"
base.archivesName = "IAS-Fabric-1.20.4"
evaluationDependsOn(":1.20.4")
val shared = project(":1.20.4")

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://api.modrinth.com/maven/")
}

loom.runs.named("client") {
    vmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=fatjar", "-Dfabric.debug.disableClassPathIsolation=true")
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.95.4+1.20.4")
    modImplementation("com.terraformersmc:modmenu:9.0.0")

    // Root
    compileOnly(shared)

    // Speed up testing
    runtimeOnly("maven.modrinth:lazydfu:0.1.3")
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
}
