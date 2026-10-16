package tech.skot.core.view

open class Icon(val res: Int) : Resource {
    override fun equals(other: Any?): Boolean {
        return other is Icon && other.res == res
    }

    override fun hashCode(): Int {
        return res
    }

}
