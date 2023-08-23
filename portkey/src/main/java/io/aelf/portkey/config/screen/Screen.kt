package io.aelf.portkey.config.screen

import android.content.res.Configuration
import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal object Screen {

    internal var SCREEN_WIDTH = 0
    internal var SCREEN_HEIGHT = 0
    private val observers: MutableSet<WeakReference<ScreenPropsObserver>> = mutableSetOf()

    internal fun subscribe(observer: ScreenPropsObserver) {
        observers.add(WeakReference(observer))
    }

    internal fun unsubscribe(observer: ScreenPropsObserver) {
        observers.removeIf { it.get() == observer }
    }

    @Synchronized
    internal fun updateScreenProps(config: Configuration, scope: CoroutineScope) {
        val widthDp = config.screenWidthDp
        val heightDp = config.screenHeightDp
        if (widthDp != SCREEN_WIDTH || heightDp != SCREEN_HEIGHT) {
            SCREEN_WIDTH = widthDp
            SCREEN_HEIGHT = heightDp
            scope.launch {
                informScreenPropsChanged()
            }
        }
    }

    @UiThread
    internal suspend fun informScreenPropsChanged() {
        for (observer in observers) {
            observer.get()?.onScreenPropsChanged()
        }
    }

}

internal interface ScreenPropsObserver {
    fun onScreenPropsChanged()
}