plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "ipr"
include(
    ":terms",
    ":parser",
    ":tptp-parser",
    ":ipr-parser",
    ":parser-test",
    ":formulas",
    ":equality",
    ":harness",
    ":common",
)
project(":terms").projectDir = file("common/terms")
project(":parser").projectDir = file("common/parser")
project(":tptp-parser").projectDir = file("common/tptp-parser")
project(":ipr-parser").projectDir = file("common/ipr-parser")
project(":parser-test").projectDir = file("common/parser-test")
project(":formulas").projectDir = file("common/formulas")
project(":equality").projectDir = file("common/equality")
project(":harness").projectDir = file("apps/harness")
project(":common").projectDir = file("common/common")
