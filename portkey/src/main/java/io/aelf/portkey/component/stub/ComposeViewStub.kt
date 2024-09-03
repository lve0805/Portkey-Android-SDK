package io.aelf.portkey.component.stub

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal.SocialRecoveryModal
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.ui.basic.Toast
import io.aelf.portkey.ui.basic.ZIndexConfig
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.Dialog.PortkeyDialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.ui.loading.Loading.PortkeyLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PortkeySDKViewStub() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .zIndex(ZIndexConfig.Modal.getZIndex())
    ) {
        SocialRecoveryModal()
        PortkeyLoading()
        PortkeyDialog()
    }
}

fun initPortkeySDKViewStub(context: Context) {
    ComposeView(context = context).setContent {
        PortkeySDKViewStub()
    }
}

@Preview
@Composable
private fun DialogAndLoadingPreview() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    PortkeySDKViewStub()
    UseComponentDidMount {
        scope.launch {
            while (true) {
                delay(5000)
                Loading.showLoading("Now reading your mind =) ...")
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