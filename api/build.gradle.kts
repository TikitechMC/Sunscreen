plugins {
    java
    id("sunscreen-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("io.netty:netty-buffer:4.1.115.Final")
    implementation("at.yawk.lz4:lz4-java:1.10.4")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-suite:1.13.4")
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    testImplementation(libs.bundles.utils)
}

tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
}

tasks.register<JavaExec>("jmh") {
    dependsOn("testClasses")
    mainClass.set("me.combimagnetron.sunscreen.bench.MapEncoderBenchmark")
    classpath = sourceSets["test"].runtimeClasspath
}
