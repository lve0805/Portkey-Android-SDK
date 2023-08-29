package io.aelf.portkey.ui.basic

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Toast {
    @Composable
    fun Toast(text: String) {
        val context = LocalContext.current
        android.widget.Toast.makeText(
            context,
            text,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun showToast(context: Context, text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            android.widget.Toast.makeText(
                context,
                text,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}
