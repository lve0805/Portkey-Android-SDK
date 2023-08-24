package io.aelf.portkey.entity.social_recovery.stage

import android.content.Context
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.aelf.portkey.component.recaptcha.GoogleRecaptchaService
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.utils.log.GLogger
import io.aelf.utils.AElfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var sent by mutableStateOf(false)

@Composable
internal fun RegisterPage() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    UseComponentDidMount {
        sent = false
        checkAndSendVerifyCode(scope, context)
    }
}

fun checkAndSendVerifyCode(scope: CoroutineScope, context: Context) {
    fun sendVerificationCode(recaptchaToken: String?) {
        try {
            val result =
                if (TextUtils.isEmpty(recaptchaToken))
                    WalletLifecyclePresenter.activeGuardian?.sendVerificationCode()
                else
                    WalletLifecyclePresenter.activeGuardian?.sendVerificationCode(
                        recaptchaToken ?: ""
                    )
            if (result == true) {
                sent = true
                GLogger.w("sendVerificationCode success.")
                return
            }
        } catch (e: Throwable) {
            GLogger.e("sendVerificationCode failure.", AElfException(e))
        }
        Dialog.show(DialogProps().apply {
            mainTitle = "Send verify code failed"
            subTitle =
                "Sorry, it seems that we had trouble with the network connection, please try again."
            positiveText = "Resend"
            negativeText = "Cancel"
            positiveCallback = {
                scope.launch(Dispatchers.IO) {
                    sendVerificationCode(recaptchaToken)
                }
            }
        })
    }

    fun checkRecaptchaProcess() {
        scope.launch(Dispatchers.IO) {
            GoogleRecaptchaService.verify(
                context = context,
                callback = object : GoogleRecaptchaService.GoogleRecaptchaCallback {
                    override fun onGoogleRecaptchaSuccess(token: String?) {
                        sendVerificationCode(token)
                    }

                    override fun onGoogleRecaptchaFailed() {
                        Dialog.show(DialogProps().apply {
                            mainTitle = "Relaunch recaptcha service"
                            subTitle =
                                "Sorry, it seems we have trouble with the recaptcha service, please click the send button to relaunch later."
                            positiveText = "Relaunch"
                            negativeText = "Cancel"
                            positiveCallback = {
                                checkRecaptchaProcess()
                            }
                        })
                    }

                    override fun onUserCancelled() {
                        Dialog.show(DialogProps().apply {
                            mainTitle = "Relaunch recaptcha service"
                            subTitle =
                                "Sorry, we need to pass the reCaptcha test to send verify code, please click the send button to relaunch later."
                            positiveText = "Relaunch"
                            negativeText = "Cancel"
                            positiveCallback = {
                                checkRecaptchaProcess()
                            }
                        })
                    }
                })
        }
    }

    scope.launch(Dispatchers.IO) {
        Loading.showLoading()
        val needRecaptcha =
            WalletLifecyclePresenter.activeGuardian?.checkForReCaptcha() ?: false
        if (needRecaptcha) {
            Loading.hideLoading()
            checkRecaptchaProcess()
        } else {
            try {
                sendVerificationCode(null)
            } catch (e: Throwable) {
                GLogger.e("sendVerificationCode failure.", AElfException(e))
                Dialog.show(DialogProps().apply {
                    mainTitle = "Send verify code failed"
                    subTitle =
                        "Sorry, it seems that we had trouble with the network connection, please try again."
                    positiveText = "Resend"
                    negativeText = "Cancel"
                    positiveCallback = {
                        scope.launch(Dispatchers.IO) {
                            sendVerificationCode(null)
                        }
                    }
                })
            }
        }
        Loading.hideLoading()
    }
}