package io.aelf.portkey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import ca.CaContract
import ca.CaContract.GetHolderInfoInput
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import io.aelf.portkey.behaviour.wallet.callCAContractMethod
import io.aelf.portkey.component.stub.PortkeySDKViewStub
import io.aelf.portkey.core.entry.Portkey
import io.aelf.portkey.core.entry.PortkeyTest
import io.aelf.portkey.entity.social_recovery.SocialRecoveryModalProps
import io.aelf.portkey.init.InitProcessor
import io.aelf.portkey.init.SDkInitConfig
import io.aelf.portkey.internal.tools.GlobalConfig
import io.aelf.portkey.network.retrofit.RetrofitProvider
import io.aelf.portkey.tools.contract.toAElfBytes
import io.aelf.portkey.tools.contract.toAElfHash
import io.aelf.portkey.tools.friendly.UseComponentDidMount
import io.aelf.portkey.tools.friendly.getCurrentStorageHandler
import io.aelf.portkey.tools.friendly.getValueCoroutine
import io.aelf.portkey.tools.friendly.putValueCoroutine
import io.aelf.portkey.ui.basic.Toast
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.HugeButton
import io.aelf.portkey.ui.dialog.DialogProps
import io.aelf.portkey.utils.log.GLogger
import io.aelf.utils.AElfException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SELECTED_ENVIRONMENT_TAG = "selected_environment_name"

private var currentEnvironmentUsing = "None"
    set(value) {
        if (field != "None") {
            Portkey.forceLogout()
            getCurrentStorageHandler().apply {
                putValueCoroutine(SELECTED_ENVIRONMENT_TAG, value)
            }
        }
        field = value
        RetrofitProvider.resetOrInitMainRetrofit(
            when (value) {
                "MainNet" -> "https://did-portkey.portkey.finance"
                "TestNet" -> "https://did-portkey-test.portkey.finance"
                "Test1" -> "https://localtest-applesign.portkey.finance"
                "Test2" -> "https://localtest-applesign2.portkey.finance"
                else -> "https://testnet-applesign.portkey.finance"
            }
        )
    }


