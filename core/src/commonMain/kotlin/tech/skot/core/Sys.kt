package tech.skot.core

import kotlin.time.Clock

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()