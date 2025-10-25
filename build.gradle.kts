/*
 * In-Game Account Switcher is a third-party mod for Minecraft Java Edition that
 * allows you to change your logged in account in-game, without restarting it.
 *
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

import com.google.gson.Gson
import com.google.gson.JsonElement
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask
import net.fabricmc.loom.util.ZipUtils
import net.fabricmc.loom.util.ZipUtils.UnsafeUnaryOperator

plugins {
    alias(libs.plugins.architectury.loom)
}

// Extract the platform and Minecraft version.
val platform = loom.platform.get().id()!!
// NeoForge 1.20.1 is loosely Forge, but not Forge. It uses ModPlatform.FORGE loom platform
// and Forge packages, but diverges from (can't keep up with) the (Lex/Upstream) MCForge 1.20.1.
val hackyNeoForge = (name == "1.20.1-neoforge")
val minecraft = stonecutter.current.version

// Determine and set Java toolchain version.
// TODO(VidTu): Revisit after making the decision about 1.16.5/1.17.1 support.
val javaTarget = if (stonecutter.eval(minecraft, ">=1.20.6")) 21
else if (stonecutter.eval(minecraft, ">=1.18.2")) 17
else if (stonecutter.eval(minecraft, ">=1.17.1")) 16
else 8
val javaVersion = JavaVersion.toVersion(javaTarget)
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion
java.toolchain.languageVersion = JavaLanguageVersion.of(javaTarget)

// TODO(VidTu): When all legacy versions are done, the code from the legacy_shared module
// (and assets too) should be just fine to copy into the root code.
evaluationDependsOn(":legacy_shared") // Migration helper.
val legacyShared = project(":legacy_shared") // Migration helper.

group = "ru.vidtu.ias"
base.archivesName = "IAS"
version = "$version+$name"
description = "Allows you to change which account you are signed in to in-game without restarting Minecraft."

stonecutter {
    // Define Stonecutter preprocessor variables.
    constants["hacky_neoforge"] = hackyNeoForge
    constants {
        match(platform, "fabric", "forge", "neoforge")
    }

    // Process the JSON files via Stonecutter.
    // This is needed for the Mixin configuration.
    filters {
        include("**/*.json")
    }
}

loom {
    // Prepare development environment.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))
    silentMojangMappingsLicense()

    // Setup JVM args, see that file.
    runs.named("client") {
        // Set up debug VM args.
        // TODO(VidTu): Revisit after making the decision about 1.16.5/1.17.1 support.
        if (javaVersion.isJava9Compatible) {
            vmArgs("@../dev/args.vm.txt")
        } else {
            vmArgs(rootDir.resolve("dev/args.vm.txt")
                .readLines()
                .filter { "line.separator" !in it }
                .filter { it.isNotBlank() })
        }

        // Set the run dir.
        runDir = "../../run"

        // AuthLib for 1.16.5 is bugged, disable Mojang API
        // to fix issues with MP testing.
        if (minecraft == "1.16.5") { // TODO(VidTu): Revisit after making the decision about 1.16.5 support.
            vmArgs(
                "-Dminecraft.api.auth.host=http://0.0.0.0:0/",
                "-Dminecraft.api.account.host=http://0.0.0.0:0/",
                "-Dminecraft.api.session.host=http://0.0.0.0:0/",
                "-Dminecraft.api.services.host=http://0.0.0.0:0/",
            )
        }
    }

    // Configure Mixin.
    @Suppress("UnstableApiUsage") // <- Required to configure Mixin.
    mixin {
        // Some platforms don't set this and fail processing the Mixin.
        useLegacyMixinAp = true

        // Set the Mixin refmap name. This is completely optional.
        defaultRefmapName = "ias.mixins.refmap.json"
    }

    // Add Mixin configs.
    if (loom.isForge) {
        forge {
            mixinConfigs("ias.mixins.json")
        }
    } else if (loom.isNeoForge) {
        neoForge {}
    }
}

// Make the game run with the required Java path.
tasks.withType<RunGameTask> {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
}

repositories {
    mavenCentral()
    if (loom.isForge) {
        if (hackyNeoForge) {
            maven("https://maven.neoforged.net/releases/") // NeoForge. (Legacy)
        }
        maven("https://maven.minecraftforge.net/") // Forge.
    } else if (loom.isNeoForge) {
        maven("https://maven.neoforged.net/releases/") // NeoForge.
    } else {
        maven("https://maven.fabricmc.net/") // Fabric.
        maven("https://maven.terraformersmc.com/releases/") // ModMenu.
        if (minecraft == "1.20.4") { // Fix for ModMenu not shading Text Placeholder API.
            maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
        }
    }
}

