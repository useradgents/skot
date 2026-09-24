package tech.skot.model

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import tech.skot.core.SKLog

abstract class SKNullableDistantDataWithCache<D : Any>(
    protected val name: String,
    dataSerializer: KSerializer<D>,
    protected open val key: String?,
    private val cache: SKPersistor = globalPersistor,
    validity: Long? = null,
    private val fetchData: suspend () -> D?,
) : SKDistantData<D?>(validity, fetchData) {
    private val serializer: KSerializer<Nullable<D>> = Nullable.serializer(dataSerializer)

    override suspend fun newDatedData(): DatedData<D?> {
        // clé capturée avant le fetch : une clé live qui change pendant la requête ne doit pas
        // ranger la donnée sous la nouvelle clé
        val keyOfFetch = completeKey
        val fetchedData = DatedData(fetchData(), currentTimeMillis())
        saveInCache(fetchedData, keyOfFetch)
        return fetchedData
    }

    protected open val completeKey: String? = key

    private val cacheWriter = SKCacheWriter(cache, name, "SKDistantDataWithCache")

    private fun saveInCache(
        newData: DatedData<D?>,
        cacheKey: String? = completeKey,
    ) {
        cacheWriter.write(serializer, Nullable(newData.data), cacheKey, newData.timestamp)
    }

    private val initMutex = Mutex()

    private suspend fun initWithCache() {
        initMutex.withLock {
            if (flow.value == null) {
                val cacheDate = cache.getDateOfData(name, completeKey)

                if (cacheDate != null) {
                    try {
                        flow.value =
                            cache.getData(serializer, name, completeKey)?.let {
                                DatedData(it.value, cacheDate)
                            }
                    } catch (ex: Exception) {
                        SKLog.e(
                            ex,
                            "SKDistantDataWithCache Problème à la récupération du cache de la donnée $name $key",
                        )
                    }
                }
            }
        }
    }

    override suspend fun fallBackValue(): D? {
        if (flow.value == null) {
            initWithCache()
        }
        return super.fallBackValue()
    }

    override suspend fun get(validity: Long?): D? {
        if (flow.value == null) {
            initWithCache()
        }
        return super.get(validity)
    }

    override fun setDatedData(newDatedData: DatedData<D?>) {
        super.setDatedData(newDatedData)
        saveInCache(newDatedData)
    }
}
