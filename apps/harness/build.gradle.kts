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

    implementation(project(":parser"))

    implementation(libs.bps.console)

    implementation(libs.bps.console.test)

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