dependencies {
    // Annotations.
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)
    testCompileOnly(libs.jspecify) // Migration helper.
    testCompileOnly(libs.jetbrains.annotations) // Migration helper.
    testCompileOnly(libs.error.prone.annotations) // Migration helper.

    // Minecraft. The dependency may be manually specified for example for snapshots.
    val minecraftDependencyProperty = findProperty("stonecutter.minecraft-dependency")
    require(minecraftDependencyProperty != minecraft) { "Unneeded 'stonecutter.minecraft-dependency' property set to $minecraftDependencyProperty in $project, it already uses this version." }
    val minecraftDependency = minecraftDependencyProperty ?: minecraft
    minecraft("com.mojang:minecraft:$minecraftDependency")
    mappings(loom.officialMojangMappings())

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
    // TODO(VidTu): Revisit after making the decision about 1.16.5/1.17.1 support.
    implementation(libs.log4j) {
        exclude("biz.aQute.bnd")
        exclude("com.github.spotbugs")
        exclude("org.osgi")
    }

    // Legacy Root migration
    compileOnly(legacyShared) // Migration helper.

    // Loader.
    if (loom.isForge) {
        if (hackyNeoForge) {
            // Legacy NeoForge.
            val neoforge = property("stonecutter.neoforge").toString()
            require(neoforge.isNotBlank() && neoforge != "[STONECUTTER]") { "NeoForge (legacy) version is not provided in $project." }
            val extractedMinecraft = neoforge.substringBefore('-')
            require(minecraft == extractedMinecraft) { "NeoForge (legacy) version '$neoforge' provides Minecraft $extractedMinecraft in $project, but we want $minecraft." }
            "forge"("net.neoforged:forge:$neoforge")
        } else {
            // Forge.
            val forge = property("stonecutter.forge").toString()
            require(forge.isNotBlank() && forge != "[STONECUTTER]") { "Forge version is not provided in $project." }
            val extractedMinecraft = forge.substringBefore('-')
            require(minecraft == extractedMinecraft) { "Forge version '$forge' provides Minecraft $extractedMinecraft in $project, but we want $minecraft." }
            "forge"("net.minecraftforge:forge:$forge")
        }
    } else if (loom.isNeoForge) {
        // Forge.
        val neoforge = property("stonecutter.neoforge").toString()
        require(neoforge.isNotBlank() && neoforge != "[STONECUTTER]") { "NeoForge version is not provided in $project." }
        val extractedMinecraft = "1.${neoforge.substringBeforeLast('.')}"
        require(minecraft == extractedMinecraft) { "NeoForge version '$neoforge' provides Minecraft $extractedMinecraft in $project, but we want $minecraft." }
        "neoForge"("net.neoforged:neoforge:$neoforge")
    } else {
        // Fabric.
        val fapi = property("stonecutter.fabric-api").toString()
        require(fapi.isNotBlank() && fapi != "[STONECUTTER]") { "Fabric API version is not provided in $project." }
        modImplementation(libs.fabric.loader)
        // TODO(VidTu): Modularize FAPI later.
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fapi")
        val modmenu = property("stonecutter.modmenu").toString()
        require(modmenu.isNotBlank() && modmenu != "[STONECUTTER]") { "ModMenu version is not provided in $project." }
        // Sometimes, ModMenu is not yet updated for the version. (it almost never updates to snapshots nowadays)
        // So we should depend on it compile-time (it is really an optional dependency for us) to allow both
        // compilation of an optional ModMenu compatibility class (HModMenu.java) and launching the game.
        if (findProperty("stonecutter.modmenu.compile-only").toString().toBoolean()) {
            modCompileOnly("com.terraformersmc:modmenu:$modmenu")
        } else {
            modImplementation("com.terraformersmc:modmenu:$modmenu")
        }
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    source(legacyShared.sourceSets.main.get().java) // Migration helper.
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    // JDK 8 (used by 1.16.x) doesn't support the "-release" flag and
    // uses "-source" and "-target" ones (see the top of the file),
    // so we must NOT specify it, or the "javac" will fail.
    // JDK 9+ does listen to this option.
    // TODO(VidTu): Revisit after making the decision about 1.16.5/1.17.1 support.
    if (javaVersion.isJava9Compatible) {
        options.release = javaTarget
    }
}

tasks.withType<ProcessResources> {
    from(legacyShared.sourceSets.main.get().resources) // Migration helper.
    // Filter with UTF-8.
    filteringCharset = "UTF-8"

    // Exclude not needed loader entrypoint files.
    if (loom.isForge) {
        exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
    } else if (loom.isNeoForge) {
        if (stonecutter.eval(minecraft, ">=1.20.6")) {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")
        } else {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
        }
    } else {
        exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
    }

    // Expand version and dependencies. The requirement may be manually specified for example for snapshots.
    val minecraftRequirementProperty = findProperty("stonecutter.minecraft-requirement")
    require(minecraftRequirementProperty != minecraft) { "Unneeded 'stonecutter.minecraft-requirement' property set to $minecraftRequirementProperty in $project, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: minecraft
    inputs.property("version", version)
    inputs.property("minecraft", minecraftRequirement)
    inputs.property("java", javaTarget)
    inputs.property("platform", platform)
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "ias.mixins.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(inputs.properties)
    }

    // Minify JSON (including ".mcmeta") and TOML files.
    val files = fileTree(outputs.files.asPath)
    doLast {
        val jsonAlike = Regex("^.*\\.(?:json|mcmeta)$", RegexOption.IGNORE_CASE)
        files.forEach {
            if (it.name.matches(jsonAlike)) {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            } else if (it.name.endsWith(".toml", ignoreCase = true)) {
                it.writeText(it.readLines()
                    .filter { s -> !s.startsWith('#') }
                    .filter { s -> s.isNotBlank() }
                    .joinToString("\n")
                    .replace(" = ", "="))
            }
        }
    }
}

// Add LICENSE and manifest into the JAR file.
// Manifest also controls Mixin/mod loading on some loaders/versions.
tasks.withType<Jar> {
    from(rootDir.resolve("GPL"))
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))
    manifest {
        attributes(
            "Specification-Title" to "In-Game Account Switcher",
            "Specification-Version" to version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "IAS",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu",
            "MixinConfigs" to "ias.mixins.json"
        )
    }
}

tasks.withType<RemapJarTask> {
    // Output into "build/libs" instead of "versions/<ver>/build/libs".
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile

    // Minify JSON files. (after Fabric Loom processing)
    val minifier = UnsafeUnaryOperator<String> { Gson().fromJson(it, JsonElement::class.java).toString() }
    doLast {
        ZipUtils.transformString(archiveFile.get().asFile.toPath(), mapOf(
            "ias.mixins.json" to minifier,
            "ias.mixins.refmap.json" to minifier,
        ))
    }
}
