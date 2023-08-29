package io.aelf.portkey.entity.social_recovery.stage.verify

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.behaviour.guardian.GuardianBehaviourEntity
import io.aelf.portkey.behaviour.login.LoginBehaviourEntity
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.entity.static.guardian_controller.GuardianController
import io.aelf.portkey.entity.static.guardian_controller.GuardianInfo
import io.aelf.portkey.entity.static.guardian_controller.OutsideStateEnum
import io.aelf.portkey.internal.model.common.AccountOriginalType
import io.aelf.portkey.sdk.R
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.tools.timeout.useTimeout
import io.aelf.portkey.ui.basic.Distance
import io.aelf.portkey.ui.basic.HugeTitle
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.HugeButton
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.ui.rich_text.RichText
import io.aelf.portkey.utils.log.GLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var isExpired by mutableStateOf(false)
private var setExpiredJob: Job? = null

@Composable
internal fun LoginStagePage() {
    if (WalletLifecyclePresenter.activeGuardian != null) {
        GuardianPage()
    } else {
        LoginMainBody()
    }
}

@Composable
private fun LoginMainBody() {
    val scope = rememberCoroutineScope()
    UseComponentDidMount {
        startCountDown(scope)
    }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            HugeTitle(text = "Guardians' Approval")
            RichText(
                text = if (isExpired) "Expired" else "Expire after 1 hour",
                maxLine = 1,
                normalTextStyle = SpanStyle(
                    color = Color(0xFF8F949C),
                    fontWeight = FontWeight(400),
                    fontSize = 14.sp,
                    background = Color.Transparent
                )
            )
            GuardianVerifyStatusBar()
            GuardianInfoList()
        }
        CommitButton()
    }
}

