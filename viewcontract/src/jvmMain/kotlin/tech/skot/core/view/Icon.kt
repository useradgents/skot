@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core.view

actual open class Icon(val res:Int) : Resource {
    override fun equals(other: Any?): Boolean {
        return other is Icon && other.res == res
    }
}