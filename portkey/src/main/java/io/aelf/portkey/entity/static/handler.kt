package io.aelf.portkey.entity.static

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModalProps
import io.aelf.portkey.storage.StorageProvider
import io.aelf.portkey.utils.log.GLogger

object Portkey {
    fun callUpSocialRecoveryModel(props: SocialRecoveryModalProps) {
        SocialRecoveryModal.callUpModal(props)
    }


    fun forceCloseSocialRecoveryModel() {
        GLogger.w("forceCloseSocialRecoveryModel")
        SocialRecoveryModal.closeModal()
    }

    fun sendGoogleAuthResult(googleSignInAccount: GoogleSignInAccount?) {
        SocialRecoveryModal.sendGoogleToken(googleSignInAccount)
    }

    fun forceLogout() {
        WalletLifecyclePresenter.reset(saveWallet = false)
        StorageProvider.getHandler().clear()
    }
}