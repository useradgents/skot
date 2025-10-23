package tech.skot.core

@Deprecated(
    message = "Use format(Locale.getDefault(), *values) instead",
    replaceWith = ReplaceWith(
        "this.format(Locale.getDefault(), *values)",
         "java.util.Locale"
    )
)
fun String.skFormat(vararg values: Any): String = this.format(*values)
