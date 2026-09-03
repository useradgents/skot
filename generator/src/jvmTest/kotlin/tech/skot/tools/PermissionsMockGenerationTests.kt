package tech.skot.tools

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import tech.skot.core.view.SKPermission
import tech.skot.tools.generation.fileClassBuilder
import tech.skot.tools.generation.resources.PermissionsGenerationException
import tech.skot.tools.generation.resources.SKPermissionMember
import tech.skot.tools.generation.resources.permissionsMembers
import tech.skot.tools.generation.resources.permissionsMockProperties
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Interfaces de permissions couvrant les formes de membres que le générateur doit savoir traiter.
 * Elles sont compilées dans ce source-set : le mock généré à partir d'elles est ensuite compilé
 * pour la cible JVM avec le classpath du test, où elles sont donc réellement présentes.
 */
interface SimplePermissionsContract {
    val camera: SKPermission
    val coarseLocation: SKPermission
    val fineLocation: SKPermission
}

interface BasePermissionsContract {
    val inherited: SKPermission
}

interface FullPermissionsContract : BasePermissionsContract {
    val camera: SKPermission
    val backgroundLocation: SKPermission?
    val locations: List<SKPermission>
    val optionalLocations: List<SKPermission>?

    /** Membre non abstrait : le mock ne doit pas le réimplémenter. */
    val firstLocation: SKPermission get() = locations.first()
}

interface SetPermissionsContract {
    val camera: SKPermission
    val locations: Set<SKPermission>
}

interface StringPermissionsContract {
    val camera: SKPermission
    val label: String
}

interface FunPermissionsContract {
    val camera: SKPermission

    fun locationOf(id: String): SKPermission
}

class PermissionsMockGenerationTests {
    private val mockClassName = ClassName("tech.skot.tools.generated", "PermissionsMock")

    private fun mockSourceOf(contract: KClass<*>): String {
        val members = permissionsMembers(contract, "src/commonMain/kotlin/${contract.simpleName}.kt")
        return mockClassName.fileClassBuilder {
            addSuperinterface(ClassName.bestGuess(contract.qualifiedName!!))
            addProperties(permissionsMockProperties(members))
        }.toString()
    }

    /**
     * Non-régression : une interface dont tous les membres sont de type simple produit exactement
     * le mock d'avant le support des formes nullable et List.
     */
    @Test
    fun simpleContractMockIsUnchanged() {
        assertEquals(
            """
            package tech.skot.tools.generated

            import tech.skot.core.view.SKPermissionMock
            import tech.skot.tools.SimplePermissionsContract

            public class PermissionsMock : SimplePermissionsContract {
                override val camera: SKPermissionMock = SKPermissionMock("camera")

                override val coarseLocation: SKPermissionMock = SKPermissionMock("coarseLocation")

                override val fineLocation: SKPermissionMock = SKPermissionMock("fineLocation")
            }

            """.trimIndent(),
            mockSourceOf(SimplePermissionsContract::class),
        )
    }

    /** Le mock d'une interface à membres nullable, List et hérités compile pour la cible JVM. */
    @Test
    fun fullContractMockCompiles() {
        val source = mockSourceOf(FullPermissionsContract::class)
        assertContains(source, """override val backgroundLocation: SKPermissionMock = SKPermissionMock("backgroundLocation")""")
        assertContains(source, "override val locations: List<SKPermissionMock> = listOf(")
        assertCompiles(source)
    }

    /** Non-régression du cas de base : le mock d'une interface simple compile aussi. */
    @Test
    fun simpleContractMockCompiles() {
        assertCompiles(mockSourceOf(SimplePermissionsContract::class))
    }

    /**
     * Garde-fou du harnais : le mock incomplet que produisait le générateur avant ce correctif —
     * les membres nullable et List omis — doit bien être rejeté par la compilation, sinon les tests
     * ci-dessus ne prouveraient rien.
     */
    @Test
    fun incompleteMockDoesNotCompile() {
        val members = permissionsMembers(FullPermissionsContract::class, "Full.kt")
            .filterIsInstance<SKPermissionMember.Single>()
            .filter { it.name == "camera" }
        val incomplete = mockClassName.fileClassBuilder {
            addSuperinterface(ClassName.bestGuess(FullPermissionsContract::class.qualifiedName!!))
            addProperties(permissionsMockProperties(members))
        }.toString()
        assertEquals(ExitCode.COMPILATION_ERROR, compile(incomplete))
    }

    @Test
    fun unsupportedContainerFailsGeneration() {
        val exception = assertFailsWith<PermissionsGenerationException> { mockSourceOf(SetPermissionsContract::class) }
        assertContains(exception.message!!, "SetPermissionsContract.kt")
        assertContains(exception.message!!, "val locations: kotlin.collections.Set<tech.skot.core.view.SKPermission>")
        assertContains(exception.message!!, "only List is supported")
    }

    @Test
    fun unsupportedTypeFailsGeneration() {
        val exception = assertFailsWith<PermissionsGenerationException> { mockSourceOf(StringPermissionsContract::class) }
        assertContains(exception.message!!, "StringPermissionsContract.kt")
        assertContains(exception.message!!, "val label: kotlin.String")
        assertContains(exception.message!!, "not a SKPermission")
    }

    @Test
    fun abstractFunctionFailsGeneration() {
        val exception = assertFailsWith<PermissionsGenerationException> { mockSourceOf(FunPermissionsContract::class) }
        assertContains(exception.message!!, "FunPermissionsContract.kt")
        assertContains(exception.message!!, "fun locationOf(...): tech.skot.core.view.SKPermission")
        assertContains(exception.message!!, "only properties are supported")
    }

    /**
     * Compile le mock pour la cible JVM avec le classpath du test — les interfaces de contrat en
     * viennent — en y ajoutant une déclaration de `SKPermissionMock`, qui vit dans le module
     * `viewmodelTests` dont le générateur ne dépend pas.
     */
    private fun assertCompiles(mockSource: String) {
        assertEquals(ExitCode.OK, compile(mockSource), "Le mock généré ne compile pas :\n$mockSource")
    }

    private fun compile(mockSource: String): ExitCode {
        val dir = File(System.getProperty("java.io.tmpdir"), "skot-permissions-mock-${System.nanoTime()}")
        val sources = File(dir, "src").apply { mkdirs() }
        val classes = File(dir, "classes").apply { mkdirs() }
        try {
            File(sources, "PermissionsMock.kt").writeText(mockSource)
            File(sources, "SKPermissionMock.kt").writeText(
                """
                package tech.skot.core.view

                class SKPermissionMock(val name: String) : SKPermission
                """.trimIndent(),
            )

            val exitCode =
                K2JVMCompiler().exec(
                    PrintingMessageCollector(System.out, MessageRenderer.PLAIN_RELATIVE_PATHS, false),
                    Services.EMPTY,
                    K2JVMCompilerArguments().apply {
                        freeArgs = listOf(sources.absolutePath)
                        destination = classes.absolutePath
                        classpath = System.getProperty("java.class.path")
                        noStdlib = true
                        noReflect = true
                    },
                )

            return exitCode
        } finally {
            dir.deleteRecursively()
        }
    }
}
