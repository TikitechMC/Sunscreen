plugins {
    java
    id("sunscreen-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite:1.13.4")
    testImplementation(libs.bundles.utils)
}

tasks.test {
    useJUnitPlatform()
}
