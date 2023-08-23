package io.aelf.portkey.tools.friendly

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.aelf.portkey.internal.tools.GsonProvider

internal fun toJson(obj: Any): String {
    return GsonProvider.getGson().toJson(obj)
}

internal const val NETWORK_TIMEOUT = 15 * 1000L

@Composable
internal fun DynamicWidth(paddingHorizontal: Int = 0): Dp {
    return (LocalConfiguration.current.screenWidthDp - 2 * paddingHorizontal).dp
}