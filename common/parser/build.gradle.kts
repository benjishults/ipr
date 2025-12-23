plugins {
    alias(libs.plugins.kotlin.jvm)
    // FIXME why am I using this?
    kotlin("plugin.allopen") version "2.2.21"
    `java-library`
}

group = "bps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(24)
}

dependencies {

    api(project(":terms"))
    api(project(":formulas"))
    api(project(":common"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockk.jvm)
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
