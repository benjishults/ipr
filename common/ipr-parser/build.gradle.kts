plugins {
    alias(libs.plugins.kotlin.jvm)
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

    api(project(":parser"))

    testImplementation(libs.antlr)
    testImplementation(project(":parser-test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockk.jvm)
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
