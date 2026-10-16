package tech.skot.viewmodel

import android.telephony.PhoneNumberUtils

fun String?.formatPhoneNumber(): String? {
    return this?.let { PhoneNumberUtils.formatNumber(it, "FR") ?: this }
}
