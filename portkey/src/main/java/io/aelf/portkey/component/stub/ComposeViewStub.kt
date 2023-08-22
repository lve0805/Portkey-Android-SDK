package io.aelf.portkey.component.stub

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import io.aelf.portkey.entity.social_recovery.Modal.SocialRecoveryModal
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.ui.dialog.Dialog.Dialog
import io.aelf.portkey.ui.loading.Loading.Loading
import io.aelf.portkey.utils.log.GLogger

internal var stubGenerated = false
internal const val initWarning =
    "You seems to have generated the stub twice, this is unexpected and you'd better check it."

@Composable
fun PortkeySDKViewStub() {
    UseComponentDidMount {
        if (stubGenerated) {
            GLogger.e(initWarning)
        }
        stubGenerated = true
    }
    SocialRecoveryModal()
    Loading()
    Dialog()
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