package io.aelf.portkey.core.presenter

import android.text.TextUtils
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.aelf.portkey.behaviour.entry.EntryBehaviourEntity
import io.aelf.portkey.behaviour.guardian.GuardianBehaviourEntity
import io.aelf.portkey.behaviour.login.LoginBehaviourEntity
import io.aelf.portkey.behaviour.pin.SetPinBehaviourEntity
import io.aelf.portkey.behaviour.register.RegisterBehaviourEntity
import io.aelf.portkey.behaviour.wallet.PortkeyWallet
import io.aelf.portkey.core.stage.social_recovery.SocialRecoveryStageEnum

internal object WalletLifecyclePresenter {

    internal var entry: EntryBehaviourEntity.CheckedEntry? by mutableStateOf(null)
    internal var login: LoginBehaviourEntity? by mutableStateOf(null)
    internal var register: RegisterBehaviourEntity? by mutableStateOf(null)
    internal var activeGuardian: GuardianBehaviourEntity? by mutableStateOf(null)
    internal var setPin: SetPinBehaviourEntity? by mutableStateOf(null)
    internal var wallet: PortkeyWallet? by mutableStateOf(null)

    internal const val PIN_LENGTH = 6

    internal var stageEnum by mutableStateOf(SocialRecoveryStageEnum.INIT)

    internal fun reset() {
        entry = null
        login = null
        register = null
        activeGuardian = null
        setPin = null
        wallet = null
        inferCurrentStage()
    }

    internal fun getStage(): SocialRecoveryStageEnum {
        inferCurrentStage()
        return stageEnum
    }

    private fun inferCurrentStage() {
        if (wallet != null) {
            stageEnum = SocialRecoveryStageEnum.ACTIVE
            return
        } else if (setPin != null) {
            stageEnum = SocialRecoveryStageEnum.SET_PIN
            return
        } else if (entry == null) {
            stageEnum = SocialRecoveryStageEnum.INIT
            return
        } else {
            if (login != null) {
                stageEnum =
                    if (login!!.isFulfilled) SocialRecoveryStageEnum.LOGIN_GUARDIAN_FULFILLED else SocialRecoveryStageEnum.READY_TO_LOGIN
            } else if (register != null) {
                stageEnum =
                    if (register!!.isVerified) SocialRecoveryStageEnum.REGISTER_GUARDIAN_FULFILLED else SocialRecoveryStageEnum.READY_TO_REGISTER
            }
        }
        stageEnum = SocialRecoveryStageEnum.ENTERED
    }

    internal fun detectCachedWallet(): Boolean {
        return EntryBehaviourEntity.ifLockedWalletExists()
    }

    internal fun headPin(pin: String): Boolean {
        if (!detectCachedWallet()) return false
        val unlockEntity = EntryBehaviourEntity.attemptToGetLockedWallet()
        if (!unlockEntity.isPresent) return false
        return unlockEntity.get().checkPin(pin)
    }


    internal fun unlockWallet(pin: String): PortkeyWallet? {
        if (!checkPin(pin)) {
            return null
        }
        EntryBehaviourEntity.attemptToGetLockedWallet().ifPresent {
            try {
                wallet = it.unlockAndBuildWallet(pin)
            } catch (ignored: Throwable) {
            }
        }
        inferCurrentStage()
        return wallet
    }

    internal fun checkPin(pin: String): Boolean {
        if (TextUtils.isEmpty(pin)) {
            return false
        }
        return pin.length == PIN_LENGTH
    }


}






