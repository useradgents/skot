package tech.skot.core

fun String.skFormat(vararg values: Any): String = this.format(*values)
