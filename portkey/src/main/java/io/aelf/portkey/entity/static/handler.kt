package io.aelf.portkey.entity.static

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModalProps
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
        // TODO: Must get access_token from the backend, i will ask Hope Wang for help.
        SocialRecoveryModal.sendGoogleToken(googleSignInAccount)
    }
}