@Suppress("DEPRECATION")
class MainActivity : FragmentActivity() {
    private var googleAuthLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
    private var googleSignInCallback: (GoogleSignInAccount?) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EntryComponent()
        }
    }

    @Composable
    fun EntryComponent() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        UseComponentDidMount {
            initDebug(context)
            recoverEnvironment()
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
            SocialRecoveryModalProps(
                onUserCancel = {
                    GLogger.w("onUserCancel")
                    showToast("onUserCancel")
                },
                onSuccess = {
                    GLogger.w("onSuccess")
                    showToast("onSuccess")
                },
                onError = {
                    GLogger.e("onError", it)
                    showToast("onError:${it.message}")
                },
                onUseGoogleAuthService = {
                    googleSignInCallback = it
                    useGoogleLogin(context)
                }
            )
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            PortkeySDKViewStub()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
                    .zIndex(1F)
                    .padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top)
            ) {
                HugeButton(
                    config = ButtonConfig().apply {
                        text = "Call Up Dialog"
                        onClick = callUp@{
                            if (Portkey.getWallet() != null) {
                                showDialogWarning("Wallet already exists.")
                                return@callUp
                            }
                            Portkey.callUpSocialRecoveryModel(props)
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
                ServiceEnvironment()
                remember { listOf(1, 2, 3) }.map {
                    HugeButton(config = ButtonConfig().apply {
                        text = "Call CA Contract Method $it"
                        onClick = {
                            callContractMethods(it, scope, ::showToast)
                        }
                    })
                }
                HugeButton(
                    config = ButtonConfig().apply {
                        text = "Goto Guardian Page"
                        onClick = {
                            jumpToGuardianActivity()
                        }
                    }
                )
            }
        }
    }

    private fun recoverEnvironment() {
        getCurrentStorageHandler().apply {
            getValueCoroutine(SELECTED_ENVIRONMENT_TAG) {
                currentEnvironmentUsing = if (TextUtils.isEmpty(it)) "Test2" else it!!
            }
        }
    }

    private fun callContractMethods(
        flavour: Int,
        scope: CoroutineScope,
        showToast: (String) -> Unit
    ) {
        val wallet = Portkey.getWallet()
        if (wallet == null || wallet.caInfo == null) {
            showDialogWarning()
            return
        }
        PortkeyTest.showLoadingForTestOnly()
        scope.launch(Dispatchers.IO) {
            try {
                val result = when (flavour) {
                    1 -> {
                        wallet.callCAContractMethod(
                            methodName = "GetVerifierServers",
                            isViewMethod = true,
                            parser = {
                                CaContract.GetVerifierServersOutput.parseFrom(
                                    it.toAElfBytes()
                                )
                            }
                        )
                    }

                    2 -> {
                        val input = GetHolderInfoInput
                            .newBuilder()
                            .setCaHash(wallet.caInfo.caHash.toAElfHash(hashAgain = false))
                            .build()
                        wallet.callCAContractMethod(
                            methodName = "GetHolderInfo",
                            isViewMethod = true,
                            params = input,
                            parser = {
                                CaContract.GetHolderInfoOutput.parseFrom(
                                    it.toAElfBytes()
                                )
                            }
                        )
                    }

                    3 -> {
                        wallet.callCAContractMethod(
                            methodName = "GetCAServers",
                            isViewMethod = true,
                            parser = {
                                CaContract.GetCAServersOutput.parseFrom(
                                    it.toAElfBytes()
                                )
                            }
                        )
                    }

                    else -> {
                        "unKnown"
                    }
                }
                PortkeyTest.showDialogForTestOnly(
                    DialogProps().apply {
                        mainTitle = "Result"
                        subTitle = (result ?: "the server returned nothing.").toString()
                        useSingleConfirmButton = true
                    }
                )
            } catch (e: Throwable) {
                showToast("Failed to call CA method, check the console.")
                GLogger.e("Failed to call CA method.", AElfException(e))
            }
            PortkeyTest.hideLoadingForTestOnly()
        }
    }

    private fun jumpToGuardianActivity() {
        if (Portkey.getWallet() == null) {
            showDialogWarning()
            return
        }
        val intent = Intent(this, GuardianActivity::class.java)
        startActivity(intent)
    }

    private fun showDialogWarning(
        text: String = "Wallet is not active."
    ) {
        PortkeyTest.showDialogForTestOnly(
            DialogProps().apply {
                mainTitle = "Error"
                subTitle = text
                useSingleConfirmButton = true
            }
        )
    }

    @Composable
    private fun ServiceEnvironment() {
        val context = LocalContext.current
        val data = mutableListOf(
            "MainNet",
            "TestNet",
            "Test1",
            "Test2"
        )
        var expand by remember {
            mutableStateOf(false)
        }
        Box(
            modifier = Modifier
                .height(50.dp)
                .width((LocalConfiguration.current.screenWidthDp - 20 * 2).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF4285F4))
                .padding(vertical = 10.dp)
                .zIndex(1F),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Select Environment", modifier = Modifier.clickable click@{
                expand = !expand
            }, color = Color.White)
            DropdownMenu(
                expanded = expand,
                onDismissRequest = { expand = false },
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            ) {
                data.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = it.plus(if (currentEnvironmentUsing == it) "  ✅" else ""),
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .wrapContentWidth()
                            )
                        },
                        onClick = {
                            expand = false
                            fun execute() {
                                currentEnvironmentUsing = it
                                Toast.showToast(
                                    context,
                                    "\nInit with:$it .\nWallet is also reset now."
                                )
                            }
                            PortkeyTest.showDialogForTestOnly(
                                DialogProps().apply {
                                    mainTitle = "Confirm"
                                    subTitle =
                                        "Are you sure to switch to $it ? Your wallet will be erased and have to start over."
                                    useSingleConfirmButton = false
                                    positiveCallback = {
                                        execute()
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
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

    private fun thenSignOut(context: Context) {
        val client = getGoogleSignInClient(context = context)
        client.signOut()
    }

    private fun handleGoogleAuthResult(result: ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            GLogger.w("signInResult:success, account: $account")
            googleSignInCallback(account)
        } else {
            GLogger.e("login failed.")
            result.data?.extras?.get(result.data?.extras?.keySet()?.elementAt(0))?.let {
                GLogger.e("status: $it}")
            }
            googleSignInCallback(null)
        }
        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            thenSignOut(context = this@MainActivity)
        }
    }
}


