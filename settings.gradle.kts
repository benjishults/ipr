plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "ipr"
include(
    "common:terms",
    "common:parser",
    "common:tptp-parser",
    "common:parser-test",
    "common:formulas",
    "common:equality",
    "apps:harness",
)
