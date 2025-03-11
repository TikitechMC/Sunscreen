plugins {
    java
    `maven-publish`
}

group = "me.combimagnetron"
version = "1.0-SNAPSHOT"

repositories {
    maven("http://repo.combimagnetron.xyz/releases/") {
        isAllowInsecureProtocol = true
    }
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    implementation("net.kyori:adventure-api:4.14.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.14.0")
    implementation("io.github.jglrxavpok.hephaistos:common:2.6.0")
    implementation("com.typesafe:config:1.4.2")
    compileOnly("me.combimagnetron:Passport:1.0-SNAPSHOT")
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("org.apache.commons:commons-lang3:3.17.0")
    compileOnly("commons-io:commons-io:2.18.0")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.0")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22")
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
}

publishing {
    repositories {
        maven {
            name = "combimagnetron"
            url = uri("http://repo.combimagnetron.xyz/releases/")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
            isAllowInsecureProtocol = true
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "Sunscreen"
            version = project.version.toString()
            from(components["java"])
        }
    }
}