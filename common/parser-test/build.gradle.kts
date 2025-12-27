plugins {
    id("shared")
    `java-library`
}

dependencies {

    implementation(project(":parser"))
    implementation(platform(libs.junit.bom))
    implementation(libs.kotest.junit5)
    implementation(libs.kotest.assertions.core)
    implementation(libs.junit.jupiter)
}
