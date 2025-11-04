package tech.skot.model

import java.lang.System.currentTimeMillis

data class DatedData<D : Any?>(val data: D, val timestamp: Long = currentTimeMillis())
