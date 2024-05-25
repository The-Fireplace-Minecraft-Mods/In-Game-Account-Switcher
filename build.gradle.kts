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
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 17
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
            "Implementation-Title" to "IAS-Root",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "VidTu"
        )
    }
}
