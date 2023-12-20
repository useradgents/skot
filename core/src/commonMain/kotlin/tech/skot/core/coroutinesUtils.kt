package tech.skot.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private var scope = CoroutineScope(Dispatchers.Main)
@Suppress("unused")
suspend fun <R> atomic(block: suspend CoroutineScope.() -> R): R =
    scope.async(block = block).await()

@Suppress("unused")
suspend fun parrallel(coroutineScope: CoroutineScope, vararg blocks: suspend () -> Unit) {
    val deffereds =
        blocks.map {
            coroutineScope.async {
                it()
            }
        }
    deffereds.forEach { it.await() }
}

suspend fun CoroutineScope.parrallelize(vararg blocks: suspend () -> Unit) {
    parrallel(this, *blocks)
}