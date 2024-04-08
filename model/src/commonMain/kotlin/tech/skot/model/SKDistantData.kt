package tech.skot.model

open class SKDistantData<D : Any?>(validity: Long? = null, private val fetchData: suspend () -> D) :
    SimpleSKData<D>() {
    override suspend fun newDatedData() = DatedData(fetchData(), currentTimeMillis())

    override val defaultValidity = validity ?: super.defaultValidity
}
