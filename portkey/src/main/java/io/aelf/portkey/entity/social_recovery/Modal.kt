package io.aelf.portkey.entity.social_recovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.wewox.modalsheet.ExperimentalSheetApi
import eu.wewox.modalsheet.ModalSheet

internal object Modal {
    private var isShow by mutableStateOf(false)

    @OptIn(ExperimentalSheetApi::class)
    @Composable
    internal fun SocialRecoveryModal() {
        ModalSheet(visible = isShow, onVisibleChange = {}) {

        }
    }
}