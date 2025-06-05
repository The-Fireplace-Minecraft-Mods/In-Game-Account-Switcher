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

import com.github.mizosoft.methanol.Methanol
import com.github.mizosoft.methanol.MultipartBodyPublisher
import com.github.mizosoft.methanol.MutableRequest
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask
import net.fabricmc.loom.util.ModPlatform
import net.fabricmc.loom.util.ZipUtils
import net.fabricmc.loom.util.ZipUtils.UnsafeUnaryOperator
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

plugins {
    alias(libs.plugins.architectury.loom)
}

// Extract the platform and Minecraft version.
val platform = loom.platform.get()
// NeoForge 1.20.1 is loosely Forge, but not Forge. It uses ModPlatform.FORGE loom platform
// and Forge packages, but diverges from (can't keep up with) the (Lex/Upstream) MCForge 1.20.1.
val hackyNeoForge = (name == "1.20.1-neoforge")
val minecraft = stonecutter.current.version

// Determine and set Java toolchain version.
val javaTarget = if (stonecutter.eval(minecraft, ">=1.20.6")) 21 else 17
val javaVersion = JavaVersion.toVersion(javaTarget)
java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion
java.toolchain.languageVersion = JavaLanguageVersion.of(javaTarget)

group = "ru.vidtu.ias"
base.archivesName = "IAS"
version = "$version+$name"
description = "This mod allows you to change your logged in account in-game, without restarting Minecraft."

// Define Stonecutter preprocessor variables.
stonecutter.const("hackyNeoForge", hackyNeoForge)
ModPlatform.values().forEach {
    stonecutter.const(it.id(), it == platform)
}

// Process the JSON files via Stonecutter.
// This is needed for the Mixin configuration.
stonecutter.allowExtensions("json")

loom {
    // Prepare development environment.
    log4jConfigs.setFrom(rootDir.resolve("dev/log4j2.xml"))
    silentMojangMappingsLicense()

    // Setup JVM args, see that file.
    runs.named("client") {
        // Set up debug VM args.
        vmArgs("@../dev/args.vm.txt")

        // Set the run dir.
        runDir = "../../run"
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
    // Minecraft.
    val minecraftDependency = findProperty("stonecutter.minecraft-dependency") ?: minecraft
    minecraft("com.mojang:minecraft:$minecraftDependency")
    mappings(loom.officialMojangMappings())

    // Loader.
    if (loom.isForge) {
        if (hackyNeoForge) {
            // Legacy NeoForge.
            "forge"("net.neoforged:forge:${property("stonecutter.neoforge")}")
        } else {
            // Forge.
            "forge"("net.minecraftforge:forge:${property("stonecutter.forge")}")
        }
    } else if (loom.isNeoForge) {
        // Forge.
        "neoForge"("net.neoforged:neoforge:${property("stonecutter.neoforge")}")
    } else {
        // Fabric.
        modImplementation(libs.fabric.loader)
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("stonecutter.fabric-api")}")
        modImplementation("com.terraformersmc:modmenu:${property("stonecutter.modmenu")}")
    }
}

// Compile with UTF-8, compatible Java, and with all debug options.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = javaTarget
}

tasks.withType<ProcessResources> {
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

    // Expand version and dependencies.
    val minecraftRequirement = findProperty("stonecutter.minecraft-requirement") ?: minecraft
    inputs.property("version", version)
    inputs.property("minecraft", minecraftRequirement)
    inputs.property("java", javaTarget)
    inputs.property("platform", platform.id())
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
                    .filter { s -> s.isNotBlank() }
                    .joinToString("\n")
                    .replace(" = ", "="))
            }
        }
    }
}

