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

// Le sample resout le plugin skot par coordonnees Maven : son buildscript classpath n'est pas
// substitue par le projet :plugin local. L'inclure obligerait tout build de la racine a disposer
// de la version publiee -- inexistante sur JitPack, sur un clone neuf et en CI -- alors qu'il
// n'apporte qu'un confort d'IDE. Le sample se construit depuis son propre dossier.
//includeBuild("sample")
