package io.aelf.portkey.entity.social_recovery.stage.pin

import android.content.Context
import android.text.TextUtils
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal
import io.aelf.portkey.internal.tools.GlobalConfig.StorageTags.TAG_PIN
import io.aelf.portkey.sdk.R
import io.aelf.portkey.storage.StorageProvider
import io.aelf.portkey.tools.biometric.launchBiometricVerify
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.tools.friendly.UseComponentWillUnmount
import io.aelf.portkey.ui.basic.ErrorMsg
import io.aelf.portkey.ui.basic.HugeTitle
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.HugeButton
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.utils.log.GLogger
import io.aelf.utils.AElfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal const val PIN_LENGTH = 6
internal const val PIN_BIOMETRIC_KEY = "pin_biometric"

private var repeat by mutableStateOf(false)
private var repeatPinValue by mutableStateOf("")
private var verifyBiometric by mutableStateOf(false)
private var verifyBiometricErrorMsg by mutableStateOf("")
private var type by mutableStateOf(PinPageType.CREATE)

private var errorMsg by mutableStateOf("")
private var pinValue by mutableStateOf("")

private const val CONTROL_DELETE = "^"
private const val CONTROL_BIOMETRIC = "&"

@Synchronized
fun setErrorMsgs(msg: String) {
    errorMsg = msg
}

@Synchronized
fun setPinValues(value: String) {
    pinValue = value
}

@Synchronized
fun setRepeatPinValues(value: String) {
    repeatPinValue = value
}

@Synchronized
fun setPinsValueByAppends(value: String, context: Context) {
    if (repeat) {
        if (repeatPinValue.length >= PIN_LENGTH) {
            return
        }
        repeatPinValue += value
        handlePinValue(CoroutineScope(Dispatchers.IO), context = context)
    } else {
        if (pinValue.length >= PIN_LENGTH) {
            return
        }
        pinValue += value
        handlePinValue(scope = CoroutineScope(Dispatchers.IO), context = context)
    }
}

@Synchronized
fun rmCharFromLast() {
    setErrorMsgs("")
    if (repeat) {
        if (repeatPinValue.isNotEmpty()) {
            repeatPinValue = repeatPinValue.substring(0, repeatPinValue.length - 1)
        }
    } else {
        if (pinValue.isNotEmpty()) {
            pinValue = pinValue.substring(0, pinValue.length - 1)
        }
    }
}

@Composable
internal fun PinPagePresenter(controlType: PinPageType) {
    if (controlType == PinPageType.VERIFY && !EntryBehaviourEntity.ifLockedWalletExists()) {
        return
    } else if (controlType == PinPageType.CREATE && WalletLifecyclePresenter.setPin == null) {
        return
    }

    UseComponentDidMount {
        type = controlType
    }

    val context = LocalContext.current

    UseComponentWillUnmount {
        setPinValues("")
        setErrorMsgs("")
        clearUp()
    }
    if (!verifyBiometric) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            FriendlyWelcomeTitle()
            PinDisplay()
            ErrorMsg(
                text = errorMsg,
                alignToCenter = true,
                paddingTop = 12,
                paddingBottom = if (type == PinPageType.CREATE) 60 else 12
            )
            PinInput()
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 36.dp)
                    .clickable(indication = null, interactionSource = remember {
                        MutableInteractionSource()
                    }) {
                        useBiometric(
                            context = context,
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF7F9FD)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.biometric_raw),
                        contentDescription = "biometric icon",
                        modifier = Modifier.size(100.dp),
                    )
                }
                Text(
                    text = "Enable biometric authentication",
                    fontWeight = FontWeight(500),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF162736),
                    modifier = Modifier.padding(top = 40.dp)
                )
                ErrorMsg(
                    text = verifyBiometricErrorMsg, alignToCenter = true, paddingTop = 24
                )
            }
            LeaveBiometricVerifyButton()
        }
    }
}

@Composable
private fun PinInput() {
    listOf("123", "456", "789", CONTROL_BIOMETRIC + "0" + CONTROL_DELETE).map {
        PinInputLine(
            controlValue = it,
        )
    }
}

