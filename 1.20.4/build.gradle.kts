plugins {
    id("fabric-loom") version "1.4-SNAPSHOT"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.ias"
base.archivesName = "IAS-1.20.4"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    val mixin = project.properties["mixin"]
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())
    compileOnly("org.spongepowered:mixin:${mixin}")
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
}
