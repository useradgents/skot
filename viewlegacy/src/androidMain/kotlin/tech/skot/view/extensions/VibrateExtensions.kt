package tech.skot.view.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

fun Context.vibrateTwoLittleTimes() {
    vibrate(listOf(60, 60, 60), listOf(128, 0, 128))
}

@SuppressLint("MissingPermission")
fun Context.vibrate(
    times: List<Long>,
    amplitude: List<Int>,
) {
    val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                times.toLongArray(),
                amplitude.toIntArray(),
                -1,
            ),
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate((listOf(0L) + times).toLongArray(), -1)
    }
}
