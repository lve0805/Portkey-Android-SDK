package io.aelf.portkey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import io.aelf.portkey.component.stub.PortkeySDKViewStub
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModalProps
import io.aelf.portkey.entity.static.Portkey
import io.aelf.portkey.init.InitProcessor
import io.aelf.portkey.init.SDkInitConfig
import io.aelf.portkey.internal.tools.GlobalConfig
import io.aelf.portkey.network.retrofit.RetrofitProvider
import io.aelf.portkey.tools.biometric.launchBiometricVerify
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.ui.basic.Toast
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.HugeButton
import io.aelf.portkey.utils.log.GLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : FragmentActivity() {
    private var googleAuthLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModalPreview()
        }
    }

    @Composable
    fun ModalPreview() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        UseComponentDidMount {
            initDebug(context)
        }
        fun showToast(msg: String) {
            scope.launch(Dispatchers.Main) {
                Toast.showToast(context, msg)
            }
        }
        googleAuthLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = {
                    handleGoogleAuthResult(it)
                }
            )
        val props = remember {
            SocialRecoveryModalProps().apply {
                onUserCancel = {
                    GLogger.w("onUserCancel")
                    showToast("onUserCancel")
                }
                onSuccess = {
                    GLogger.w("onSuccess")
                    showToast("onSuccess")
                }
                onError = {
                    GLogger.e("onError", it)
                    showToast("onError:${it.message}")
                }
                onUseGoogleAuthService = {
                    useGoogleLogin(context)
                }
            }
        }
        Column() {
            HugeButton(
                config = ButtonConfig().apply {
                    text = "Call Up Dialog"
                    onClick = {
                        Portkey.callUpSocialRecoveryModel(props)
                    }
                }
            )
            HugeButton(
                config = ButtonConfig().apply {
                    text = "Call Up Biometric"
                    onClick = {
                        launchBiometricVerify(
                            context = this@MainActivity,
                            success = { it: BiometricPrompt.AuthenticationResult ->
                                GLogger.w("onSuccess: $it")
                            })
                    }
                }
            )
            HugeButton(
                config = ButtonConfig().apply {
                    text = "Clear Wallet"
                    onClick = {
                        Portkey.forceLogout()
                        showToast("Wallet removed.")
                    }
                }
            )
        }
        PortkeySDKViewStub()
    }

    internal fun initDebug(context: Context) {
        InitProcessor.init(SDkInitConfig.Builder().build(), context)
        RetrofitProvider.resetOrInitMainRetrofit("https://localtest-applesign2.portkey.finance")
        GlobalConfig.setTestEnv(true)
    }

    private fun useGoogleLogin(context: Context) {
        val client = getGoogleSignInClient(context = context)
        val signInIntent = client.signInIntent
        googleAuthLauncher?.launch(signInIntent)
    }

    private fun handleGoogleAuthResult(result: ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            GLogger.w("signInResult:success, account: $account")
            Portkey.sendGoogleAuthResult(account)
        } else {
            GLogger.e("login failed.")
            result.data?.extras?.get(result.data?.extras?.keySet()?.elementAt(0))?.let {
                GLogger.e("status: $it}")
            }
            Portkey.sendGoogleAuthResult(null)
        }
    }
}


