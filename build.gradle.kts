/*
 * In-Game Account Switcher is a third-party mod for Minecraft Java Edition that
 * allows you to change your logged in account in-game, without restarting it.
 *
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2026 VidTu
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

// This is the main (multi-version loader) buildscript. It is processed by the
// Stonecutter multiple times, for each version and each loader. (compiled once)
// Based on Architectury Loom and processes the preparation/complation/building
// of the most of the mod that is not covered by the Stonecutter or Blossom.
// See "stonecutter.gradle.kts" for the Stonecutter configuration.
// See "settings.gradle.kts" for the Gradle configuration.

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.architectury.loom)
}

// Extract the platform and Minecraft version.
val platform = loom.platform.get().id()!!
// NeoForge 1.20.1 is loosely Forge, but not Forge. It uses ModPlatform.FORGE loom platform
// and Forge packages, but diverges from (can't keep up with) the (Lex/Upstream) MCForge 1.20.1.
val hackyNeoForge = (name == "1.20.1-neoforge")

// Extract versions.
val mc = sc.current
val mcv = mc.version // Literal version. (toString)
val mcp = mc.parsed // Comparable version. (operator overloading)

// Language.
// TODO(VidTu): Revisit after making the decision about 1.16.5/1.17.1 support.
val javaTarget = if (mcp >= "26.1") 25
else if (mcp >= "1.20.6") 21
else if (mcp >= "1.18.2") 17
else if (mcp >= "1.17.1") 16
else 8
val javaVersion = JavaVersion.toVersion(javaTarget)!!
java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    toolchain.languageVersion = JavaLanguageVersion.of(javaTarget)
}

// TODO(VidTu): When all legacy versions are done, the code from the legacy_shared module
// (and assets too) should be just fine to copy into the root code.
evaluationDependsOn(":legacy_shared") // Migration helper.
val legacyShared = project(":legacy_shared") // Migration helper.

// Metadata.
group = "ru.vidtu.ias"
base.archivesName = "IAS"
version = "${version}+${name}"
description = "Allows you to change which account you are signed in to in-game without restarting Minecraft."

// Define Stonecutter preprocessor variables/constants.
sc {
    constants["hacky_neoforge"] = hackyNeoForge
    constants {
        match(platform, "fabric", "forge", "neoforge")
    }
}

// MC 26.1+ is unobfuscated. Fabric's intermediary POM has version 0.0.0 causing
// Gradle metadata mismatch, and only v1 jar exists (no -v2). Generate a local
// identity intermediary with correct POM version and v2 format.
if (mcp >= "26.1") {
    val localMaven = rootDir.resolve(".gradle/local-maven")
    val intermediaryDir = localMaven.resolve("net/fabricmc/intermediary/${mcv}")
    val pomFile = intermediaryDir.resolve("intermediary-${mcv}.pom")
    val jarFile = intermediaryDir.resolve("intermediary-${mcv}-v2.jar")
    if (!jarFile.exists()) {
        intermediaryDir.mkdirs()
        // POM with correct version (Fabric's POM declares 0.0.0).
        pomFile.writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>net.fabricmc</groupId>
              <artifactId>intermediary</artifactId>
              <version>${mcv}</version>
            </project>
        """.trimIndent())
        // Identity v2 tiny mappings JAR (official == intermediary, no entries).
        val tinyContent = "tiny\t2\t0\tofficial\tintermediary\n".toByteArray()
        ZipOutputStream(jarFile.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("mappings/mappings.tiny"))
            zip.write(tinyContent)
            zip.closeEntry()
        }
    }
    repositories {
        maven(localMaven) {
            name = "LocalIntermediary"
            content {
                includeModule("net.fabricmc", "intermediary")
            }
        }
    }
}

// Architectury Loom doesn't support NeoForm spec 6 (26.1+) which changed the
// config.json format (classpath arrays instead of version strings, no mappings).
// Provide a patched NeoForm ZIP that converts spec 6 back to spec 4 format.
if (mcp >= "26.1" && loom.isNeoForge) {
    val localMaven = rootDir.resolve(".gradle/local-maven")
    val neoformVersion = "${mcv}-1"
    val neoformDir = localMaven.resolve("net/neoforged/neoform/$neoformVersion")
    val patchedZip = neoformDir.resolve("neoform-$neoformVersion.zip")

    if (!patchedZip.exists()) {
        neoformDir.mkdirs()
        val neoformUrl = URI("https://maven.neoforged.net/releases/net/neoforged/neoform/$neoformVersion/neoform-$neoformVersion.zip").toURL()
        val originalBytes = neoformUrl.readBytes()

        val entries = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(originalBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entries[entry.name] = if (entry.isDirectory) ByteArray(0) else zis.readBytes()
                entry = zis.nextEntry
            }
        }

        val json = JsonParser.parseString(String(entries["config.json"]!!)).asJsonObject
        // Add missing data.mappings (identity mapping) and mark as official (unobfuscated).
        json.getAsJsonObject("data").addProperty("mappings", "config/joined.tsrg")
        json.addProperty("official", true)
        // Convert functions from spec 6 (classpath array) to spec 4 (version string).
        val functions = json.getAsJsonObject("functions")
        for (key in functions.keySet()) {
            val func = functions.getAsJsonObject(key)
            if (func.has("classpath") && !func.has("version")) {
                val classpath = func.getAsJsonArray("classpath")
                if (classpath.size() > 0) {
                    func.addProperty("version", classpath[0].asString)
                }
                func.remove("classpath")
            }
            func.remove("java_version")
            // Ensure 'repo' exists (spec 4 reads it without null check).
            if (!func.has("repo")) {
                func.addProperty("repo", "https://maven.neoforged.net/releases/")
            }
        }
        // Rename preProcessJar step to "rename" (Loom enqueues "rename" step by name).
        val joinedSteps = json.getAsJsonObject("steps").getAsJsonArray("joined")
        for (i in 0 until joinedSteps.size()) {
            val step = joinedSteps[i].asJsonObject
            if (step.get("type").asString == "preProcessJar") {
                step.addProperty("name", "rename")
            }
        }
        entries["config.json"] = com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json).toByteArray()
        entries["config/joined.tsrg"] = "tsrg2 left right\n".toByteArray()

        ZipOutputStream(patchedZip.outputStream()).use { zos ->
            for ((name, bytes) in entries) {
                zos.putNextEntry(ZipEntry(name))
                if (bytes.isNotEmpty()) zos.write(bytes)
                zos.closeEntry()
            }
        }
        neoformDir.resolve("neoform-$neoformVersion.pom").writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <modelVersion>4.0.0</modelVersion>
              <groupId>net.neoforged</groupId>
              <artifactId>neoform</artifactId>
              <version>$neoformVersion</version>
            </project>
        """.trimIndent())
    }

    repositories {
        exclusiveContent {
            forRepository {
                maven(localMaven) { name = "LocalNeoForm" }
            }
            filter {
                includeModule("net.neoforged", "neoform")
            }
        }
    }

    // Loom's AT tool bundles ASM 9.7 which doesn't support Java 25 (class version 69).
    // Use component metadata rules to upgrade ASM dependencies in the AT tool.
    dependencies {
        components {
            withModule("net.neoforged.accesstransformers:at-cli") {
                allVariants {
                    withDependencies {
                        removeAll { it.group == "org.ow2.asm" }
                        add("org.ow2.asm:asm:9.9.1")
                        add("org.ow2.asm:asm-tree:9.9.1")
                        add("org.ow2.asm:asm-commons:9.9.1")
                    }
                }
            }
            withModule("net.neoforged:accesstransformers") {
                allVariants {
                    withDependencies {
                        removeAll { it.group == "org.ow2.asm" }
                        add("org.ow2.asm:asm:9.9.1")
                        add("org.ow2.asm:asm-tree:9.9.1")
                        add("org.ow2.asm:asm-commons:9.9.1")
                    }
                }
            }
        }
    }
    // Force ASM version via resolution strategy for all configurations.
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.ow2.asm") {
                useVersion("9.9.1")
            }
        }
    }

    // MC 26.1 has no Mojang mappings (unobfuscated). Loom's NeoForge pipeline
    // unconditionally calls mergeMojang() which throws "Failed to find official
    // mojang mappings". Fix by pre-creating a valid mappings-mojang.tiny and
    // cleaning stale lock files so refreshDeps stays false.
    // NOTE: After a clean Loom cache the first build will fail because the mapping
    // directory has not been created yet. Run the build a second time.
    run {
        val loomCacheDir = file("${gradle.gradleUserHomeDir}/caches/fabric-loom")
        // Delete stale lock files that force refreshDeps=true (disowned or dead PID).
        loomCacheDir.listFiles()?.filter {
            it.name.endsWith(".lock") && it.isFile
        }?.forEach {
            val content = it.readText().trim()
            val isStale = content == "disowned" || content.toLongOrNull()?.let { pid ->
                ProcessHandle.of(pid).isEmpty
            } ?: false
            if (isStale) it.delete()
        }
        // Pre-create mappings-mojang.tiny in the mapping directory for this version.
        val versionCacheDir = loomCacheDir.resolve(mcv)
        if (versionCacheDir.isDirectory) {
            versionCacheDir.listFiles()?.filter {
                it.isDirectory && it.name.startsWith("loom.mappings.") && it.name.contains("neoforge")
            }?.forEach { mappingDir ->
                val mojangTiny = mappingDir.resolve("mappings-mojang.tiny")
                val baseTiny = mappingDir.resolve("mappings-base.tiny")
                if (baseTiny.exists() && (!mojangTiny.exists() || mojangTiny.length() == 0L)) {
                    mojangTiny.writeText("tiny\t2\t0\tofficial\tintermediary\tnamed\tmojang\n")
                }
            }
        }
    }
}

loom {
    // Prepare development environment.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))
    silentMojangMappingsLicense()

    // Set up runs.
    runs {
        // Customize the client run.
        named("client") {
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
            // to fix issues with multiplayer testing.
            if (mcp eq "1.16.5") { // TODO(VidTu): Revisit after making the decision about 1.16.5 support.
                vmArgs(
                    "-Dminecraft.api.auth.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.account.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.session.host=http://0.0.0.0:0/",
                    "-Dminecraft.api.services.host=http://0.0.0.0:0/",
                )
            }
        }

        // Remove server run, the mod is client-only.
        remove(findByName("server"))
    }

    // Configure Mixin.
    @Suppress("UnstableApiUsage") // <- Required to configure Mixin.
    mixin {
        // Use direct remapping instead of annotation processor and refmaps.
        useLegacyMixinAp = false
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

// Make the game run with the compatible Java. (e.g,. Java 17 for 1.20.1)
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
        if (mcp eq "1.20.4" || mcp >= "26.1") { // Fix for ModMenu not providing Text Placeholder API.
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
    val minecraftDependencyProperty = findProperty("sc.minecraft-dependency")
    require(minecraftDependencyProperty != mcv) { "Unneeded 'sc.minecraft-dependency' property set to ${minecraftDependencyProperty} in ${project}, it already uses this version." }
    val minecraftDependency = minecraftDependencyProperty ?: mcv
    minecraft("com.mojang:minecraft:${minecraftDependency}")

    // Mappings.
    if (mcp < "26.1") {
        mappings(loom.officialMojangMappings())
    } else {
        mappings(loom.layered {})
    }

    // Force non-vulnerable Log4J, so that vulnerability scanners don't scream loud.
    // It's also cool for our logging config. (see the "dev/log4j2.xml" file)
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
            val neoforge = "${property("sc.neoforge")}"
            require(neoforge.isNotBlank() && neoforge != "[SC]") { "NeoForge (legacy) version is not provided via 'sc.neoforge' in ${project}." }
            val extractedMinecraft = neoforge.substringBefore('-')
            require(mcp eq extractedMinecraft) { "NeoForge (legacy) version '${neoforge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
            "forge"("net.neoforged:forge:${neoforge}")
        } else {
            // Forge.
            val forge = "${property("sc.forge")}"
            require(forge.isNotBlank() && forge != "[SC]") { "Forge version is not provided via 'sc.forge' in ${project}." }
            val extractedMinecraft = forge.substringBefore('-')
            require(mcp eq extractedMinecraft) { "Forge version '${forge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
            "forge"("net.minecraftforge:forge:${forge}")
        }
    } else if (loom.isNeoForge) {
        // NeoForge.
        val neoforge = "${property("sc.neoforge")}"
        require(neoforge.isNotBlank() && neoforge != "[SC]") { "NeoForge version is not provided via 'sc.neoforge' in ${project}." }
        val neoParts = neoforge.substringBefore('-').split('.')
        val extractedMinecraft = if ((neoParts[0].toIntOrNull() ?: 0) >= 22) "${neoParts[0]}.${neoParts[1]}" else "1.${neoParts[0]}.${neoParts[1]}"
        require(mcp eq extractedMinecraft) { "NeoForge version '${neoforge}' provides Minecraft ${extractedMinecraft} in ${project}, but we want ${mcv}." }
        "neoForge"("net.neoforged:neoforge:${neoforge}")
    } else {
        // Fabric Loader.
        modImplementation(libs.fabric.loader)

        // Fabric API. // TODO(VidTu): Modularize.
        val fapi = "${property("sc.fabric-api")}"
        require(fapi.isNotBlank() && fapi != "[SC]") { "Fabric API version is not provided via 'sc.fabric-api' in ${project}." }
        // MC 26.1+ is unobfuscated. Architectury Loom can't remap mods with "official"
        // namespace access wideners (expects "intermediary"). Use implementation() to
        // skip Loom's mod remapping (not needed for unobfuscated MC anyway).
        if (mcp >= "26.1") {
            implementation("net.fabricmc.fabric-api:fabric-api:${fapi}")
        } else {
            modImplementation("net.fabricmc.fabric-api:fabric-api:${fapi}")
        }

        // ModMenu.
        val modmenu = "${property("sc.modmenu")}"
        require(modmenu.isNotBlank() && modmenu != "[SC]") { "ModMenu version is not provided via 'sc.modmenu' in ${project}." }
        // Sometimes, ModMenu is not yet updated for the version. (it almost never updates to snapshots nowadays)
        // So we should depend on it compile-time (it is really an optional dependency for us) to allow both
        // compilation of an optional ModMenu compatibility class (HModMenu.java) and launching the game.
        if ("${findProperty("sc.modmenu.compile-only")}".toBoolean()) {
            if (mcp >= "26.1") {
                compileOnly("com.terraformersmc:modmenu:${modmenu}")
            } else {
                modCompileOnly("com.terraformersmc:modmenu:${modmenu}")
            }
        } else {
            if (mcp >= "26.1") {
                implementation("com.terraformersmc:modmenu:${modmenu}")
            } else {
                modImplementation("com.terraformersmc:modmenu:${modmenu}")
            }
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
        if (mcp >= "1.20.6") {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/mods.toml")
        } else {
            exclude("fabric.mod.json", "quilt.mod.json", "META-INF/neoforge.mods.toml")
        }
    } else {
        exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml")
    }

    // Determine and replace the platform version range requirement.
    val platformRequirement = "${project.property("sc.platform-requirement")}"
    if (loom.isForge || loom.isNeoForge) {
        require(platformRequirement.isNotBlank() && platformRequirement != "[SC]") { "Platform requirement is not provided via 'sc.platform-requirement' in ${project}." }
        inputs.property("platformRequirement", platformRequirement)
    } else {
        require(platformRequirement == "[SC]") { "Platform requirement is provided via 'sc.platform-requirement' in ${project}, but Fabric builds ignore it." }
    }

    // Expand the updater URL for Forge-like loaders.
    if (loom.isNeoForge || hackyNeoForge) {
        inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/main/updater-neoforge.json")
    } else if (loom.isForge) {
        inputs.property("forgeUpdaterUrl", "https://raw.githubusercontent.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/main/updater-forge.json")
    }

    // Expand Minecraft requirement that can be manually overridden for reasons. (e.g., snapshots)
    val minecraftRequirementProperty = findProperty("sc.minecraft-requirement")
    require(minecraftRequirementProperty != mcv) { "Unneeded 'sc.minecraft-requirement' property set to ${minecraftRequirementProperty} in ${project}, it already uses this version." }
    val minecraftRequirement = minecraftRequirementProperty ?: mcv
    inputs.property("minecraft", minecraftRequirement)

    // Expand Mixin Java version. It's pretty much the same Java, except the Forge 1.20.6 edge case,
    // where it must not be JAVA_21 (even tho we're using Java 21), but instead a Java 18 due to Mixin 0.8.5:
    // https://github.com/SpongePowered/Mixin/blob/releases/0.8.5/src/main/java/org/spongepowered/asm/mixin/MixinEnvironment.java#L747
    val mixinJava = if (loom.isForge && mcp eq "1.20.6") 18 else javaTarget
    inputs.property("mixinJava", mixinJava)

    // Expand version and dependencies.
    inputs.property("version", version)
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
    // Add LICENSE, GPL (reference for LICENSE), and NOTICE.
    from(rootDir.resolve("GPL"))
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("NOTICE"))

    // Remove package-info.class, unless package debug is on. (to save space)
    if (!"${findProperty("ru.vidtu.ias.debug.package")}".toBoolean()) {
        exclude("**/package-info.class")
    }

    // Add manifest.
    manifest {
        attributes(
            "Specification-Title" to "In-Game Account Switcher",
            "Specification-Version" to version,
            "Specification-Vendor" to "VidTu",
            "Implementation-Title" to "IAS",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "VidTu",
            "MixinConfigs" to "ias.mixins.json" // Forge and old NeoForge.
        )
    }
}

// Output into "build/libs" instead of "versions/<ver>/build/libs".
tasks.withType<RemapJarTask> {
    destinationDirectory = rootProject.layout.buildDirectory.file("libs").get().asFile
}

// Fix the annotation libs causing "No tests found" Gradle test discovery failure
tasks.withType<Test> {
    failOnNoDiscoveredTests = false
}