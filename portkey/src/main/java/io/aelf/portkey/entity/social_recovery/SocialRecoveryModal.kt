package io.aelf.portkey.entity.social_recovery


import android.text.TextUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity
import io.aelf.portkey.behaviour.wallet.PortkeyWallet
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.core.stage.social_recovery.SocialRecoveryStageEnum
import io.aelf.portkey.debug.initDebug
import io.aelf.portkey.entity.social_recovery.stage.init.EntryPage
import io.aelf.portkey.entity.social_recovery.stage.init.continueEntryWithGoogleToken
import io.aelf.portkey.entity.social_recovery.stage.pin.SetPinStagePage
import io.aelf.portkey.entity.social_recovery.stage.pin.UnlockStagePage
import io.aelf.portkey.entity.social_recovery.stage.verify.LoginStagePage
import io.aelf.portkey.entity.social_recovery.stage.verify.RegisterPage
import io.aelf.portkey.entity.social_recovery.stage.verify.continueVerifyWithGoogleAccount
import io.aelf.portkey.entity.static.footage.PortkeyFootage
import io.aelf.portkey.sdk.R
import io.aelf.portkey.tools.friendly.UseAndroidBackButtonSettings
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.tools.friendly.UseEffect
import io.aelf.portkey.ui.basic.ZIndexConfig
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.HugeButton
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.Dialog.PortkeyDialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.ui.loading.Loading.PortkeyLoading
import io.aelf.portkey.utils.log.GLogger
import io.aelf.response.ResultCode
import io.aelf.utils.AElfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal object SocialRecoveryModal : ModalController {
    private var isShow by mutableStateOf(false)
    private const val heightPercent = 0.90f
    private var backFunction: (() -> Unit)? by mutableStateOf(null)
    private var modalProps: SocialRecoveryModalProps =
        SocialRecoveryModalProps(onUseGoogleAuthService = {})

    internal fun callUpModal(props: SocialRecoveryModalProps) {
        this.modalProps = props
        if (WalletLifecyclePresenter.wallet != null) {
            GLogger.w("Already login")
            props.onError(AElfException(ResultCode.INTERNAL_ERROR, "Already login"))
            return
        }
        isShow = true
    }

    internal fun sendGoogleToken(googleAccount: GoogleSignInAccount?) {
        if (googleAccount == null || TextUtils.isEmpty(googleAccount.id)) {
            Loading.hideLoading()
            Dialog.show(DialogProps().apply {
                mainTitle = "Google Auth Failure"
                subTitle = "Sorry, we can't get your Google Account at this time, please try again."
                positiveText = "Retry"
                negativeText = "Cancel"
                positiveCallback = {
                    checkGoogleToken()
                }
            })
        } else if (WalletLifecyclePresenter.stageEnum == SocialRecoveryStageEnum.INIT) {
            continueEntryWithGoogleToken(googleAccount)
        } else if (WalletLifecyclePresenter.stageEnum == SocialRecoveryStageEnum.READY_TO_LOGIN) {
            continueVerifyWithGoogleAccount(googleAccount)
        }
    }


    internal fun onSuccess() {
        val wallet = WalletLifecyclePresenter.wallet ?: return
        isShow = false
        WalletLifecyclePresenter.reset(saveWallet = true)
        modalProps.onSuccess(wallet)
    }

    internal fun checkGoogleToken() {
        val onUseGoogleAuthService = modalProps.onUseGoogleAuthService
        Loading.showLoading("Checking Google Account...")
        onUseGoogleAuthService {
            sendGoogleToken(it)
        }
    }

    override fun closeModal() {
        Dialog.show(DialogProps().apply {
            mainTitle = "Leave this page?"
            subTitle = "Are you sure you want to leave this page? All changes will not be saved."
            positiveCallback = {
                isShow = false
                WalletLifecyclePresenter.reset()
                modalProps.onUserCancel()
            }
        })
    }

    internal fun resetAndBackToHomePage() {
        Dialog.show(DialogProps().apply {
            mainTitle = "Leave this page?"
            subTitle =
                "Are you sure you want to leave this page? All the changes you made will be erased."
            positiveCallback = {
                WalletLifecyclePresenter.reset()
            }
        })
    }

    internal fun leaveCurrentGuardianPage() {
        Dialog.show(DialogProps().apply {
            mainTitle = "Leave this page?"
            subTitle =
                "Are you sure you want to leave this page? All the changes you made will be erased."
            positiveCallback = {
                WalletLifecyclePresenter.activeGuardian = null
            }
        })
    }

    @Composable
    internal fun SocialRecoveryModal() {
        val backgroundColor by animateColorAsState(
            targetValue = if (isShow) Color.Black.copy(alpha = 0.5F) else Color.Transparent,
            animationSpec = tween(500),
            label = "modal bgColor"
        )
        val scope = rememberCoroutineScope()
        if (isShow) {
            UseComponentDidMount {
                unlockWalletCheck(scope)
            }
            UseWatch()
            UseAndroidBackButtonSettings {
                if (WalletLifecyclePresenter.stageEnum == SocialRecoveryStageEnum.INIT) {
                    closeModal()
                } else {
                    backFunction?.let {
                        it()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(ZIndexConfig.Modal.getZIndex())
            ) {
                Column(
                    modifier = Modifier
                        .background(backgroundColor)
                        .zIndex(ZIndexConfig.Modal.getZIndex() + 1)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    ModalBody()
                }
            }
        }
    }

    @Composable
    private fun UseWatch() {
        UseEffect(
            WalletLifecyclePresenter.wallet,
            WalletLifecyclePresenter.entry,
            WalletLifecyclePresenter.login,
            WalletLifecyclePresenter.register,
            WalletLifecyclePresenter.activeGuardian,
            WalletLifecyclePresenter.SpecialStageIdentifier.CHOSE_TO_INPUT_EMAIL,
            WalletLifecyclePresenter.setPin,
            WalletLifecyclePresenter.unlock
        ) {
            WalletLifecyclePresenter.inferCurrentStage()
            inferBackProcess()
        }
    }

    private fun inferBackProcess() {
        backFunction = when (WalletLifecyclePresenter.stageEnum) {
            SocialRecoveryStageEnum.INIT -> {
                if (WalletLifecyclePresenter.SpecialStageIdentifier.CHOSE_TO_INPUT_EMAIL) {
                    {
                        WalletLifecyclePresenter.SpecialStageIdentifier.CHOSE_TO_INPUT_EMAIL = false
                    }
                } else {
                    null
                }
            }

            SocialRecoveryStageEnum.READY_TO_REGISTER,
            SocialRecoveryStageEnum.SET_PIN -> {
                ::resetAndBackToHomePage
            }

            SocialRecoveryStageEnum.READY_TO_LOGIN -> {
                if (WalletLifecyclePresenter.activeGuardian == null) {
                    ::resetAndBackToHomePage
                } else {
                    ::leaveCurrentGuardianPage
                }
            }

            else -> {
                null
            }
        }
    }

    private fun unlockWalletCheck(scope: CoroutineScope) {
        Loading.showLoading("Checking existing wallet...")
        scope.launch(Dispatchers.IO) {
            EntryBehaviourEntity.ifLockedWalletExists().let {
                if (it) {
                    GLogger.t("locked wallet exists, trying to unlock")
                    WalletLifecyclePresenter.unlock =
                        EntryBehaviourEntity.attemptToGetLockedWallet().get()
                }
            }
            Loading.hideLoadingCoroutine(scope = this, duration = 100)
        }
    }

    @Composable
    private fun ModalBody() {
        Box(
            modifier = Modifier
                .fillMaxHeight(heightPercent)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .zIndex(ZIndexConfig.Modal.getZIndex() + 1)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                FunctionalButtons()
                Content()
                PortkeyFootage()
            }
        }
    }

    @Composable
    private fun Content() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(determineHeightPercent())
                .background(Color.White)
        ) {
            when (WalletLifecyclePresenter.stageEnum) {
                SocialRecoveryStageEnum.INIT -> {
                    EntryPage()
                }

                SocialRecoveryStageEnum.READY_TO_REGISTER -> {
                    RegisterPage()
                }

                SocialRecoveryStageEnum.READY_TO_LOGIN -> {
                    LoginStagePage()
                }

                SocialRecoveryStageEnum.UNLOCK -> {
                    UnlockStagePage()
                }

                SocialRecoveryStageEnum.SET_PIN -> {
                    SetPinStagePage()
                }

                else -> {}
            }
        }
    }

    private fun determineHeightPercent(): Float {
        return when (WalletLifecyclePresenter.stageEnum) {
            SocialRecoveryStageEnum.UNLOCK -> {
                0.96f
            }

            else -> {
                0.85f
            }
        }
    }

    @Composable
    private fun FunctionalButtons() {
        Row(
            modifier = Modifier
                .height(40.dp)
                .width((LocalConfiguration.current.screenWidthDp - (12 * 2)).dp)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val painter = if (backFunction != null) {
                painterResource(id = R.drawable.icon_back)
            } else {
                ColorPainter(Color.Transparent)
            }
            Icon(
                painter = painter,
                contentDescription = "go back button",
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        enabled = backFunction != null,
                        onClick = {
                            backFunction?.let {
                                it()
                            }
                        }
                    ),
                tint = Color(0xFF414852)
            )
            Icon(
                painter = painterResource(id = R.drawable.close_icon),
                contentDescription = "close button",
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        onClick = SocialRecoveryModal::closeModal
                    ),
                tint = Color(0xFF414852)
            )
        }
    }
}

internal interface ModalController {
    fun closeModal()
}

data class SocialRecoveryModalProps(
    val onUserCancel: () -> Unit = {},
    val onSuccess: (PortkeyWallet) -> Unit = {},
    val onError: (AElfException) -> Unit = {},
    val onUseGoogleAuthService: (onSuccess: (GoogleSignInAccount?) -> Unit) -> Unit
)


@Preview
@Composable
internal fun ModalPreview() {
    val context = LocalContext.current
    UseComponentDidMount {
        initDebug(context)
    }
    val props = remember {
        SocialRecoveryModalProps(
            onUserCancel = {
                GLogger.w("onUserCancel")
            },
            onSuccess = {
                GLogger.w("onSuccess")
            },
            onError = {
                GLogger.e("onError", it)
            },
            onUseGoogleAuthService = {
                GLogger.w("onUseGoogleAuthService")
            }
        )
    }
    SocialRecoveryModal.callUpModal(props)
    HugeButton(
        config = ButtonConfig().apply {
            text = "Call Up Dialog"
            onClick = {
                SocialRecoveryModal.callUpModal(props)
            }
        }
    )
    PortkeyLoading()
    PortkeyDialog()
    SocialRecoveryModal.SocialRecoveryModal()
}