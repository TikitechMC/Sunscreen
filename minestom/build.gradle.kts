dependencies {
    implementation(project(":api"))
    implementation(project(":common"))
    implementation("ch.qos.logback:logback-classic:1.5.29")
    implementation("net.minestom:minestom:2026.02.09-1.21.11")
    compileOnly("commons-io:commons-io:2.18.0")
    implementation("com.google.code.gson:gson:2.13.2")
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, "seconds")
    }
}

plugins {
    application
    id("sunscreen-publish")
}

application {
    mainClass.set("me.combimagnetron.sunscreen.MinestomTest")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}