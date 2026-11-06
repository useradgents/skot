package tech.skot.tools

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import tech.skot.model.SKData
import tech.skot.model.SKManualData
import tech.skot.tools.generation.fileClassBuilder
import tech.skot.tools.generation.viewmodel.addModelMockMembers
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * Contrats de modèle couvrant les formes de membres que le générateur de mocks doit savoir
 * traiter. Ils sont compilés dans ce source-set : le mock généré à partir d'eux est ensuite
 * compilé pour la cible JVM avec le classpath du test, où ils sont donc réellement présents.
 */
interface BaseMockMC {
    var onUpdateOneLoading: ((loading: Boolean) -> Unit)?
    var onError: ((error: Exception) -> Unit)?

    fun destroyPresenter()
}

/** Membres hérités redéclarés en `override`, comme `MainScreenMC` de PLDT. */
interface RedeclaringMockMC : BaseMockMC {
    val accountBadge: SKData<String?>

    fun isConnected(): Boolean

    override var onUpdateOneLoading: ((loading: Boolean) -> Unit)?
    override var onError: ((error: Exception) -> Unit)?

    override fun destroyPresenter()
}

/** Membres hérités sans redéclaration. */
interface InheritingMockMC : BaseMockMC {
    fun isConnected(): Boolean
}

/** Propriétés `var` de type `SKManualData` et `SKData`, comme `HomeScreenMC` de PLDT. */
interface ManualDataMockMC {
    var searchData: SKManualData<String?>
    var requiredData: SKManualData<String>
    val readOnlyManualData: SKManualData<String?>
    var mutableData: SKData<String>
}

/** Fonction surchargée héritée, comme `addFilter` de `BaseFilterMC` dans PLDT. */
interface BaseOverloadMockMC {
    suspend fun addFilter(key: String, value: String)

    suspend fun addFilter(key: String, min: Float, max: Float)
}

interface OverloadMockMC : BaseOverloadMockMC {
    fun reset()
}

class ModelMockGenerationTests {
    private val mockClassName = ClassName("tech.skot.tools.generated", "ModelMock")

    @OptIn(ExperimentalStdlibApi::class)
    private fun mockSourceOf(contract: KClass<*>): String =
        mockClassName.fileClassBuilder {
            addSuperinterface(ClassName.bestGuess(contract.qualifiedName!!))
            addModelMockMembers(contract, emptySet())
        }.toString()

    @Test
    fun mockOfContractRedeclaringInheritedMembersCompiles() {
        val source = mockSourceOf(RedeclaringMockMC::class)
        assertContains(source, "override var onUpdateOneLoading: Function1<Boolean, Unit>? = null")
        assertContains(source, "override fun destroyPresenter(): Unit = destroyPresenterMock(Unit)")
        assertCompiles(source)
    }

    @Test
    fun mockOfContractInheritingMembersCompiles() {
        val source = mockSourceOf(InheritingMockMC::class)
        assertContains(source, "override var onError: Function1<Exception, Unit>? = null")
        assertContains(source, "override fun destroyPresenter(): Unit = destroyPresenterMock(Unit)")
        assertCompiles(source)
    }

    @Test
    fun mockOfManualDataVarCompiles() {
        val source = mockSourceOf(ManualDataMockMC::class)
        assertContains(source, "override var searchData: SKManualData<String?> = SKManualData(null)")
        assertContains(source, "override lateinit var requiredData: SKManualData<String>")
        assertContains(source, "override var mutableData: SKData<String> = SKDataMock(\"mutableData\")")
        assertCompiles(source)
    }

    @Test
    fun mockOfOverloadedFunctionsCompiles() {
        val source = mockSourceOf(OverloadMockMC::class)
        assertContains(source, "addFilter1Mock(AddFilter1Call(key, min, max))")
        assertContains(source, "addFilter2Mock(AddFilter2Call(key, value))")
        assertContains(source, "override fun reset(): Unit = resetMock(Unit)")
        assertCompiles(source)
    }

    private fun assertCompiles(mockSource: String) {
        assertEquals(ExitCode.OK, compile(mockSource), "Le mock généré ne compile pas :\n$mockSource")
    }

    /**
     * Compile le mock pour la cible JVM avec le classpath du test — les contrats en viennent — en
     * y ajoutant des déclarations minimales de `SKDataMock`, `SKFunMock` et `SKFunUnitMock`, qui
     * vivent dans le module `viewmodelTests` dont le générateur ne dépend pas.
     */
    private fun compile(mockSource: String): ExitCode {
        val dir = File(System.getProperty("java.io.tmpdir"), "skot-model-mock-${System.nanoTime()}")
        val sources = File(dir, "src").apply { mkdirs() }
        val classes = File(dir, "classes").apply { mkdirs() }
        try {
            File(sources, "ModelMock.kt").writeText(mockSource)
            File(sources, "TestMocks.kt").writeText(
                """
                package tech.skot.core.test

                import tech.skot.model.SKData
                import tech.skot.model.SKManualData

                @Suppress("UNCHECKED_CAST")
                class SKDataMock<D : Any?>(val name: String) : SKData<D> by SKManualData(null as D)

                open class SKFunMock<C : Any, R : Any?>(val name: String) {
                    operator fun invoke(c: C): R = throw NotImplementedError(name)
                }

                class SKFunUnitMock<C : Any>(name: String) : SKFunMock<C, Unit>(name)
                """.trimIndent(),
            )

            return K2JVMCompiler().exec(
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
        } finally {
            dir.deleteRecursively()
        }
    }
}
