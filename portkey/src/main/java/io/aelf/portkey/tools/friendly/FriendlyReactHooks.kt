package io.aelf.portkey.tools.friendly

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

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
    val callback = remember {
        return@remember object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callback()
            }
        }
    }
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    DisposableEffect(key1 = Unit, effect = {
        dispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    })
}

@Composable
fun <T> UseState(initValue: T): MutableState<T> {
    return remember {
        mutableStateOf(initValue)
    }
}
