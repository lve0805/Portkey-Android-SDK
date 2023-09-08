package io.aelf.portkey.tools.friendly

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.aelf.portkey.internal.model.google.GoogleAccount
import io.aelf.portkey.internal.tools.GsonProvider
import io.aelf.portkey.storage.IStorageBehaviour
import io.aelf.portkey.storage.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal const val NETWORK_TIMEOUT = 15 * 1000L

@Composable
internal fun DynamicWidth(paddingHorizontal: Int = 0): Dp {
    return (LocalConfiguration.current.screenWidthDp - 2 * paddingHorizontal).dp
}

internal fun convertGoogleAccount(
    googleAccount: GoogleSignInAccount,
    givenAccountToken: String
): GoogleAccount {
    return GoogleAccount().apply {
        id = googleAccount.id
        email = googleAccount.email
        idToken = googleAccount.idToken
        accessToken = givenAccountToken
    }
}

fun getCurrentStorageHandler(): IStorageBehaviour = StorageProvider.getHandler()

fun IStorageBehaviour.putValueCoroutine(
    key: String,
    value: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    scope.launch {
        putValue(key, value)
    }
}

fun IStorageBehaviour.getValueCoroutine(
    key: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    callback: (String?) -> Unit
) {
    scope.launch {
        val res = getValue(key)
        callback(res)
    }
}