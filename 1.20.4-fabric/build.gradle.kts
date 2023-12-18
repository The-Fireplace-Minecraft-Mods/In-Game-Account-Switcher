plugins {
    id("fabric-loom") version "1.4-SNAPSHOT"
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
    maven("https://api.modrinth.com/maven")
}

dependencies {
    val fabric = project.properties["fabric"]
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${fabric}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.91.3+1.20.4")
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
}
