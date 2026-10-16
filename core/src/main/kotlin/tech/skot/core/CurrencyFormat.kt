package tech.skot.core

fun Double.asPriceTwoOrNoDigits(isoCurrency: String) =
    if ((this - toInt()) == 0.0) {
        asPrice(isoCurrency)
    } else {
        asPrice(
            isoCurrency,
            minimumFractionDigits = 2,
        )
    }
