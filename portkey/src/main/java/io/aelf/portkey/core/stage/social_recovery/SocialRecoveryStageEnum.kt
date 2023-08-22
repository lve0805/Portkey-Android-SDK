package io.aelf.portkey.core.stage.social_recovery

enum class SocialRecoveryStageEnum {
    // Initial state, or have just logout
    INIT,
    // User has entered the login identification
    ENTERED,
    // Did not detect the existing account, ready to register
    READY_TO_REGISTER,
    // This account has been registered, ready to login
    READY_TO_LOGIN,
    // All the required guardians have been fulfilled, ready to set pin
    LOGIN_GUARDIAN_FULFILLED,
    // All the required guardians have been fulfilled, ready to set pin
    REGISTER_GUARDIAN_FULFILLED,
    // Set pin and get the wallet
    SET_PIN,
    // Active wallet is in use
    ACTIVE
}

