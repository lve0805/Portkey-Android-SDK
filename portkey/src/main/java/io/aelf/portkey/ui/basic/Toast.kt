package io.aelf.portkey.ui.basic

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object Toast {
    @Composable
    fun showToast(text: String) {
        val context = LocalContext.current
        android.widget.Toast.makeText(
            context,
            text,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun showToast(context: Context, text: String) {
        android.widget.Toast.makeText(
            context,
            text,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
