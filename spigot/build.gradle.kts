import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
    //id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

repositories {
    mavenCentral()
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.nexomc.com/releases")
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Dcom.mojang.eula.agree=true", "-Dfile.encoding=UTF-8", "--add-modules=jdk.incubator.vector")
        downloadPlugins {
            //github("retrooper", "packetevents", "v2.11.1", "packetevents-spigot-2.11.1.jar")
            hangar("PlaceholderAPI", "2.11.6")
        }
        serverJar(kotlin.io.path.Path("paper-1.21.11-127.jar").toFile())
    }

    build {
        dependsOn( "shadowJar")
    }

    shadowJar {
        archiveBaseName.set("Sunscreen")
        archiveClassifier.set(null)
        archiveVersion.set(this.project.version.toString())
        configurations = listOf(project.configurations.runtimeClasspath.get())
        dependencies {
            exclude(dependency("com.google.guava:guava:31.1-jre"))
            exclude(dependency("org.apache.commons:commons-lang3:3.17.0"))
            exclude(dependency("commons-io:commons-io:2.18.0"))
            exclude(dependency("com.github.ben-manes.caffeine:caffeine:3.2.0"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:1.7.22"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22"))
        }
        relocate("io.github.retrooper.packetevents", "me.combimagnetron.shaded.packetevents.impl")
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.AZUL
        languageVersion = JavaLanguageVersion.of(25)
    }
    //jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

val betterHudVersion = "1.12.2"
val adventureVersion = "4.20.0"

fun libs(): LibrariesForLibs {
    return rootProject.libs
}

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))
    implementation(libs().packetevents)
    implementation(libs().lamp.paper)
    implementation(libs().lamp.brigadier)
    compileOnly(libs().paper)
    library(libs().bundles.utils)
}

bukkit {
    name = "Sunscreen"
    main = "me.combimagnetron.sunscreen.SunscreenPlugin"
    apiVersion = "1.21.11"
    version = project.version.toString()
    authors = listOf("Combimagnetron")
    description = "Create UIs like never seen before, all from within the game!"
    website = "https://combimagnetron.me"
}