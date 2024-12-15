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

    implementation(project(":common:parser"))
    implementation(platform("org.junit:junit-bom:5.10.0"))
    implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
    implementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
