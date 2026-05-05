import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import java.nio.file.Files
import java.nio.file.Path

plugins {
    id("java")
    id("net.fabricmc.fabric-loom-remap") version "1.16-SNAPSHOT"
    id("com.gradleup.shadow") version "9.2.2"
    id("sunscreen-publish")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val minecraftVersion = libs.versions.minecraft.get()
val fabricModulesByMinecraft = mapOf(
    "1.21.1" to "0.116.10",
    "1.21.11" to "0.140.2"
)
val fabricModules = fabricModulesByMinecraft[minecraftVersion]
    ?: error("Unsupported minecraft version for split Fabric modules: $minecraftVersion")

repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}

configurations.configureEach {
    if (name.endsWith("runtimeClasspath", ignoreCase = true)) {
        // Minecraft pulls org.lz4 while :api pulls at.yawk.lz4; keep only one provider.
        exclude(group = "org.lz4", module = "lz4-java")
    }
}

loom {
    splitEnvironmentSourceSets()
    accessWidenerPath.set(file("src/main/resources/sunscreen.accesswidener"))

    mods {
        create("sunscreen") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    runs {
        named("client") {
            client()
            ideConfigGenerated(true)
            runDir("run/client")
            programArgs("--username", "TestUser")
            vmArgs(
                "-XX:+AllowEnhancedClassRedefinition",
                "-javaagent:\"C:\\Users\\Justin\\.gradle\\caches\\modules-2\\files-2.1\\net.fabricmc\\sponge-mixin\\0.17.0+mixin.0.8.7\\cf31463202f72d03b0ef1e1e38e8ee71b7faaab6\\sponge-mixin-0.17.0+mixin.0.8.7.jar\""
            )
        }
        named("server") {
            server()
            configName = "Run Fabric Server"
            ideConfigGenerated(true)
            runDir("run/server")
            vmArgs(
                "-XX:+AllowEnhancedClassRedefinition",
                "-javaagent:\"C:\\Users\\Justin\\.gradle\\caches\\modules-2\\files-2.1\\net.fabricmc\\sponge-mixin\\0.17.0+mixin.0.8.7\\cf31463202f72d03b0ef1e1e38e8ee71b7faaab6\\sponge-mixin-0.17.0+mixin.0.8.7.jar\""
            )
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricModules+$minecraftVersion")
    compileOnly("systems.manifold:manifold-preprocessor:+")
    annotationProcessor("systems.manifold:manifold-preprocessor:+")

    implementation(project(":api"))
    implementation(project(":common"))

    implementation("net.kyori:adventure-api:4.25.0")
}

val manifoldMcFlag = when (minecraftVersion) {
    "1.21.1" -> "MC_1_21_1"
    "1.21.11" -> "MC_1_21_11"
    else -> error("Unsupported minecraft version for Manifold preprocessors: $minecraftVersion")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Xplugin:Manifold",
            "-Amanifold.source.files=.java",
            "-A$manifoldMcFlag"
        )
    )
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("shaded")
    // Loom split environments keep client classes/configuration separate.
    from(sourceSets["main"].output)
    from(sourceSets["client"].output)
    configurations = listOf(
        project.configurations.runtimeClasspath.get(),
        project.configurations.getByName("clientRuntimeClasspath")
    )
    mergeServiceFiles()
    relocate("net.jpountz.lz4", "me.combimagnetron.sunscreen.shaded.net.jpountz.lz4")
    dependencies {
        exclude {
            var g = it.moduleGroup
            g != "net.kyori" && g != "me.combimagnetron" && g != "io.github.revxrsal" && g != "team.unnamed" && g != "at.yawk.lz4"
        }
    }
}

tasks.named<RemapJarTask>("remapJar") {
    dependsOn(shadowJar)
    inputFile.set(shadowJar.get().archiveFile)
}

tasks.processResources {
    val version = project.version.toString()
    inputs.property("version", version)
    inputs.property("minecraftVersion", minecraftVersion)
    inputs.property("fabricModules", fabricModules)
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to version,
                "minecraftVersion" to minecraftVersion,
                "fabricModules" to fabricModules
            )
        )
    }
}
