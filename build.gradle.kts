/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2024 VidTu
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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

plugins {
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
group = "ru.vidtu.ias"
base.archivesName = "IAS-Root"
description = "This mod allows you to change your logged in account in-game, without restarting Minecraft."

repositories {
    mavenCentral()
}

dependencies {
    // Annotations (Compile)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.error.prone.annotations)

    // Generic (Provided)
    implementation(libs.gson)
    implementation(libs.slf4j)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
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

// These are dependencies required by the uploader.
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.github.mizosoft.methanol:methanol:1.7.0")
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

                    // Dependencies, only Fabric and Quilt have dependencies.
                    if (modrinthLoaders.contains("quilt") || modrinthLoaders.contains("fabric")) {
                        // Fabric has FAPI, Quilt has QSL.
                        // Both have ModMenu.
                        val modrinthDependenciesJson = JsonArray(2)

                        // Fabric API or Quilt Standard Libraries.
                        // FAPI: P7dR8mSH (https://modrinth.com/mod/P7dR8mSH)
                        // QSL: qvIfYCYJ (https://modrinth.com/mod/qvIfYCYJ)
                        val modrinthDependencyApiJson = JsonObject()
                        val id = if (modrinthLoaders.contains("quilt")) "qvIfYCYJ" else "P7dR8mSH"
                        modrinthDependencyApiJson.addProperty("project_id", id)
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
        val retryAtMillis = Date.parse(retryAfter)
        val timeDiffMillis = (retryAtMillis - nowMillis)
        return Duration.ofMillis(timeDiffMillis)
    } catch (t: Throwable) {
        wrapper.addSuppressed(t)
    }

    // Rethrow.
    throw wrapper;
}
