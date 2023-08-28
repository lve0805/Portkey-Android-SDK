package io.aelf.portkey.tools.biometric

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

fun launchBiometricVerify(
    context: Context,
    success: (BiometricPrompt.AuthenticationResult) -> Unit,
    fail: (() -> Unit)? = null
) {
    val biometricPrompt = BiometricPrompt(
        context as FragmentActivity,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                success(result)
            }

            override fun onAuthenticationFailed() {
                fail?.let { it() }
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("BiometricVerify")
        .setSubtitle("Please use your fingerprint or facial recognition to verify")
        .setNegativeButtonText("Cancel")
        .build()

    biometricPrompt.authenticate(promptInfo)
}