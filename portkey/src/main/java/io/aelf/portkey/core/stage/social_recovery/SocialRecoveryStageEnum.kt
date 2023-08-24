package io.aelf.portkey.core.stage.social_recovery

enum class SocialRecoveryStageEnum {
    // Initial state, or have just logout
    INIT,
    // Did not detect the existing account, ready to register
    READY_TO_REGISTER,
    // This account has been registered, ready to login
    READY_TO_LOGIN,
    // Set pin and get the wallet
    SET_PIN,
    // Active wallet is in use
    ACTIVE,

    // Trying to unlock wallet, if succeed, will enter ACTIVE
    UNLOCK
}

