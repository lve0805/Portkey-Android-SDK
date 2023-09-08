package io.aelf.portkey.tools.friendly

import android.graphics.Rect
import android.os.Build
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun UseComponentDidMount(callback: () -> Unit) {
    LaunchedEffect(Unit) {
        callback()
    }
}

@Composable
fun UseEffect(vararg dependency: Any?, callback: () -> Unit) {
    val den: Any = if (dependency.isEmpty()) Unit else dependency
    LaunchedEffect(den) {
        callback()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun useKeyboardVisibleState(): State<Boolean> {
    val keyboardVisible = remember {
        mutableStateOf(false)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val view = LocalView.current
        DisposableEffect(view) {
            val onKeyboardChangeListener = ViewTreeObserver.OnGlobalLayoutListener {
                val rect = Rect()
                view.getWindowVisibleDisplayFrame(rect)
                val screenHeight = view.rootView.height
                val keypadHeight = screenHeight - rect.bottom
                keyboardVisible.value = keypadHeight > screenHeight * 0.15
            }
            view.viewTreeObserver.addOnGlobalLayoutListener(onKeyboardChangeListener)
            onDispose {
                view.viewTreeObserver.removeOnGlobalLayoutListener(onKeyboardChangeListener)
            }
        }
    } else {
        keyboardVisible.value = WindowInsets.isImeVisible
    }
    return keyboardVisible
}

@Composable
fun UseComponentWillUnmount(callback: () -> Unit) {
    DisposableEffect(Unit) {
        onDispose {
            callback()
        }
    }
}

@Composable
fun UseAndroidBackButtonSettings(callback: () -> Unit) {
    val backPressCallback = remember {
        return@remember object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callback()
            }
        }
    }
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    DisposableEffect(key1 = Unit, effect = {
        dispatcher?.addCallback(backPressCallback)
        onDispose {
            backPressCallback.remove()
        }
    })
}
