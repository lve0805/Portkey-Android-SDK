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
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.core.stage.social_recovery.SocialRecoveryStageEnum
import io.aelf.portkey.debug.initDebug
import io.aelf.portkey.entity.social_recovery.stage.init.EntryPage
import io.aelf.portkey.entity.social_recovery.stage.init.continueWithGoogleToken
import io.aelf.portkey.entity.social_recovery.stage.pin.SetPinStagePage
import io.aelf.portkey.entity.social_recovery.stage.pin.UnlockStagePage
import io.aelf.portkey.entity.social_recovery.stage.verify.LoginStagePage
import io.aelf.portkey.entity.social_recovery.stage.verify.RegisterPage
import io.aelf.portkey.entity.static.footage.PortkeyFootage
import io.aelf.portkey.internal.model.google.GoogleAccount
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
import io.aelf.utils.AElfException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SocialRecoveryModal : ModalController {
    private var isShow by mutableStateOf(false)
    private const val heightPercent = 0.85f
    private var backFunction: (() -> Unit)? by mutableStateOf(null)
    private var modalProps: SocialRecoveryModalProps = SocialRecoveryModalProps()

    fun callUpModal(props: SocialRecoveryModalProps) {
        this.modalProps = props
        isShow = true
    }

    internal fun sendGoogleToken(googleAccount: GoogleSignInAccount?) {
        Loading.hideLoading()
        if (googleAccount == null || TextUtils.isEmpty(googleAccount?.id)) {
            Dialog.show(DialogProps().apply {
                mainTitle = "Google Auth Failure"
                subTitle = "Sorry, we can't get your Google Account at this time, please try again."
                positiveText = "Retry"
                negativeText = "Cancel"
                positiveCallback = {
                    checkGoogleToken()
                }
            })
        } else {
            continueWithGoogleToken(convertGoogleAccount(googleAccount))
        }
    }

    private fun convertGoogleAccount(googleAccount: GoogleSignInAccount): GoogleAccount {
        return GoogleAccount().apply {
            id = googleAccount.id
            email = googleAccount.email
            idToken = googleAccount.idToken
        }
    }

    internal fun onSuccess() {
        isShow = false
        WalletLifecyclePresenter.reset(saveWallet = true)
        modalProps.onSuccess?.let { it() }
    }

    internal fun checkGoogleToken() {
        Loading.showLoading("Checking Google Account...")
        modalProps.onUseGoogleAuthService?.let { it() }
    }

    override fun closeModal() {
        Dialog.show(DialogProps().apply {
            mainTitle = "Leave this page?"
            subTitle = "Are you sure you want to leave this page? All changes will not be saved."
            positiveCallback = {
                isShow = false
                WalletLifecyclePresenter.reset()
                modalProps.onUserCancel?.let { it() }
            }
        })
    }

    internal fun forceCloseModal(exception: AElfException) {
        isShow = false
        WalletLifecyclePresenter.reset()
        modalProps.onError?.let { it(exception) }
    }

    internal fun goBack(){
        backFunction?.let {
            it()
        }
    }

    override fun setBackProcess(goWithCleanItself: Boolean, function: () -> Unit) {
        backFunction = {
            function()
            if (goWithCleanItself) {
                backFunction = null
            }
        }
    }

    override fun clearBackProcess() {
        backFunction = null
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
                Loading.showLoading("Checking existing wallet...")
                scope.launch(Dispatchers.IO) {
                    EntryBehaviourEntity.ifLockedWalletExists().let {
                        if (it) {
                            GLogger.t("locked wallet exists, trying to unlock")
                            WalletLifecyclePresenter.unlock =
                                EntryBehaviourEntity.attemptToGetLockedWallet().get()
                            clearBackProcess()
                        }
                    }
                    Loading.hideLoadingCoroutine(scope = this, duration = 800)
                }
            }
            UseEffect(
                WalletLifecyclePresenter.wallet,
                WalletLifecyclePresenter.entry,
                WalletLifecyclePresenter.login,
                WalletLifecyclePresenter.register,
                WalletLifecyclePresenter.activeGuardian,
                WalletLifecyclePresenter.setPin,
                WalletLifecyclePresenter.unlock
            ) {
                WalletLifecyclePresenter.inferCurrentStage()
            }
            UseAndroidBackButtonSettings(::closeModal)
            Column(
                modifier = Modifier
                    .zIndex(ZIndexConfig.Modal.getZIndex())
                    .background(backgroundColor)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                ModalBody()
            }
        }
    }

    @Composable
    private fun ModalBody() {
        Box(
            modifier = Modifier
                .fillMaxHeight(heightPercent)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .zIndex(ZIndexConfig.Modal.getZIndex())
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
                .fillMaxHeight(0.85f)
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
                        onClick = {
                            backFunction?.let {
                                it()
                            }
                        }
                    )
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
                    )

            )

        }
    }
}

internal interface ModalController {
    fun closeModal()
    fun clearBackProcess()
    fun setBackProcess(goWithCleanItself: Boolean = true, function: () -> Unit)
}

open class SocialRecoveryModalProps {
    var onUserCancel: (() -> Unit)? = null
    var onSuccess: (() -> Unit)? = null
    var onError: ((AElfException) -> Unit)? = null
    var onUseGoogleAuthService: (() -> Unit)? = null
}


@Preview
@Composable
internal fun ModalPreview() {
    val context = LocalContext.current
    UseComponentDidMount {
        initDebug(context)
    }
    val props = remember {
        SocialRecoveryModalProps().apply {
            onUserCancel = {
                GLogger.w("onUserCancel")
            }
            onSuccess = {
                GLogger.w("onSuccess")
            }
            onError = {
                GLogger.e("onError", it)
            }
        }
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