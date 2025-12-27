plugins {
    id("shared")
    `java-library`
}

dependencies {

    api(project(":terms"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockk.jvm)
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.junit.jupiter)
}
