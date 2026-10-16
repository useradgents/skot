package tech.skot.tools.generation.resources

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import tech.skot.core.view.SKPermission
import tech.skot.tools.generation.AndroidClassNames
import tech.skot.tools.generation.FrameworkClassNames
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType

/**
 * Une forme de membre supportée de l'interface `Permissions` du projet consommateur.
 *
 * Le générateur doit être capable d'implémenter *tous* les membres abstraits de cette interface :
 * tout membre qu'il ne sait pas classer fait échouer la génération
 * (cf. [PermissionsGenerationException]) plutôt que de produire un mock qui ne compile pas.
 */
sealed class SKPermissionMember {
    abstract val name: String

    /** `val x: SKPermission` ou `val x: SKPermission?` */
    data class Single(override val name: String) : SKPermissionMember()

    /** `val x: List<SKPermission>` ou `val x: List<SKPermission>?` */
    data class Multiple(override val name: String) : SKPermissionMember()
}

class PermissionsGenerationException(message: String) : IllegalStateException(message)

private const val SUPPORTED_FORMS = "SKPermission, SKPermission?, List<SKPermission>, List<SKPermission>?"

private fun unsupportedMember(
    declarationFile: String,
    declaration: String,
    form: String,
): Nothing =
    throw PermissionsGenerationException(
        """
        |Can't generate the Permissions implementations: unsupported member shape.
        |  file        : $declarationFile
        |  declaration : $declaration
        |  shape       : $form
        |Supported shapes are: $SUPPORTED_FORMS
        |Any other member must provide a default implementation in the interface (get() = ...),
        |so that generated classes don't have to implement it.
        """.trimMargin(),
    )

private fun KType.isSKPermission() = classifier == SKPermission::class

private fun KType.isListOfSKPermission() =
    classifier == List::class && arguments.singleOrNull()?.type?.isSKPermission() == true

private fun KType.unsupportedShapeDescription(): String =
    when (val classifier = classifier) {
        List::class -> "List of ${arguments.singleOrNull()?.type ?: "*"}, only List<SKPermission> is supported"
        is KClass<*> ->
            if (Collection::class.java.isAssignableFrom(classifier.java) || Map::class.java.isAssignableFrom(classifier.java)) {
                "container type ${classifier.qualifiedName}, only List is supported"
            } else {
                "type $this, which is not a SKPermission"
            }

        else -> "type $this, which is not a SKPermission"
    }

/**
 * Classe tous les membres abstraits de l'interface `Permissions` du projet.
 *
 * On se base sur [KClass.members] — et non sur les seules propriétés déclarées — pour couvrir aussi
 * les membres hérités d'une interface parente, et sur `isAbstract` pour ignorer les membres qui
 * portent déjà une implémentation par défaut (`get() = ...`) et n'ont donc pas à être réimplémentés.
 *
 * Les [SKPermissionMember.Single] sont renvoyés avant les [SKPermissionMember.Multiple] : les listes
 * générées réfèrent les permissions unitaires, qui doivent donc être initialisées avant elles.
 *
 * @param declarationFile chemin du fichier de l'interface, cité en cas d'échec.
 * @throws PermissionsGenerationException si un membre abstrait n'a pas une forme supportée.
 */
fun permissionsMembers(
    interfaceClass: KClass<*>,
    declarationFile: String,
): List<SKPermissionMember> {
    val abstractMembers = interfaceClass.members.filter { it.isAbstract }

    abstractMembers.filterIsInstance<KFunction<*>>().forEach {
        unsupportedMember(
            declarationFile = declarationFile,
            declaration = "fun ${it.name}(...): ${it.returnType}",
            form = "abstract function, only properties are supported",
        )
    }

    val members =
        abstractMembers.filterIsInstance<KProperty<*>>().map { property ->
            val type = property.returnType
            when {
                type.isSKPermission() -> SKPermissionMember.Single(property.name)
                type.isListOfSKPermission() -> SKPermissionMember.Multiple(property.name)
                else ->
                    unsupportedMember(
                        declarationFile = declarationFile,
                        declaration = "val ${property.name}: $type",
                        form = type.unsupportedShapeDescription(),
                    )
            }
        }

    return members.filterIsInstance<SKPermissionMember.Single>() +
        members.filterIsInstance<SKPermissionMember.Multiple>()
}

private fun List<SKPermissionMember>.singleNames() =
    filterIsInstance<SKPermissionMember.Single>().map { it.name }

/**
 * Propriétés du mock jvmTest : chaque permission unitaire devient un [FrameworkClassNames.skPermissionMock]
 * nommé d'après la propriété — y compris pour une permission nullable, une instance de mock étant plus
 * utile qu'un `null` (un test qui a besoin de `null` peut sous-classer le mock). Chaque liste est
 * initialisée avec l'ensemble des permissions unitaires de l'interface, seul défaut déductible.
 */
fun permissionsMockProperties(members: List<SKPermissionMember>): List<PropertySpec> {
    val mockName = FrameworkClassNames.skPermissionMock.simpleName
    return members.map { member ->
        when (member) {
            is SKPermissionMember.Single ->
                PropertySpec.builder(member.name, FrameworkClassNames.skPermissionMock, KModifier.OVERRIDE)
                    .initializer("$mockName(\"${member.name}\")")
                    .build()

            is SKPermissionMember.Multiple ->
                PropertySpec.builder(
                    member.name,
                    LIST.parameterizedBy(FrameworkClassNames.skPermissionMock),
                    KModifier.OVERRIDE,
                )
                    .initializer(members.singleNames().listInitializer())
                    .build()
        }
    }
}

/**
 * Propriétés de l'implémentation Android échafaudée à la première génération : le nom de permission
 * Android reste un `PERMISSION_NAME` à compléter à la main, c'est le principe de cet échafaudage.
 * Seules les permissions unitaires sont échafaudées : la composition des listes relève d'une
 * sémantique métier (`listOfNotNull(coarseLocation, fineLocation)`) que le générateur ne peut pas
 * deviner, et ce fichier appartient au projet dès sa création.
 */
fun permissionsAndroidImplProperties(members: List<SKPermissionMember>): List<PropertySpec> {
    val androidName = FrameworkClassNames.permissionAndroidLegacy.simpleName
    return members.filterIsInstance<SKPermissionMember.Single>().map { member ->
        PropertySpec.builder(member.name, FrameworkClassNames.permissionAndroidLegacy, KModifier.OVERRIDE)
            .initializer(
                CodeBlock.of("$androidName(name = ${AndroidClassNames.manifest.simpleName}.permission.PERMISSION_NAME)"),
            )
            .build()
    }
}

private fun List<String>.listInitializer() =
    if (isEmpty()) {
        CodeBlock.of("emptyList()")
    } else {
        CodeBlock.of("listOf(${joinToString(", ")})")
    }
