val kotestVersion: String by project
val konfVersion: String by project
val mockkVersion: String by project

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

    implementation("io.github.nhubbard:konf:$konfVersion")

    testImplementation(project(":common:tptp-parser"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