@Composable
private fun GuardianVerifyStatusBar() {
    Row(
        modifier = Modifier
            .width(DynamicWidth(paddingHorizontal = 20))
            .padding(top = 40.dp)
            .height(22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RichText(text = "Guardians' Approval", maxLine = 1)
            Distance(width = 4)
            Icon(
                painter = painterResource(id = R.drawable.question_icon),
                contentDescription = "question icon",
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        Dialog.show(DialogProps().apply {
                            mainTitle = "Guardians' Approval Rule"
                            subTitle =
                                "You will need a certain number of guardians to confirm your action. The requirements differ depending on your guardian counts.\n" +
                                        "If the total number is less than or equal to 3, approval from all is needed. If that figure is greater than 3, approval from a minimum of 60% is needed."
                            useSingleConfirmButton = true
                            positiveText = "Got it"
                        })
                    },
                tint = Color(0xFF8F949C)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val current = WalletLifecyclePresenter.login?.fullFilledGuardianCount ?: 0
            val limit = WalletLifecyclePresenter.login?.guardianVerifyLimit ?: 0
            ProgressIcon()
            Distance(5)
            RichText(
                text = "#${current}# / $limit",
                specialTextStyle = SpanStyle(
                    color = Color(0xFF20CD85),
                    fontWeight = FontWeight(500),
                    fontSize = 14.sp,
                    background = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun ProgressIcon() {
    val login = WalletLifecyclePresenter.login ?: return
    val useCircleBg = !login.isFulfilled && login.fullFilledGuardianCount > 0
    val resId = computeProgressIconRes(login = login)
    if (useCircleBg) {
        Row(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .border(width = 1.dp, color = Color(0xFF20CD85), shape = CircleShape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressIconContent(resId = resId, size = 14)
        }
    } else {
        ProgressIconContent(resId = resId, size = 14)
    }
}

@Composable
private fun ProgressIconContent(resId: Int, size: Int) {
    Icon(
        painter = painterResource(
            id = resId
        ),
        tint = Color(0xFF20CD85),
        contentDescription = "guardian verify status",
        modifier = Modifier.size(size.dp)
    )
}

private fun computeProgressIconRes(login: LoginBehaviourEntity): Int {
    return if (login.isFulfilled) {
        R.drawable.guardian_verify_verified
    } else if (login.fullFilledGuardianCount == 0) {
        R.drawable.guardian_verify_init
    } else {
        val progress: Double =
            login.fullFilledGuardianCount.toDouble() / login.guardianVerifyLimit.toDouble()
        when {
            progress <= 0.125 -> R.drawable.pending_01
            progress <= 0.25 -> R.drawable.pending_02
            progress <= 0.375 -> R.drawable.pending_03
            progress <= 0.5 -> R.drawable.pending_04
            progress <= 0.625 -> R.drawable.pending_05
            progress <= 0.75 -> R.drawable.pending_06
            progress <= 0.875 -> R.drawable.pending_07
            else -> R.drawable.pending_07
        }
    }
}

@Composable
private fun GuardianInfoList() {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val loginEntity = WalletLifecyclePresenter.login ?: return
    val guardiansOriginal = loginEntity.guardians ?: emptyList()
    val guardians =
        guardiansOriginal.mapIndexed { index, info ->
            GuardianInfo().apply {
                guardianEntity = loginEntity.getGuardianBehaviourEntity(index)
                state = if (isExpired) OutsideStateEnum.Expired
                else if (loginEntity!!.isFulfilled) OutsideStateEnum.LimitReached
                else OutsideStateEnum.Normal
                buttonClick = {
                    when (info.originalData.type) {
                        AccountOriginalType.Email.name,
                        AccountOriginalType.Apple.name,
                        AccountOriginalType.Phone.name -> {
                            WalletLifecyclePresenter.activeGuardian =
                                loginEntity.getGuardianBehaviourEntity(index)
                        }

                        AccountOriginalType.Google.name -> {
                            scope.launch(Dispatchers.IO) {
                                googleGuardianVerify(scope, index, context)
                            }
                        }
                    }
                }
            }
        }
    BoxWithConstraints(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 68.dp)
            .width(DynamicWidth(paddingHorizontal = 20))
            .wrapContentHeight(Alignment.CenterVertically)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(
                    state = scrollState
                )
        ) {
            guardians.map {
                GuardianController(info = it, modifier = Modifier.padding(bottom = 12.dp))
            }
        }
    }
}

private suspend fun googleGuardianVerify(scope: CoroutineScope, index: Int, context: Context) {
    Loading.showLoading("Verifying...")
    val job = scope.launch(Dispatchers.IO) {
        val guardian: GuardianBehaviourEntity =
            WalletLifecyclePresenter.login!!.getGuardianBehaviourEntity(index)
        // google's guardian
        try {
            val result = guardian.verifyVerificationCode("FAKE")
            if (result) {
                Loading.hideLoading()
                forceRecomposition()
                return@launch
            }
        } catch (e: Throwable) {
            GLogger.e("verifyVerificationCode error:${e.message}")

        }
        Dialog.show(DialogProps().apply {
            mainTitle = "Network failure"
            subTitle = "There's some issue happened, please try again."
            positiveText = "Try again"
            negativeText = "Cancel"
            positiveCallback = {
                scope.launch(Dispatchers.IO) {
                    googleGuardianVerify(scope, index, context)
                }
            }
        })
        Loading.hideLoading()
    }
    useTimeout(job = job, restart = {
        scope.launch(Dispatchers.IO) {
            googleGuardianVerify(scope, index, context)
        }
    })
}

private suspend fun forceRecomposition() {
    val login = WalletLifecyclePresenter.login
    WalletLifecyclePresenter.login = null
    delay(10)
    WalletLifecyclePresenter.login = login
}


@Composable
private fun CommitButton() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(DynamicWidth(paddingHorizontal = 20))
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        HugeButton(
            config = ButtonConfig().apply {
                text = "Confirm"
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        afterVerified(scope, context)
                    }
                }
            },
            enable = WalletLifecyclePresenter.login?.isFulfilled ?: false
        )
    }
}

private fun startCountDown(scope: CoroutineScope) {
    setExpiredJob?.cancel()
    setExpiredJob = scope.launch(Dispatchers.IO) {
        var timeout = 60 * 60 * 1000
        while (timeout > 0) {
            delay(1000)
            timeout -= 1000
        }
        isExpired = true
    }
}

private suspend fun afterVerified(scope: CoroutineScope, context: Context) {
    fun restart() {
        scope.launch(Dispatchers.IO) {
            afterVerified(scope, context)
        }
    }
    if (WalletLifecyclePresenter.login?.isFulfilled != true) return
    Loading.showLoading("Checking data...")
    val job = scope.launch(Dispatchers.IO) {
        try {
            val setPin = WalletLifecyclePresenter.login?.afterVerified()
            if (setPin != null) {
                Loading.hideLoading()
                WalletLifecyclePresenter.setPin = setPin
                return@launch
            }
        } catch (e: Throwable) {
            GLogger.e("afterVerified error:${e.message}")
        }
        Loading.hideLoading()
        Dialog.show(DialogProps().apply {
            mainTitle = "Network failure"
            subTitle = "There's some issue happened, please try again."
            positiveText = "Try again"
            negativeText = "Cancel"
            positiveCallback = ::restart
        })
    }
    useTimeout(job = job, restart = ::restart)
}

private fun cleanUp() {
    WalletLifecyclePresenter.login = null
    isExpired = false
    setExpiredJob?.cancel()
}

