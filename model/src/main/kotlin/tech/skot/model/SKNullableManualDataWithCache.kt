package tech.skot.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.skot.core.SKLog

@Serializable
@SerialName("Nullable")
data class Nullable<D : Any>(val value: D?)

abstract class SKNullableManualDataWithCache<D : Any>(
    private val name: String,
    dataSerializer: KSerializer<D>,
    private val key: String? = null,
    private val cache: SKPersistor = globalPersistor,
    private val initialDefaultValue: D?,
) : SKData<D?> {
    private val serializer: KSerializer<Nullable<D>> = Nullable.serializer(dataSerializer)

    override val flow: MutableStateFlow<DatedData<D?>?> = MutableStateFlow(null)
    override val defaultValidity = Long.MAX_VALUE
    override val _current: DatedData<D?>?
        get() = flow.value

    private val initMutex = Mutex()

    private suspend fun initWithCache() {
        initMutex.withLock {
            if (flow.value == null) {
                val cacheDate = cache.getDateOfData(name, key)
                if (cacheDate != null) {
                    try {
                        flow.value = cache.getData(serializer, name, key)?.let { DatedData(it.value, cacheDate) }
                    } catch (ex: Exception) {
                        SKLog.e(ex, "SKManualDataWithCache Problème à la récupération du cache de la donnée $name $key")
                    }
                } else {
                    flow.value = DatedData(initialDefaultValue, 0)
                }
            }
        }
    }

    override suspend fun fallBackValue(): D? {
        if (flow.value == null) {
            initWithCache()
        }
        return _current?.data
    }

    override suspend fun get(validity: Long?): D? {
        if (flow.value == null) {
            initWithCache()
        }
        return super.get(validity)
    }

    private val cacheWriter = SKCacheWriter(cache, name, "SKManualDataWithCache")

    fun setValue(newValue: D?) {
        flow.value = DatedData(newValue)
        cacheWriter.write(serializer, Nullable(newValue), key)
    }

    override suspend fun update(): D? {
        if (flow.value == null) {
            initWithCache()
        }
        return _current?.data ?: initialDefaultValue
    }
}
