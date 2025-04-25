/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
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
import net.fabricmc.loom.util.ModPlatform
import net.fabricmc.loom.util.ZipUtils
import net.fabricmc.loom.util.ZipUtils.UnsafeUnaryOperator

plugins {
    alias(libs.plugins.architectury.loom)
}

val loomPlatform = loom.platform.get()
val legacyNeoForge = loom.isForge && name.contains(ModPlatform.NEOFORGE.id())
val mcVersion = stonecutter.current.version

val javaMajor = if (stonecutter.eval(mcVersion, ">=1.20.6")) 21
else if (stonecutter.eval(mcVersion, ">=1.18.2")) 17
else if (stonecutter.eval(mcVersion, ">=1.17.1")) 16
else 8
val javaVersion = JavaVersion.toVersion(javaMajor)
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion
java.toolchain.languageVersion = JavaLanguageVersion.of(javaMajor)

val versionSuffix = if (legacyNeoForge) ModPlatform.NEOFORGE.id()!! else loomPlatform.id()!!
group = "ru.vidtu.ias"
base.archivesName = "IAS"
version = "$version+$mcVersion-$versionSuffix"
description = "This mod allows you to change your logged in account in-game, without restarting Minecraft."

stonecutter.const("legacyNeoForge", legacyNeoForge)
ModPlatform.values().forEach {
    stonecutter.const(it.id(), it == loomPlatform)
}

loom {
    log4jConfigs.setFrom(rootDir.resolve("log4j2.xml"))
    silentMojangMappingsLicense()
    runs.named("client") {
        vmArgs(
            // Allow JVM without hotswap to work.
            "-XX:+IgnoreUnrecognizedVMOptions",

            // Set up RAM.
            "-Xmx2G",

            // Force UNIX newlines.
            "-Dline.separator=\n",

            // Debug arguments.
            "-ea",
            "-esa",
            "-Dmixin.debug=true",
            "-Dmixin.debug.strict.unique=true",
            "-Dmixin.checks=true",
            "-Dio.netty.tryReflectionSetAccessible=true",
            "-Dio.netty.leakDetection.level=PARANOID",

            // Allow hot swapping on supported JVM.
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:+AllowRedefinitionToAddDeleteMethods",
            "-XX:HotswapAgent=fatjar",
            "-Dfabric.debug.disableClassPathIsolation=true",

            // Open modules for Netty.
            "--add-opens",
            "java.base/java.nio=ALL-UNNAMED",
            "--add-opens",
            "java.base/jdk.internal.misc=ALL-UNNAMED"
        )
    }
    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp = true
        defaultRefmapName = "ias.mixins.refmap.json"
    }
    if (loom.isForge) {
        forge {
            mixinConfigs("ias.mixins.json")
        }
    } else if (loom.isNeoForge) {
        neoForge {}
    }
}

repositories {
    mavenCentral()
    if (loom.isForge) {
        if (legacyNeoForge) {
            maven("https://maven.neoforged.net/releases/") // Neo. (Legacy)
        }
        maven("https://maven.minecraftforge.net/") // Forge.
    } else if (loom.isNeoForge) {
        maven("https://maven.neoforged.net/releases/") // Neo.
    } else {
        maven("https://maven.fabricmc.net/") // Fabric.
        maven("https://maven.terraformersmc.com/releases/") // ModMenu.
        if (mcVersion == "1.20.4") { // Fix for ModMenu not shading Text Placeholder API.
            maven("https://maven.nucleoid.xyz/") // ModMenu. (Text Placeholder API)
        }
    }
}

dependencies {
    // Annotations
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)

    // Minecraft
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    // Loader.
    if (loom.isForge) {
        if (legacyNeoForge) {
            // Legacy NeoForge
            "forge"("net.neoforged:forge:${property("stonecutter.neo")}")
        } else {
            // Forge
            "forge"("net.minecraftforge:forge:${property("stonecutter.forge")}")
        }
    } else if (loom.isNeoForge) {
        // Forge
        "neoForge"("net.neoforged:neoforge:${property("stonecutter.neo")}")
    } else {
        // Fabric
        modImplementation(libs.fabric.loader)
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("stonecutter.fabric-api")}")
        modImplementation("com.terraformersmc:modmenu:${property("stonecutter.modmenu")}")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    if (javaVersion.isJava9Compatible) {
        options.release = javaMajor
    }
}

tasks.withType<ProcessResources> {
    if (loom.isForge) {
        exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
    } else if (loom.isNeoForge) {
        if (stonecutter.eval(mcVersion, ">=1.20.6")) {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")
        } else {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
        }
    } else {
        exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
    }
    inputs.property("version", version)
    inputs.property("minecraft", mcVersion)
    inputs.property("java", javaMajor)
    inputs.property("platform", loomPlatform.id())
    filesMatching(listOf("fabric.mod.json", "quilt.mod.json", "ias.mixins.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(inputs.properties)
    }
    val files = fileTree(outputs.files.asPath)
    doLast {
        val jsonAlike = Regex("^.*\\.(?:json|mcmeta)$", RegexOption.IGNORE_CASE)
        files.forEach {
            if (it.name.matches(jsonAlike)) {
                it.writeText(Gson().fromJson(it.readText(), JsonElement::class.java).toString())
            } else if (it.name.endsWith(".toml", ignoreCase = true)) {
                it.writeText(it.readLines()
                    .filter { s -> s.isNotBlank() }
                    .joinToString("\n")
                    .replace(" = ", "="))
            }
        }
    }
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("GPL"))
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
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
    val minifier = UnsafeUnaryOperator<String> { Gson().fromJson(it, JsonElement::class.java).toString() }
    doLast {
        ZipUtils.transformString(archiveFile.get().asFile.toPath(), mapOf(
            "ias.mixins.json" to minifier,
            "ias.mixins.refmap.json" to minifier,
        ))
    }
}
