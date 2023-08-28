package io.aelf.portkey.entity.social_recovery.stage.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal
import io.aelf.portkey.sdk.R
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.tools.friendly.UseState
import io.aelf.portkey.ui.basic.ErrorMsg
import io.aelf.portkey.ui.basic.HugeTitle
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.utils.log.GLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal const val PIN_LENGTH = 6

private var repeat by mutableStateOf(false)
private var repeatPinValue by mutableStateOf("")

private const val CONTROL_DELETE = "^"
private const val CONTROL_BIOMETRIC = "&"

@Composable
internal fun PinPagePresenter(type: PinPageType) {
    if (type == PinPageType.VERIFY && !EntryBehaviourEntity.ifLockedWalletExists()) {
        return
    } else if (type == PinPageType.CREATE && WalletLifecyclePresenter.setPin == null) {
        return
    }
    var errorMsg by UseState(initValue = "")
    var pinValue by UseState(initValue = "")

    GLogger.t("pin value is: $pinValue")

    fun setErrorMsg(msg: String) {
        errorMsg = msg
    }

    fun setPinValue(value: String) {
        pinValue = value
    }

    fun setPinsValueByAppend(value: String) {
        if (repeat) {
            if (repeatPinValue.length >= PIN_LENGTH) {
                repeatPinValue = repeatPinValue.substring(0, PIN_LENGTH - 1)
                return
            }
            GLogger.e("1")
            repeatPinValue += value
            handlePinValue(
                pinValue,
                ::setErrorMsg,
                ::setPinValue,
                CoroutineScope(Dispatchers.Main),
                type
            )
        } else {
            if (pinValue.length >= PIN_LENGTH) {
                pinValue = pinValue.substring(0, PIN_LENGTH - 1)
                return
            }
            pinValue += value
            handlePinValue(
                pinValue,
                ::setErrorMsg,
                ::setPinValue,
                CoroutineScope(Dispatchers.Main),
                type
            )
        }
    }

    fun rmCharFromLast() {
        setErrorMsg("")
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FriendlyWelcomeTitle(type = type)
        PinDisplay(pinValue)
        ErrorMsg(
            text = errorMsg,
            alignToCenter = true,
            paddingTop = 12,
            paddingBottom = if (type == PinPageType.CREATE) 60 else 12
        )
        PinInput(
            type = type,
            setPinsValueByAppend = ::setPinsValueByAppend,
            rmCharFromLast = ::rmCharFromLast
        )
    }
}

@Composable
private fun PinInput(
    type: PinPageType,
    setPinsValueByAppend: (String) -> Unit,
    rmCharFromLast: () -> Unit
) {
    listOf("123", "456", "789", CONTROL_BIOMETRIC + "0" + CONTROL_DELETE).map {
        PinInputLine(
            type = type,
            controlValue = it,
            setPinsValueByAppend = setPinsValueByAppend,
            rmCharFromLast = rmCharFromLast
        )
    }
}

@Composable
private fun PinInputLine(
    type: PinPageType,
    controlValue: String,
    setPinsValueByAppend: (String) -> Unit,
    rmCharFromLast: () -> Unit
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
                type = type,
                controlValue = it.toString(),
                setPinsValueByAppend = setPinsValueByAppend,
                rmCharFromLast = rmCharFromLast
            )
        }
    }
}

@Composable
private fun PinInputItem(
    type: PinPageType,
    controlValue: String,
    setPinsValueByAppend: (String) -> Unit,
    rmCharFromLast: () -> Unit
) {
    val regex0To9 = "[0-9]".toRegex()
    fun handleClick() {
        if (regex0To9.matches(controlValue)) {
            setPinsValueByAppend(controlValue)
        } else if (CONTROL_DELETE == controlValue) {
            rmCharFromLast()
        } else if (CONTROL_BIOMETRIC == controlValue) {
            // TODO:CONTROL_BIOMETRIC
        }
    }

    if (CONTROL_BIOMETRIC == controlValue) {
        val modifier = Modifier
            .size(70.dp)
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
        if (type == PinPageType.CREATE) {
            Box(modifier = modifier)
        } else {
            Row(
                modifier = modifier
                    .clickable(indication = null, interactionSource = remember {
                        MutableInteractionSource()
                    }) {
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
                        .clickable(enabled = type == PinPageType.VERIFY) {
                            handleClick()
                        }
                )
            }
        }
    } else {
        var modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
        modifier = if (controlValue != CONTROL_DELETE) {
            modifier
                .background(Color(0xFFF7F9FD))
                .clickable {
                    handleClick()
                }
        } else {
            modifier
                .background(Color.Transparent)
                .clickable(indication = null, interactionSource = remember {
                    MutableInteractionSource()
                }) {
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FriendlyWelcomeTitle(type: PinPageType) {
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
private fun PinDisplay(pinValue: String) {
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
                modifier.background(Color.Black)
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
    pin: String,
    errorMsgSetter: (String) -> Unit,
    pinSetter: (String) -> Unit,
    scope: CoroutineScope,
    type: PinPageType
) {
    errorMsgSetter("")
    if (repeat) {
        if (repeatPinValue.length != PIN_LENGTH) {
            return
        }
    } else {
        if (pin.length != PIN_LENGTH) {
            return
        }
    }
    checkPin(pin, errorMsgSetter, pinSetter, scope, type)
}

private fun checkPin(
    pin: String,
    errorMsgSetter: (String) -> Unit,
    pinSetter: (String) -> Unit,
    scope: CoroutineScope,
    type: PinPageType
) {
    scope.launch(Dispatchers.IO) {
        if (type == PinPageType.CREATE) {
            checkPinCreate(pin, errorMsgSetter, pinSetter)
        } else {
            checkPinVerify(pin, errorMsgSetter, pinSetter)
        }
    }
}

private fun checkPinCreate(
    pin: String,
    errorMsgSetter: (String) -> Unit,
    pinSetter: (String) -> Unit
) {
    val setPin = WalletLifecyclePresenter.setPin ?: return
    if (repeat) {
        if (repeatPinValue != pin) {
            clearAndReportErr(errorMsgSetter, pinSetter, "pin not match")
            return
        }
    } else {
        val result = setPin.isValidPin(pin)
        if (!result) {
            clearAndReportErr(errorMsgSetter, pinSetter, "invalid pin")
            return
        }
        repeat = true
        return
    }
    WalletLifecyclePresenter.wallet = setPin.lockAndGetWallet(pin)
    SocialRecoveryModal.onSuccess()
}


private fun checkPinVerify(
    pin: String,
    errorMsgSetter: (String) -> Unit,
    pinSetter: (String) -> Unit
) {
    val unlock = WalletLifecyclePresenter.unlock ?: return
    val result = unlock.checkPin(pin)
    if (!result) {
        clearAndReportErr(errorMsgSetter, pinSetter, "incorrect pin")
        return
    }
    WalletLifecyclePresenter.wallet = unlock.unlockAndBuildWallet(pin)
    SocialRecoveryModal.onSuccess()
}

private fun clearAndReportErr(
    errorMsgSetter: (String) -> Unit,
    pinSetter: (String) -> Unit,
    errMsg: String
) {
    pinSetter("")
    errorMsgSetter(errMsg)
    return
}

private fun clearUp() {
    repeat = false
    repeatPinValue = ""
}

internal enum class PinPageType {
    CREATE,
    VERIFY
}
