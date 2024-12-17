val kotestVersion: String by project
val jacksonVersion: String by project
val konfVersion: String by project
val mockkVersion: String by project
val consoleVersion: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.allopen") version "2.1.0"
    `java-library`
}

group = "bps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
    compilerOptions {
//        freeCompilerArgs.add("-Xcontext-receivers")
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
    }
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
