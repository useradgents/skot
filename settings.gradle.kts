plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "SKotFramework"
rootProject.buildFileName = "build.gradle.kts"
include(":core")
include(":viewcontract")
include(":model")
include(":modelcontract")
include(":viewmodel")
include(":viewlegacy")
include(":plugin")
include(":generator")
include(":viewmodelTests")
include(":viewlegacyTests")

includeBuild("sample")