private fun useBiometric(
    context: Context,
) {
    CoroutineScope(Dispatchers.Main).launch {
        launchBiometricVerify(
            context = context,
            success = success@{
                if (type == PinPageType.VERIFY) {
                    val extraPinValue = getExtraPinValue()
                    if (TextUtils.isEmpty(extraPinValue)) {
                        setErrorMsgs("biometric verify failed, please try again")
                        return@success
                    } else {
                        checkPinVerifyFromBioPin(extraPinValue)
                    }
                } else {
                    onFinish(it)
                }
            },
            fail = {
                errorMsg = "Biometric verify failed, please try again"
            }
        )
    }
}

@Composable
private fun LeaveBiometricVerifyButton() {
    Column(
        modifier = Modifier
            .width(DynamicWidth(paddingHorizontal = 20))
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        HugeButton(
            config = ButtonConfig().apply {
                text = "Skip"
                onClick = {
                    verifyBiometric = false
                    onFinish()
                }
                bgColor = Color.White
                textColor = Color.Black
                borderWidth=1.dp
            },
        )
    }
}

@Composable
private fun PinInputLine(
    controlValue: String,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .wrapContentHeight()
            .width(274.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        controlValue.forEach {
            PinInputItem(
                controlValue = it.toString(),
            )
        }
    }
}

@Composable
private fun PinInputItem(
    controlValue: String,
) {
    val context = LocalContext.current
    val regex0To9 = "[0-9]".toRegex()
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val underPress by interactionSource.collectIsPressedAsState()

    fun handleClick() {
        if (regex0To9.matches(controlValue)) {
            setPinsValueByAppends(controlValue, context)
        } else if (CONTROL_DELETE == controlValue) {
            rmCharFromLast()
        } else if (CONTROL_BIOMETRIC == controlValue) {
            useBiometric(
                context = context,
            )
        }
    }

    if (CONTROL_BIOMETRIC == controlValue) {
        val containsExtraPin = StorageProvider.getHandler().contains(PIN_BIOMETRIC_KEY)
        val modifier = Modifier
            .size(70.dp)
            .background(Color.Transparent)
        if (type == PinPageType.CREATE || !containsExtraPin) {
            Box(modifier = modifier)
        } else {
            Row(
                modifier = modifier
                    .clickable(
                        indication = null, interactionSource = interactionSource
                    ) {
                        handleClick()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bio_icon),
                    contentDescription = "BIOMETRIC icon",
                    modifier = Modifier
                        .size(70.dp)
                        .clickable(
                            enabled = type == PinPageType.VERIFY,
                            indication = null,
                            interactionSource = interactionSource
                        ) {
                            handleClick()
                        },
                    tint = if (underPress) Color(0xFF8F949C).copy(alpha = 0.8F)
                    else Color(0xFF8F949C)
                )
            }
        }
    } else {
        var modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
        modifier = if (controlValue != CONTROL_DELETE) {
            modifier
                .background(
                    if (underPress) Color(0xFFEDEFF5)
                    else Color(0xFFF7F9FD)
                )
                .clickable(
                    indication = null,
                    interactionSource = interactionSource
                ) {
                    handleClick()
                }
        } else {
            modifier
                .background(Color.Transparent)
                .clickable(
                    indication = null, interactionSource = interactionSource
                ) {
                    handleClick()
                }
        }
        Box(
            modifier = modifier
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (regex0To9.matches(controlValue)) {
                Text(
                    text = controlValue,
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = Color(0xFF162736),
                        lineHeight = 28.sp
                    ),
                )
            } else if (CONTROL_DELETE == controlValue) {
                Icon(
                    painter = painterResource(id = R.drawable.delete_icon),
                    contentDescription = "delete icon",
                    modifier = Modifier.size(24.dp),
                    tint = if (underPress) Color(0xFF8F949C).copy(alpha = 0.8F)
                    else Color(0xFF8F949C)
                )
            }
        }
    }
}

@Composable
private fun FriendlyWelcomeTitle() {
    if (type == PinPageType.CREATE) {
        HugeTitle(text = if (repeat) "Confirm Pin" else "Enter pin to protect your device")
    } else {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.portkey_icon),
                    contentDescription = "portkey wallet's icon",
                    tint = Color(0xFF5B8EF4)
                )
                HugeTitle(text = "Welcome Back !")
            }
        }
    }
}

