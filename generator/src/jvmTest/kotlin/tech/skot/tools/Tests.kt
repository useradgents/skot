package tech.skot.tools

import tech.skot.tools.generation.*
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class EssaiAbs<B : Any>() {
    fun bidon(binding: B) {}
}

class EssaiImplViewProxy() : EssaiAbs<String>()

class EssaiImpl

interface Cont {
    val essai: EssaiImpl
}

class Tests {
    @Test
    fun essai() {
//        val kclass = Cont::class
//        val essaiType = kclass.members.find { it.name == "essai" }!!.returnType
//
//        println((Class.forName(essaiType.asTypeName().toProxy().canonicalName)!!.kotlin.supertypes.get(0)?.arguments?.get(0)))// .find { it.simpleName == "EssaiAbs" }?.java?.annotatedSuperclass))
//        println(essaiType.asTypeName().binding("coucou"))
    }

    @Test
    fun extractStringPatterns() {
        val chaineTest = "chaîne test %d   et %s   %ser %i"

        val regex = Regex("(%[a-zA-Z])")
        val res =
            regex.findAll(chaineTest).map {
                it.groupValues[1]
            }.joinToString(" ")

        assertEquals(
            "%d %s %s %i",
            res,
        )
    }

    interface TestPrim {
        val prim1: Char
        val prim2: Byte
        val prim3: Short
        val prim4: Int
        val prim5: Float
        val prim6: Double
    }

    @Test
    fun testPrims()  {
        TestPrim::class.ownProperties().forEach {
            println(it.returnType.primitiveDefaultInit())
        }
    }
}
