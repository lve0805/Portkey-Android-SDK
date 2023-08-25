package io.aelf.portkey.component.stub

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal.SocialRecoveryModal
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.ui.basic.Toast
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.Dialog.PortkeyDialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.ui.loading.Loading.PortkeyLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PortkeySDKViewStub() {
    SocialRecoveryModal()
    PortkeyLoading()
    PortkeyDialog()
}

fun initPortkeySDKViewStub(context: Context) {
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