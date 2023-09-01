package io.aelf.portkey.entity.social_recovery.stage.verify

import android.content.Context
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.component.recaptcha.GoogleRecaptchaService
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.core.stage.social_recovery.SocialRecoveryStageEnum
import io.aelf.portkey.entity.static.guardian_controller.GuardianController
import io.aelf.portkey.entity.static.guardian_controller.GuardianInfo
import io.aelf.portkey.entity.static.guardian_controller.OutsideStateEnum
import io.aelf.portkey.entity.static.verify_box.useVerifyCodeInputBox
import io.aelf.portkey.entity.static.verify_box.VerifyCodeInputBoxInterface
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.ui.basic.ErrorMsg
import io.aelf.portkey.ui.basic.HugeTitle
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.ui.rich_text.RichText
import io.aelf.portkey.utils.log.GLogger
import io.aelf.utils.AElfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val COUNT_DOWN_TIME_LIMIT = 60


private var sent by mutableStateOf(false)
private var errorMsg by mutableStateOf("")
private var countDown by mutableIntStateOf(-1)
private const val CODE_LENGTH = 6

private var verifyCodeBoxHandler: VerifyCodeInputBoxInterface? = null

private var activeCoroutineCountDown: Job? = null

@Composable
internal fun GuardianPage() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    UseComponentDidMount {
        sent = false
        checkAndSendVerifyCode(scope, context)
    }
    RegisterPageBody()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RegisterPageBody() {
    fun decorateIntroductionText(): String {
        val accountMsg =
            WalletLifecyclePresenter.register?.config?.accountIdentifier
                ?: WalletLifecyclePresenter.login?.accountIdentifier
                ?: "your phone/email"
        return "A 6-digit code " +
                (if (sent) "was sent" else "will be sent") +
                " to #" +
                (accountMsg) +
                "#.\n " +
                (if (sent) "Enter it within 10 minutes." else "Click the button below to send it.")
    }

    val guardianInfo = remember {
        object : GuardianInfo() {}.apply {
            guardianEntity = WalletLifecyclePresenter.activeGuardian
            state = OutsideStateEnum.Register
        }
    }
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        HugeTitle(text = "Verify Code")
        RichText(
            text = decorateIntroductionText(),
            modifier = Modifier
                .width(DynamicWidth(paddingHorizontal = 20))
                .defaultMinSize(minHeight = 44.dp)
                .padding(top = 2.dp, bottom = 40.dp)
        )
        GuardianController(info = guardianInfo, modifier = Modifier.padding(bottom = 40.dp))
        verifyCodeBoxHandler = useVerifyCodeInputBox(
            onTextChange = {
                scope.launch(Dispatchers.IO) {
                    checkInputCode(it, scope, context, keyboard)
                }
            },
            modifier = Modifier
                .height(56.dp)
                .width(DynamicWidth(paddingHorizontal = 20)),
            size = CODE_LENGTH,
            enable = sent
        )
        ErrorMsg(
            text = errorMsg,
            paddingTop = 12,
            paddingBottom = 18,
            alignToCenter = true
        )
        Handler()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Handler() {
    val text = if (!sent) "Send" else if (countDown <= 0) "Resend" else "Resend ($countDown)"
    val enable = !sent || countDown <= 0
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Text(
        text = text,
        modifier = Modifier
            .clickable(enabled = enable) {
                if (enable) {
                    keyboardController?.hide()
                    verifyCodeBoxHandler?.clearInput?.let { it() }
                    errorMsg = ""
                    checkAndSendVerifyCode(scope, context)
                }
            },
        fontSize = 14.sp,
        lineHeight = 22.sp,
        textAlign = TextAlign.Center,
        color = Color(if (enable) 0xFF4285F4 else 0xFF8F949C)
    )
}

private fun startCountDown(scope: CoroutineScope) {
    activeCoroutineCountDown = scope.launch(Dispatchers.IO) {
        countDown = COUNT_DOWN_TIME_LIMIT
        while (countDown > 0) {
            delay(1000)
            countDown--
        }
    }
}

private fun headVerifyCode(code: String, scope: CoroutineScope) {
    scope.launch(Dispatchers.IO) {
        try {
            Loading.showLoading("Checking verify code...")
            val result = WalletLifecyclePresenter.activeGuardian!!.verifyVerificationCode(code)
            if (result) {
                handleVerifySuccess(scope)
            } else {
                Loading.hideLoading()
                errorMsg = "incorrect code"
            }
        } catch (e: Throwable) {
            GLogger.e("Check verify code failed!", AElfException(e))
            errorMsg = "network failure"
            Loading.hideLoading()
        }
    }
}

private fun handleVerifySuccess(scope: CoroutineScope) {
    Loading.hideLoading()
    if (WalletLifecyclePresenter.stageEnum == SocialRecoveryStageEnum.READY_TO_REGISTER) {
        scope.launch(Dispatchers.IO) {
            delay(100)
            Loading.showLoading("Sending request...")
            try {
                val setPin = WalletLifecyclePresenter.register?.afterVerified()
                if (setPin != null) {
                    GLogger.w("sendRegisterRequest success.")
                    WalletLifecyclePresenter.setPin = setPin
                    Loading.hideLoading()
                    return@launch
                }
            } catch (e: Throwable) {
                GLogger.e("sendRegisterRequest failure.", AElfException(e))
            }
            errorMsg = "network failure, please try again later."
            Loading.hideLoading()
        }
    } else {
        // Login step
        WalletLifecyclePresenter.activeGuardian = null
    }
    cleanUp()
}

private fun cleanUp() {
    sent = false
    errorMsg = ""
    activeCoroutineCountDown?.cancel()
    countDown = -1
    activeCoroutineCountDown = null
}

@OptIn(ExperimentalComposeUiApi::class)
internal fun checkInputCode(
    code: String,
    scope: CoroutineScope,
    context: Context,
    keyboardController: SoftwareKeyboardController?
) {
    errorMsg = ""
    if (code.length != CODE_LENGTH) {
        return
    }
    keyboardController?.hide()
    headVerifyCode(code, scope)
}

fun checkAndSendVerifyCode(scope: CoroutineScope, context: Context) {
    fun sendVerificationCode(recaptchaToken: String?) {
        scope.launch(Dispatchers.IO) {
            Loading.showLoading("Sending verify code...")
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
                    startCountDown(this)
                    GLogger.w("sendVerificationCode success.")
                    Loading.hideLoading()
                    return@launch
                }
            } catch (e: Throwable) {
                GLogger.e("sendVerificationCode failure.", AElfException(e))
            }
            Loading.hideLoading()
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
                        GLogger.e("GoogleRecaptchaService failed!")
                        Dialog.show(DialogProps().apply {
                            mainTitle = "Relaunch recaptcha service"
                            subTitle =
                                "Sorry, it seems that we have trouble with the recaptcha service, please click the relaunch button to relaunch later."
                            positiveText = "Relaunch"
                            negativeText = "Cancel"
                            positiveCallback = {
                                checkRecaptchaProcess()
                            }
                        })
                    }

                    override fun onUserCancelled() {
                        GLogger.e("GoogleRecaptchaService cancelled by user.")
                        Dialog.show(DialogProps().apply {
                            mainTitle = "Relaunch recaptcha service"
                            subTitle =
                                "Sorry, we need to pass the reCaptcha test to send verify code, please click the relaunch button to relaunch later."
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