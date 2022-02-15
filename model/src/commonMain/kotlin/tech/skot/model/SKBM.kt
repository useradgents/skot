package tech.skot.model

import kotlinx.serialization.KSerializer

abstract class SKBM(val key: String?) {


    open inner class DistantData<D : Any>(
        name: String,
        serializer: KSerializer<D>,
        cache: SKPersistor = globalCache,
        validity: Long? = null,
        fetchData: suspend () -> D
    ) : SKDistantDataWithCache<D>(
        name = name,
        serializer = serializer,
        key = key,
        cache = cache,
        validity = validity,
        fetchData = fetchData
    )

    open inner class DistantDataWithLiveKey<D : Any>(
        name: String,
        serializer: KSerializer<D>,
        cache: SKPersistor = globalCache,
        validity: Long? = null,
        private val liveKey: () -> String,
        fetchData: suspend () -> D
    ) : SKDistantDataWithCacheAndLiveKey<D>(
        name = name,
        serializer = serializer,
        cache = cache,
        validity = validity,
        fixKey = key,
        liveKey = liveKey,
        fetchData = fetchData
    )

    open inner class ManualData<D : Any>(
        name: String,
        serializer: KSerializer<D>,
        cache: SKPersistor = globalCache,
        initialDefaultValue: D
    ) : SKManualDataWithCache<D>(
        name = name,
        serializer = serializer,
        key = key,
        cache = cache,
        initialDefaultValue = initialDefaultValue
    )

    open inner class NullableManualData<D : Any>(
        name: String,
        serializer: KSerializer<D>,
        cache: SKPersistor = globalCache,
        initialDefaultValue: D?
    ) : SKNullableManualDataWithCache<D>(
        name = name,
        dataSerializer = serializer,
        key = key,
        cache = cache,
        initialDefaultValue = initialDefaultValue
    )
}