package io.aelf.portkey.entity.social_recovery.stage.verify

import androidx.compose.runtime.Composable


@Composable
internal fun RegisterPage() {
    // Only one guardian is in this page, so we only need to enter GuardianPage
    GuardianPage()
}