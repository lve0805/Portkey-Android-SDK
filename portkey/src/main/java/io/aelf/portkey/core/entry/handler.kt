package io.aelf.portkey.core.entry

import io.aelf.portkey.core.presenter.WalletLifecyclePresenter
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModal
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModalProps
import io.aelf.portkey.storage.StorageProvider
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.ui.loading.Loading
import io.aelf.portkey.utils.log.GLogger

object Portkey {
    fun callUpSocialRecoveryModel(props: SocialRecoveryModalProps) {
        SocialRecoveryModal.callUpModal(props)
    }


    fun forceCloseSocialRecoveryModel() {
        GLogger.w("forceCloseSocialRecoveryModel")
        SocialRecoveryModal.closeModal()
    }

    fun forceLogout() {
        WalletLifecyclePresenter.wallet?.disableWallet()
        WalletLifecyclePresenter.reset(saveWallet = false)
        StorageProvider.getHandler().clear()
    }

    fun getWallet() = WalletLifecyclePresenter.wallet

}

object PortkeyTest{
    fun showLoadingForTestOnly() {
        Loading.showLoading()
    }

    fun hideLoadingForTestOnly() {
        Loading.hideLoading()
    }

    fun showDialogForTestOnly(props: DialogProps) {
        Dialog.show(props)
    }
}