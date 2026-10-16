package tech.skot.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import tech.skot.core.SKLog
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * Mise en cache asynchrone d'une donnée : pour chaque clé, le cache finit sur la dernière valeur
 * demandée, quel que soit l'ordre dans lequel les coroutines d'écriture s'exécutent.
 * La clé et la valeur sont capturées à l'appel de [write].
 */
@OptIn(ExperimentalAtomicApi::class)
internal class SKCacheWriter(
    private val cache: SKPersistor,
    private val name: String,
    private val logTag: String,
) {
    private val writeCounter = AtomicLong(0L)
    private val writeMutex = Mutex()
    private val lastWrittenVersion = mutableMapOf<String?, Long>()

    fun <D : Any> write(
        serializer: KSerializer<D>,
        data: D,
        key: String?,
        timestamp: Long? = null,
    ) {
        val version = writeCounter.incrementAndFetch()
        CoroutineScope(Dispatchers.Default).launch {
            writeMutex.withLock {
                if (version > (lastWrittenVersion[key] ?: 0L)) {
                    try {
                        if (timestamp == null) {
                            cache.putData(serializer = serializer, name = name, data = data, key = key)
                        } else {
                            cache.putData(serializer, name, data, key, timestamp)
                        }
                    } catch (ex: Exception) {
                        SKLog.e(ex, "$logTag Problème à la mise en cache de la donnée $name $key")
                    }
                    lastWrittenVersion[key] = version
                }
            }
        }
    }
}
