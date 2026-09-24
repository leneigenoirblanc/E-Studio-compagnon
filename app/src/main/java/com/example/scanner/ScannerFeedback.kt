package com.example.scanner

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Retour haptique et sonore instantané lors de la lecture d'un code-barres :
 * - Bip de caisse classique via ToneGenerator (TONE_PROP_BEEP)
 * - Vibration haptique courte (50 ms)
 */
class ScannerFeedback(context: Context) {

    private val applicationContext = context.applicationContext
    private var toneGenerator: ToneGenerator? = null

    init {
        runCatching {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        }
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun notifyScanSuccess() {
        // 1. Bip sonore de caisse (70 ms)
        runCatching {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        }

        // 2. Vibration courte de 50 ms
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        }
    }

    fun notifyError() {
        runCatching {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }

    fun playSuccessTone() = notifyScanSuccess()
    fun playErrorTone() = notifyError()

    fun vibrateClick() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        }
    }
}
