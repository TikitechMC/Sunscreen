import org.gradle.accessors.dm.LibrariesForLibs
plugins {
    kotlin("jvm") version "2.2.10"
    `kotlin-dsl`
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    testImplementation(kotlin("test"))
    implementation(libs.build.system.license)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
