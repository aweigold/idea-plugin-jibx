import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.adamweigold.jibx"
version = "0.3-SNAPSHOT"

intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java"))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jibx:jibx-bind:1.4.2")
    implementation("org.apache.maven:maven-model:3.9.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.google.truth:truth:1.1.3")
}

tasks{

    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    jar {
        val dependencies = configurations
                .runtimeClasspath
                .get()
                .map(::zipTree) // OR .map { zipTree(it) }
        from(dependencies)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("${project.name}.jar")
    }
}

tasks.test {
    useJUnitPlatform()
}