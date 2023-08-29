package io.aelf.portkey

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

private const val TOKEN = "176147744733-918t0kh5n0jnq2u3a757adk05kqfqc3k.apps.googleusercontent.com"

internal fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestServerAuthCode(TOKEN)
        .requestEmail()
        .build()
    // Build a GoogleSignInClient with the options specified by gso.
    return GoogleSignIn.getClient(context, gso)
}