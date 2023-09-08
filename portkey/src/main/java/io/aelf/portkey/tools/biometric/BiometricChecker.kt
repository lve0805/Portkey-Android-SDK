package io.aelf.portkey.tools.biometric

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import io.aelf.portkey.utils.log.GLogger

fun launchBiometricVerify(
    context: Context,
    success: (BiometricPrompt.AuthenticationResult) -> Unit,
    fail: (() -> Unit)? = null
) {
    val fragmentActivity = context as? FragmentActivity
    if (fragmentActivity == null) {
        GLogger.e(
            "BiometricVerify failed: If you mean to use biometric, please make sure the context that Compose" +
                    "lives in is a FragmentActivity.\n" +
                    "It's simple: just make your Activity extends from FragmentActivity instead of other Activity classes."
        )
        fail?.let { it() }
        return
    }
    val biometricPrompt = BiometricPrompt(
        fragmentActivity,
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