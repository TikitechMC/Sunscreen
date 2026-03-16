import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    java
    application
    id("sunscreen-main")
}

group = "me.combimagnetron"
version = "2.0.0-SNAPSHOT"

allprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "sunscreen-main")

    fun libs(): LibrariesForLibs {
        return rootProject.libs
    }

    repositories {
        mavenCentral()
        maven("https://repo.combimagnetron.net/releases")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.nexomc.com/releases")
    }

    dependencies {
        compileOnly(libs().bundles.adventure)
        if (project.name.equals("minestom")) {
            implementation(libs().bundles.utils)
        } else {
            compileOnly(libs().bundles.utils)
        }
        implementation(libs().bundles.minecraft)
        implementation(libs().passport)
    }

}
