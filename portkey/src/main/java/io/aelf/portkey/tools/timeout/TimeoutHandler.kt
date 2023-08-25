package io.aelf.portkey.tools.timeout

import io.aelf.portkey.tools.friendly.NETWORK_TIMEOUT
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

internal suspend fun useTimeout(duration: Long = NETWORK_TIMEOUT, job: Job, restart: () -> Unit) {
    delay(duration)
    if (job.isActive) {
        Loading.hideLoading()
        job.cancel()
        Dialog.show(DialogProps().apply {
            mainTitle = "Network timeout"
            subTitle = "It seems that we are facing some network issues, please try again later."
            positiveText = "Try again"
            negativeText = "Cancel"
            positiveCallback = {
                restart()
            }
        })
    }
}