@Composable
private fun PinDisplay() {
    val length = if (repeat) repeatPinValue.length else pinValue.length
    Row(
        modifier = Modifier
            .padding(top = 40.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until PIN_LENGTH) {
            var modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(16.dp)
                .clip(CircleShape)
            modifier = if (i < length) {
                modifier.background(Color(0xFF162736))
            } else {
                modifier
                    .border(1.dp, Color(0xFFEDEFF5), CircleShape)
                    .background(Color(0xFFFCFCFF))
            }
            Box(
                modifier = modifier
            )
        }
    }
}

private fun handlePinValue(
    scope: CoroutineScope,
    context: Context
) {
    setErrorMsgs("")
    if (repeat) {
        if (repeatPinValue.length != PIN_LENGTH) {
            return
        }
    } else {
        if (pinValue.length != PIN_LENGTH) {
            return
        }
    }
    checkPin(scope, context)
}

private fun checkPin(
    scope: CoroutineScope,
    context: Context
) {
    scope.launch(Dispatchers.IO) {
        if (type == PinPageType.CREATE) {
            checkPinCreate(context = context)
        } else {
            checkPinVerify()
        }
    }
}

private fun checkPinCreate(context: Context) {
    val setPin = WalletLifecyclePresenter.setPin ?: return
    if (repeat) {
        if (repeatPinValue != pinValue) {
            clearAndReportErrRepeat("pin not match")
            return
        }
    } else {
        val result = setPin.isValidPin(pinValue)
        if (!result) {
            clearAndReportErr("invalid pin")
            return
        }
        repeat = true
        return
    }
    verifyBiometric = true
    useBiometric(context = context)
}

private fun onFinish(biometric: BiometricPrompt.AuthenticationResult? = null) {
    CoroutineScope(Dispatchers.IO).launch {
        val setPin = WalletLifecyclePresenter.setPin ?: return@launch
        if (!setPin.isValidPin(pinValue) || pinValue != repeatPinValue) {
            return@launch
        }
        Loading.showLoading("Creating Wallet...")
        try {
            val wallet = setPin.lockAndGetWallet(pinValue)
            Loading.hideLoading()
            if (wallet != null) {
                if (biometric != null) {
                    extraPinValueStorage()
                }
                WalletLifecyclePresenter.wallet = wallet
                clearUp()
                SocialRecoveryModal.onSuccess()
                return@launch
            }
        } catch (e: Throwable) {
            GLogger.e("set pin process failed:", AElfException(e))
            Loading.hideLoading()
        }
        Dialog.show(
            DialogProps().apply {
                mainTitle = "Network failure"
                subTitle = "Create wallet failed, please try again."
                positiveText = "Try again"
                negativeText = "Cancel"
                positiveCallback = {
                    onFinish(biometric)
                }
            }
        )

    }
}


private fun extraPinValueStorage() {
    val handler = StorageProvider.getHandler()
    handler.putValue(PIN_BIOMETRIC_KEY, "verified")
}

private fun getExtraPinValue(): String {
    val handler = StorageProvider.getHandler()
    if (handler.headValue(PIN_BIOMETRIC_KEY, "verified")) {
        return handler.getValue(TAG_PIN)
    }
    return ""
}

private fun checkPinVerify() {
    val unlock = WalletLifecyclePresenter.unlock ?: return
    val result = unlock.checkPin(pinValue)
    if (!result) {
        clearAndReportErr("incorrect pin")
        return
    }
    WalletLifecyclePresenter.wallet = unlock.unlockAndBuildWallet(pinValue)
    SocialRecoveryModal.onSuccess()
}

private fun checkPinVerifyFromBioPin(pin: String) {
    val unlock = WalletLifecyclePresenter.unlock ?: return
    val result = unlock.checkPin(pin)
    if (!result) {
        setErrorMsgs("internal err")
        return
    }
    WalletLifecyclePresenter.wallet = unlock.unlockAndBuildWallet(pin)
    SocialRecoveryModal.onSuccess()
}

private fun clearAndReportErrRepeat(
    errMsg: String
) {
    setRepeatPinValues("")
    setErrorMsgs(errMsg)
    return
}

private fun clearAndReportErr(errMsg: String) {
    setPinValues("")
    setErrorMsgs(errMsg)
    return
}

private fun clearUp() {
    pinValue = ""
    errorMsg = ""
    type = PinPageType.CREATE
    repeat = false
    repeatPinValue = ""
    verifyBiometric = false
    verifyBiometricErrorMsg = ""
}

internal enum class PinPageType {
    CREATE,
    VERIFY
}
