val kotestVersion: String by project
val jacksonVersion: String by project
val konfVersion: String by project
val mockkVersion: String by project
val consoleVersion: String by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "bps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/benjishults/console")
        credentials {
            username = providers
                .gradleProperty("github.actor")
                .getOrElse(System.getenv("GITHUB_ACTOR"))
            password = providers
                .gradleProperty("github.token")
                .getOrElse(System.getenv("GITHUB_TOKEN"))
        }
    }
}

kotlin {
    jvmToolchain(24)
}
dependencies {

    implementation(project(":common:parser"))

    implementation("io.github.benjishults:console:5.2.0")
    implementation("io.github.benjishults:console-test:5.2.0")

    testImplementation("org.antlr:antlr4:4.13.2")
    testImplementation(project(":common:parser-test"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
