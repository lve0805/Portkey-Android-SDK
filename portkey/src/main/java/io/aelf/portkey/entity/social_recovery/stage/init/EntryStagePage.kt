package io.aelf.portkey.entity.social_recovery.stage.init

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity.CheckedEntry
import io.aelf.portkey.behaviour.global.EntryCheckConfig
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal
import io.aelf.portkey.internal.model.common.AccountOriginalType
import io.aelf.portkey.internal.model.google.GoogleAccount
import io.aelf.portkey.network.connecter.NetworkService
import io.aelf.portkey.sdk.R
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.tools.timeout.useTimeout
import io.aelf.portkey.ui.basic.ErrorMsg
import io.aelf.portkey.ui.basic.HugeTitle
import io.aelf.portkey.ui.basic.Toast.showToast
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.HugeButton
import io.aelf.portkey.ui.button.IconConfig
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.utils.log.GLogger
import io.aelf.utils.AElfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var entryPageHandler: (((String, CoroutineScope, Context) -> Unit, String) -> Unit)? = null

@Composable
internal fun EntryPage() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    if (WalletLifecyclePresenter.SpecialStageIdentifier.CHOSE_TO_INPUT_EMAIL) {
        InputEmailPage()
    } else {
        ChoosePathPage()
    }
    entryPageHandler = { callback, token ->
        callback(token, scope, context)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InputEmailPage() {
    var email by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
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
            scope.launch(Dispatchers.IO) {
                authCheck(email, this, context)
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
                .border(width = 1.dp, color = Color(0xFFEDEFF5), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    commitCheck(email)
                }
            ),
            singleLine = true,
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
                keyboard?.hide()
                commitCheck(email)
            }
        }, enable = commitButtonEnable)
    }
}


@WorkerThread
private suspend fun authCheck(
    auth: String,
    scope: CoroutineScope,
    context: Context,
    accountType: AccountOriginalType = AccountOriginalType.Email,
    googleAccount: GoogleAccount? = null
) {
    Loading.showLoading("Checking on-chain data...")
    val checkDeferred = scope.launch(Dispatchers.IO) {
        val entry: CheckedEntry =
            EntryBehaviourEntity.attemptAccountCheck(
                EntryCheckConfig().apply {
                    accountIdentifier = auth
                    accountOriginalType = accountType
                },
                googleAccount
            )
        if (entry.isRegistered) {
            entry.asLogInChain().onLoginStep {
                Loading.hideLoading()
                WalletLifecyclePresenter.login = it
                WalletLifecyclePresenter.activeGuardians = it.guardians
                leavesEntryPage()
            }
        } else {
            Loading.hideLoading()
            Dialog.show(DialogProps().apply {
                mainTitle = "Continue with this Account?"
                subTitle =
                    "This account has not been registered yet. Click \"Confirm\" to complete the registration."
                positiveCallback = {
                    scope.launch(Dispatchers.IO) {
                        entry.asRegisterChain().onRegisterStep {
                            if (googleAccount == null) {
                                Loading.showLoading("Checking on-chain data...")
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        WalletLifecyclePresenter.activeGuardian = it.guardian
                                    } catch (e: Throwable) {
                                        GLogger.e(
                                            "error when checking register guardian info.",
                                            AElfException(e)
                                        )
                                    }
                                    Loading.hideLoading()
                                    if (WalletLifecyclePresenter.activeGuardian == null) {
                                        showToast(
                                            context,
                                            "Sorry but the sever was not responding, please try again later."
                                        )
                                    } else {
                                        WalletLifecyclePresenter.register = it
                                    }
                                    leavesEntryPage()
                                }
                            } else {
                                // Google account registration
                                scope.launch(Dispatchers.IO) {
                                    val result = it.guardian.verifyVerificationCode("FAKE")
                                    if (result) {
                                        WalletLifecyclePresenter.setPin = it.afterVerified()
                                        leavesEntryPage()
                                    } else {
                                        showToast(
                                            context,
                                            "Sorry but the sever was not responding, please try again later."
                                        )
                                    }
                                    Loading.hideLoading()
                                }
                            }
                        }
                    }
                }
            })
        }
    }
    useTimeout(job = checkDeferred, restart = {
        scope.launch(Dispatchers.IO) {
            authCheck(auth, scope, context, accountType, googleAccount)
        }
    })
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
                SocialRecoveryModal.checkGoogleToken()
            }
        }, icon = IconConfig().apply {
            iconResId = R.drawable.google_icon
            tintColor = Color.White
        })
        Divider()
        HugeButton(config = ButtonConfig().apply {
            text = "Login with Email"
            bgColor = Color(0xFFFFFFFF)
            textColor = Color(0xFF162736)
            onClick = ::enterInputEmailPage
        }, icon = IconConfig().apply {
            iconResId = R.drawable.email_icon
        })
    }
}

internal fun continueWithGoogleToken(googleAccount: GoogleSignInAccount) {
    entryPageHandler?.let {
        it(
            { token, scope, context ->
                run {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val accessToken = NetworkService.getInstance()
                                .getGoogleAuthResult(googleAccount.serverAuthCode ?: "")
                                .access_token
                            authCheck(
                                token,
                                scope,
                                context,
                                AccountOriginalType.Google,
                                convertGoogleAccount(googleAccount, accessToken)
                            )
                        } catch (e: Throwable) {
                            Loading.hideLoading()
                        }
                    }
                }
            }, googleAccount.id ?: ""
        )
    }
}

private fun convertGoogleAccount(
    googleAccount: GoogleSignInAccount,
    givenAccountToken: String
): GoogleAccount {
    return GoogleAccount().apply {
        id = googleAccount.id
        email = googleAccount.email
        idToken = googleAccount.idToken
        accessToken = givenAccountToken
    }
}

internal fun enterInputEmailPage() {
    WalletLifecyclePresenter
        .SpecialStageIdentifier
        .CHOSE_TO_INPUT_EMAIL = true
}

internal fun leavesEntryPage() {
    CoroutineScope(Dispatchers.IO).launch {
        delay(500)
        WalletLifecyclePresenter
            .SpecialStageIdentifier.reset()
    }
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
