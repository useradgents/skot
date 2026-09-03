package tech.skot.tools.generation.resources

import tech.skot.tools.generation.*

@ExperimentalStdlibApi
fun Generator.generatePermissions() {
    val permissionsMembers =
        if (!permissionsInterface.existsCommonInModule(modules.viewcontract)) {
            permissionsInterface.fileInterfaceBuilder {
            }.writeTo(commonSources(modules.viewcontract))
            emptyList()
        } else {
            permissionsMembers(
                interfaceClass = Class.forName(permissionsInterface.canonicalName).kotlin,
                declarationFile =
                    commonSources(modules.viewcontract)
                        .resolve(permissionsInterface.packageName.packageToPathFragment())
                        .resolve("${permissionsInterface.simpleName}.kt")
                        .toString(),
            )
        }

    if (!permissionsImpl.existsAndroidInModule(modules.view)) {
        permissionsImpl.fileClassBuilder(
            listOf(AndroidClassNames.manifest),
        ) {
            addSuperinterface(permissionsInterface)
            addProperties(permissionsAndroidImplProperties(permissionsMembers))
        }.writeTo(androidSources(feature ?: modules.view))
    }

    println("generate Permissions jvm mock .........")
    permissionsMock.fileClassBuilder {
        addSuperinterface(permissionsInterface)
        addProperties(permissionsMockProperties(permissionsMembers))
    }
        .writeTo(generatedJvmTestSources(feature ?: modules.viewmodel))
}
