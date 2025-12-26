plugins {
    id("shared")
    `java-library`
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
