package io.aelf.portkey.component.stub

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import io.aelf.portkey.config.screen.Screen
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal.SocialRecoveryModal
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.ui.basic.Toast
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.Dialog.PortkeyDialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.ui.loading.Loading.PortkeyLoading
import io.aelf.portkey.utils.log.GLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal var stubGenerated = false
internal const val initWarning =
    "You seems to have generated the stub twice, this is unexpected and you'd better check it."

@Composable
fun PortkeySDKViewStub() {
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    UseComponentDidMount {
        if (stubGenerated) {
            GLogger.e(initWarning)
        }
        stubGenerated = true
        Screen.updateScreenProps(config, scope)
    }
    SocialRecoveryModal()
    PortkeyLoading()
    PortkeyDialog()
}

fun initPortkeySDKViewStub(context: Context) {
    if (stubGenerated) {
        GLogger.e(initWarning)
        return
    }
    ComposeView(context = context).setContent {
        PortkeySDKViewStub()
    }
}

@Preview
@Composable
internal fun DialogAndLoadingPreview() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    PortkeySDKViewStub()
    UseComponentDidMount {
        scope.launch {
            while (true) {
                delay(5000)
                Loading.showLoading("Waiting...")
                delay(3000)
                Loading.hideLoading()
            }
        }
        scope.launch {
            while (true) {
                delay(2000)
                Dialog.hide()
                Dialog.show(DialogProps().apply {
                    mainTitle = "Carbon's asking =)"
                    subTitle = "R U a robot?"
                    positiveCallback = {
                        Toast.showToast(context, "U R a robot =)")
                    }
                    negativeCallback = {
                        Toast.showToast(context, "U R human -_-")
                    }
                })
            }
        }
    }
}