// Reproducible builds.
tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Add LICENSE and manifest into the JAR file.
// Manifest also controls Mixin/mod loading on some loaders/versions.
tasks.withType<Jar> {
    from(rootDir.resolve("LICENSE"))
    from(rootDir.resolve("GPL"))
    from(rootDir.resolve("NOTICE"))
    manifest {
        attributes(
            "Specification-Title" to "IAS",
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

// These are dependencies required by the uploader.
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.github.mizosoft.methanol:methanol:1.8.2")
    }
}

// Register the upload task.
tasks.register("upload") {
    // Wait for build.
    dependsOn("clean", "build")

    // Upload.
    doLast {
        // Wrap to provide better logging.
        try {
            // Begin.
            logger.lifecycle("Publishing...")
            val version = version.toString()
            val modrinthToken = requireNotNull(System.getenv("MODRINTH_TOKEN")) { "No 'MODRINTH_TOKEN' env value has been found." }

            // Create the client.
            val timeout = Duration.ofSeconds(30L)
            val randomSessionId = UUID.randomUUID()
            val userAgent = "IAS-Publish/$version (https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher; $randomSessionId; Gradle/${gradle.gradleVersion}; Java/${Runtime.version()})"
            logger.lifecycle("User Agent: $userAgent")
            val client = Methanol.newBuilder()
                .connectTimeout(timeout)
                .requestTimeout(timeout)
                .readTimeout(timeout)
                .headersTimeout(timeout)
                .userAgent(userAgent)
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NEVER)
                .executor(Runnable::run)
                .build()

            // Iterate over every project.
            for (sub in subprojects) {
                // Wrap to differentiate causes.
                try {
                    // Obtain project's JAR file, skip invalid JARs.
                    val name = sub.name
                    if (name.contains("root", ignoreCase = true)) continue
                    logger.lifecycle("Processing $name...")
                    val subTask = sub.tasks.findByName("remapJar") ?: continue
                    val file = subTask.outputs.files.singleFile

                    // Obtain properties.
                    val displayType = requireNotNull(sub.property("display.type")) { "No 'display.type' is specified for: $name" }.toString()
                    val rawModrinthVersions = requireNotNull(sub.property("modrinth.versions")) { "No 'modrinth.versions' is specified for: $name" }.toString()
                    val rawModrinthLoaders = requireNotNull(sub.property("modrinth.loaders")) { "No 'modrinth.loaders' is specified for: $name" }.toString()
                    val modrinthVersions = rawModrinthVersions.split(',', ignoreCase = true)
                    val modrinthLoaders = rawModrinthLoaders.split(',', ignoreCase = true)

                    // > MODRINTH PART START
                    val modrinthJson = JsonObject()

                    // Project data.
                    modrinthJson.addProperty("name", "IAS $version (for $displayType)")
                    modrinthJson.addProperty("project_id", "cudtvDnd") // IAS: https://modrinth.com/mod/cudtvDnd
                    modrinthJson.addProperty("featured", false)
                    modrinthJson.addProperty("primary_file", file.name)
                    modrinthJson.addProperty("status", "listed")
                    modrinthJson.addProperty("requested_status", "listed")
                    modrinthJson.addProperty("changelog", "- Added full support for Forge/NeoForge/Fabric 1.21.1/1.21.3/1.21.4/1.21.5.\n- Added Forge 1.20.6 support too.\n- Fixed PojavLauncher support. ([#188](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/issues/188))\n- Dropped Quilt support, use Fabric version for Quilt.\n- Removed \"Shutting down IAS...\" screen, it was very buggy.\n- Fixed a bug with singleplayer worlds not shutting down properly. ([#194](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/issues/194)/[#207](https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/issues/207))\n- Added zh_cn (by [mowenxuan](https://github.com/mowenxuan)) and zh_tw (by [yichifauzi](https://github.com/yichifauzi)) translations.\n- Fixed buttons alignment in 1.18.2 and 1.19.2.\n- Some fixes and improvements.\n\nFull changelog: https://github.com/The-Fireplace-Minecraft-Mods/In-Game-Account-Switcher/compare/v9.0.1..v9.0.2")
                    val filePartsJson = JsonArray(1)
                    filePartsJson.add(file.name)
                    modrinthJson.add("file_parts", filePartsJson)

                    // Version data.
                    modrinthJson.addProperty("version_number", version)
                    if (version.contains("snapshot", ignoreCase = true) || version.contains("alpha", ignoreCase = true)) {
                        modrinthJson.addProperty("version_type", "alpha")
                    } else if (version.contains("beta", ignoreCase = true) || version.contains("rc", ignoreCase = true)) {
                        modrinthJson.addProperty("version_type", "beta")
                    } else {
                        modrinthJson.addProperty("version_type", "release")
                    }

                    // Minecraft data.
                    val modrinthVersionsJson = JsonArray(modrinthVersions.size)
                    for (modrinthVersion in modrinthVersions) {
                        modrinthVersionsJson.add(modrinthVersion)
                    }
                    modrinthJson.add("game_versions", modrinthVersionsJson)
                    val modrinthLoadersJson = JsonArray(modrinthLoaders.size)
                    for (modrinthLoader in modrinthLoaders) {
                        modrinthLoadersJson.add(modrinthLoader)
                    }
                    modrinthJson.add("loaders", modrinthLoadersJson)

                    // Dependencies, only Fabric has dependencies.
                    if (modrinthLoaders.contains("quilt") || modrinthLoaders.contains("fabric")) {
                        // Fabric API and Mod Menu.
                        val modrinthDependenciesJson = JsonArray(2)

                        // Fabric API
                        // ID: P7dR8mSH (https://modrinth.com/mod/P7dR8mSH)
                        val modrinthDependencyApiJson = JsonObject()
                        modrinthDependencyApiJson.addProperty("project_id", "P7dR8mSH")
                        modrinthDependencyApiJson.addProperty("dependency_type", "required")
                        modrinthDependenciesJson.add(modrinthDependencyApiJson)

                        // Mod Menu.
                        // ID: mOgUt4GM (https://modrinth.com/mod/mOgUt4GM)
                        val modrinthDependencyModMenuJson = JsonObject()
                        modrinthDependencyModMenuJson.addProperty("project_id", "mOgUt4GM")
                        modrinthDependencyModMenuJson.addProperty("dependency_type", "optional")
                        modrinthDependenciesJson.add(modrinthDependencyModMenuJson)

                        // Flush.
                        modrinthJson.add("dependencies", modrinthDependenciesJson)
                    } else {
                        // Modrinth forces dependencies field.
                        modrinthJson.add("dependencies", JsonArray(0))
                    }

                    // Prepare the request.
                    val modrinthBody = MultipartBodyPublisher.newBuilder()
                        .textPart("data", modrinthJson.toString())
                        .filePart(file.name, file.toPath())
                        .build()
                    val modrinthRequest = MutableRequest.POST("https://api.modrinth.com/v2/version", modrinthBody)
                        .timeout(timeout)
                        .header("User-Agent", userAgent)
                        .header("Authorization", modrinthToken)

                    // Try sending it a few times.
                    for (i in 1..5) {
                        // Send.
                        logger.lifecycle("Sending request... (attempt $i out of 5)")
                        val modrinthResponse = client.send(modrinthRequest, HttpResponse.BodyHandlers.ofString())

                        // Process the response.
                        val code = modrinthResponse.statusCode()

                        // If there's 429 (Too Many Requests) or 503 (Service Unavailable) response,
                        // try to just sleep through rate-limiters, we have 5 attempts.
                        if (code == 429 || code == 503) {
                            logger.warn("Received rate-limiting ($code) response.")
                            val headers = modrinthResponse.headers()
                            val retryAfter = headers.allValues("Retry-After")
                                .map { header -> parseDuration(header) } // Parse.
                                .filter { duration -> !duration.isNegative && !duration.isZero }
                                .maxOfOrNull { duration -> if (duration.toMinutes() > 5) Duration.ofMinutes(5L) else duration }
                            val sleepTime = Objects.requireNonNullElse(retryAfter, Duration.ofSeconds(10L))
                            logger.lifecycle("Retrying in $sleepTime...")
                            Thread.sleep(sleepTime)
                            logger.lifecycle("Retrying...")
                        }

                        // Other errors indicate that something is wrong.
                        if (code != 200) {
                            throw RuntimeException("Received code: $code")
                        }

                        // Success.
                        logger.lifecycle("Success. (version: ${modrinthResponse.body()})")
                        break
                    }

                    // < MODRINTH PART END

                    // Done.
                    logger.lifecycle("Processed $name.")
                } catch (t: Throwable) {
                    // Log and stop.
                    logger.error("Unable to upload the project: {}", sub, t)
                    error(RuntimeException("Unable to upload the project: $sub", t))
                }
            }

            // Close.
            logger.lifecycle("Finalizing...")
            client.close()

            // End.
            logger.lifecycle("Published.")
        } catch (t: Throwable) {
            // Log and stop.
            logger.error("Unable to upload projects", t)
            error(RuntimeException("Unable to upload projects", t))
        }
    }
}

/**
 * Parses the duration of the {@code Retry-After} header.
 * @property retryAfter Header value
 * @return Parsed duration
 */
fun parseDuration(retryAfter: String): Duration {
    // Create wrapper exception for parsing.
    val wrapper = RuntimeException("Unable to parse retry-after: $retryAfter")

    // Parse as double.
    try {
        val seconds = retryAfter.toDouble()
        return seconds.toDuration(DurationUnit.SECONDS).toJavaDuration()
    } catch (t: Throwable) {
        wrapper.addSuppressed(t)
    }

    // Parse as date.
    try {
        // Unfortunately, only legacy Date.parse() method works
        // with all dates returned by HTTP.
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Date
        // https://httpwg.org/specs/rfc9110.html#http.date
        val nowMillis = System.currentTimeMillis()
        @Suppress("DEPRECATION") // <- Sorry.
        val retryAtMillis = Date.parse(retryAfter)
        val timeDiffMillis = (retryAtMillis - nowMillis)
        return Duration.ofMillis(timeDiffMillis)
    } catch (t: Throwable) {
        wrapper.addSuppressed(t)
    }

    // Rethrow.
    throw wrapper;
}
