package tech.skot.model

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.serializer
import org.junit.Assert.assertEquals
import org.junit.Test

class TestSKManualDataWithCacheWriteOrder {
    private fun persistor() =
        AndroidSKPersistor(InstrumentationRegistry.getInstrumentation().context, "testwriteorder", false)
            .apply { clearSync() }

    private fun manual(persistor: SKPersistor) =
        object : SKManualDataWithCache<Int>(
            name = "WRITE_ORDER",
            serializer = Int.serializer(),
            cache = persistor,
            initialDefaultValue = -1,
        ) {}

    // setValue lance chaque mise en cache dans sa propre coroutine : le cache doit tout de même
    // finir sur la dernière valeur écrite, sinon une relecture (redémarrage de l'app) la perd.
    @Test
    fun lastSetValueIsTheOnePersisted() {
        val persistor = persistor()
        runBlocking {
            val writer = manual(persistor)
            repeat(100) { writer.setValue(it) }
            delay(2000)
            assertEquals(99, manual(persistor).get())
        }
    }

    // Le schéma de TestSKNullableManualDataWithCache : deux setValue consécutifs.
    @Test
    fun twoConsecutiveSetValueKeepTheSecond() {
        val persistor = persistor()
        runBlocking {
            val writer = manual(persistor)
            writer.setValue(1)
            writer.setValue(2)
            delay(500)
            assertEquals(2, manual(persistor).get())
        }
    }

    @Test
    fun distantDataTwoConsecutiveSetDatedDataKeepTheSecond() {
        val persistor = persistor()
        runBlocking {
            val distant =
                object : SKDistantDataWithCache<Int>(
                    name = "DISTANT_WRITE_ORDER",
                    serializer = Int.serializer(),
                    key = null,
                    cache = persistor,
                    fetchData = { -1 },
                ) {}
            distant.setDatedData(DatedData(1))
            distant.setDatedData(DatedData(2))
            delay(500)
            assertEquals(2, persistor.getData(Int.serializer(), "DISTANT_WRITE_ORDER", null))
        }
    }

    // La clé live change pendant la requête : la donnée doit rester rangée sous la clé du fetch.
    @Test
    fun liveKeyChangedDuringFetchKeepsTheKeyOfTheFetch() {
        val persistor = persistor()
        var liveKey = "A"
        runBlocking {
            val distant =
                object : SKDistantDataWithCacheAndLiveKey<Int>(
                    name = "LIVE_KEY",
                    serializer = Int.serializer(),
                    cache = persistor,
                    fixKey = "fix",
                    liveKey = { liveKey },
                    fetchData = {
                        liveKey = "B"
                        42
                    },
                ) {}
            distant.update()
            delay(500)
            assertEquals(42, persistor.getData(Int.serializer(), "LIVE_KEY", "fix_A"))
            assertEquals(null, persistor.getData(Int.serializer(), "LIVE_KEY", "fix_B"))
        }
    }

    // Même cas côté mémoire : la donnée reçue appartient à la clé du fetch, un get() sous la
    // nouvelle clé doit refaire la requête au lieu de la servir.
    @Test
    fun liveKeyChangedDuringFetchIsRefetchedForTheNewKey() {
        val persistor = persistor()
        var liveKey = "A"
        var fetchCount = 0
        runBlocking {
            val distant =
                object : SKDistantDataWithCacheAndLiveKey<Int>(
                    name = "LIVE_KEY_MEMORY",
                    serializer = Int.serializer(),
                    cache = persistor,
                    fixKey = "fix",
                    liveKey = { liveKey },
                    fetchData = {
                        fetchCount++
                        if (fetchCount == 1) {
                            liveKey = "B"
                            42
                        } else {
                            43
                        }
                    },
                ) {}
            assertEquals(42, distant.get())
            assertEquals(43, distant.get())
        }
    }
}
