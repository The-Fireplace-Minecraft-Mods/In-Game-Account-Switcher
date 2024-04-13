plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.ias"
base.archivesName = "IAS-Root"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.12")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("GPL"))
    manifest {
        attributes(
                "Specification-Title" to "In-Game Account Switcher",
                "Specification-Version" to project.version,
                "Specification-Vendor" to "VidTu",
                "Implementation-Title" to "IAS-Root",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "VidTu",
                "MixinConfigs" to "ias.mixins.json"
        )
    }
}
