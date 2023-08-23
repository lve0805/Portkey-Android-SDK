package io.aelf.portkey.entity.social_recovery.stage

import android.content.Context
import android.text.TextUtils
import androidx.annotation.WorkerThread
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.R
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity.CheckedEntry
import io.aelf.portkey.behaviour.global.EntryCheckConfig
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal
import io.aelf.portkey.internal.model.common.AccountOriginalType
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.tools.friendly.NETWORK_TIMEOUT
import io.aelf.portkey.ui.basic.ErrorMsg
import io.aelf.portkey.ui.basic.HugeTitle
import io.aelf.portkey.ui.basic.Toast.showToast
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.HugeButton
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun EntryPage() {
    if (WalletLifecyclePresenter.SpecialStageIdentifier.CHOSE_TO_INPUT_EMAIL) {
        InputEmailPage()
    } else {
        ChoosePathPage()
    }
}

@Composable
private fun InputEmailPage() {
    var email by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var commitButtonEnable by remember {
        mutableStateOf(false)
    }
    val inputCheck: (String) -> Unit = {
        if (!TextUtils.isEmpty(errorMsg) || !commitButtonEnable) {
            errorMsg = ""
            commitButtonEnable = true
        }
        if (TextUtils.isEmpty(it)) {
            commitButtonEnable = false
        }
        email = it
    }
    val commitCheck: (String) -> Unit = {
        if (!isEmailValid(it)) {
            errorMsg = "Invalid email address"
            commitButtonEnable = false
        } else {
            scope.async {
                emailCheck(email, this, context)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HugeTitle(text = "Login with Email")
        TextField(
            value = email,
            onValueChange = inputCheck,
            modifier = Modifier
                .padding(top = 40.dp)
                .width(DynamicWidth(20))
                .border(width = 1.dp, color = Color(0xFFEDEFF5)),
            shape = RoundedCornerShape(8.dp),
            placeholder = {
                Text(
                    text = "Email",
                    style = TextStyle(
                        color = Color(0xFF8F949C),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                backgroundColor = Color.White
            )
        )
        ErrorMsg(text = errorMsg, paddingTop = 4, paddingBottom = 10)
        HugeButton(config = ButtonConfig().apply {
            text = "Login with Email"
            onClick = {
                commitCheck(email)
            }
        }, enable = commitButtonEnable)
    }

}

@WorkerThread
private suspend fun emailCheck(email: String, scope: CoroutineScope, context: Context) {
    Loading.showLoading("Checking on-chain data...")
    val checkDeferred = scope.launch(Dispatchers.IO) {
        val entry: CheckedEntry =
            EntryBehaviourEntity.attemptAccountCheck(EntryCheckConfig().apply {
                accountIdentifier = email
                accountOriginalType = AccountOriginalType.Email
            })
        Loading.hideLoading()
        if (entry.isRegistered) {
            entry.asLogInChain().onLoginStep {
                WalletLifecyclePresenter.login = it
                leavesEntryPage()
            }
        } else {
            Dialog.show(DialogProps().apply {
                mainTitle = "Continue with this Account?"
                subTitle =
                    "This account has not been registered yet. Click \"Confirm\" to complete the registration."
                positiveCallback = {
                    entry.asRegisterChain().onRegisterStep {
                        WalletLifecyclePresenter.register = it

                        leavesEntryPage()
                    }
                }
            })
        }
    }
    delay(NETWORK_TIMEOUT)
    if (checkDeferred.isActive) {
        checkDeferred.cancel()
        Loading.hideLoading()
        showToast(context, "Sorry but the sever was not responding, please try again later.")
    }
}

private fun isEmailValid(email: String): Boolean {
    val emailRegex = Regex("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-.]+)\\.([a-zA-Z0-9_\\-.]+)$")
    return emailRegex.matches(email)
}

@Composable
private fun ChoosePathPage() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TitleLine()
        LoginPathSelector()
    }
}

@Composable
private fun TitleLine() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(id = R.drawable.portkey_icon),
            contentDescription = "portkey wallet's icon",
            tint = Color(0xFF5B8EF4)
        )
        HugeTitle(text = "Login to Portkey")
    }
}

@Composable
private fun LoginPathSelector() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HugeButton(config = ButtonConfig().apply {
            text = "Login with Google"
            bgColor = Color(0xFF4285F4)
            textColor = Color(0xFFFFFFFF)
            onClick = {
                showToast(context, "Stub: need implement")
            }
        })
        Divider()
        HugeButton(config = ButtonConfig().apply {
            text = "Login with Email"
            bgColor = Color(0xFFFFFFFF)
            textColor = Color(0xFF162736)
            onClick = ::enterInputEmailPage
        })
    }
}

internal fun enterInputEmailPage() {
    WalletLifecyclePresenter
        .SpecialStageIdentifier
        .CHOSE_TO_INPUT_EMAIL = true
    SocialRecoveryModal.setBackProcess {
        WalletLifecyclePresenter
            .SpecialStageIdentifier
            .CHOSE_TO_INPUT_EMAIL = false
    }
}

internal fun leavesEntryPage() {
    WalletLifecyclePresenter
        .SpecialStageIdentifier.reset()
    SocialRecoveryModal.setBackProcess(
        function = {
            Dialog.show(DialogProps().apply {
                mainTitle = "Are you sure to leave?"
                subTitle = "If you leave, you will lose all the progress and have to start over."
                positiveCallback = {
                    SocialRecoveryModal.closeModal()
                    WalletLifecyclePresenter.reset()
                    WalletLifecyclePresenter.inferCurrentStage()
                }
            })
        }, goWithCleanItself = false
    )
}

@Composable
private fun Divider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
                .height((0.5).dp)
                .background(
                    Color(0xFFDFE4EC)
                )
        )
        Text(
            text = "OR",
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color(0xFF8F949C),
                fontSize = 12.sp,
                lineHeight = 18.sp
            ),
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .height((0.5).dp)
                .background(
                    Color(0xFFDFE4EC)
                )
        )
    }
}
