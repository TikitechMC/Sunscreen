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
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    testImplementation(libs.bundles.utils)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--add-modules=jdk.incubator.vector")
}

tasks.register<JavaExec>("jmh") {
    dependsOn("testClasses")
    mainClass.set("me.combimagnetron.sunscreen.bench.MapEncoderBenchmark")
    classpath = sourceSets["test"].runtimeClasspath
    jvmArgs("--add-modules=jdk.incubator.vector")
}
