plugins {
    id("shared")
    `java-library`
}

dependencies {

    implementation(libs.konf)
    implementation(project(":common"))

    testImplementation(project(":tptp-parser"))
    testImplementation(project(":ipr-parser"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockk.jvm)
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.junit.jupiter)
}
