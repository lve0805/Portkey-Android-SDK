package io.aelf.portkey.ui.basic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun Distance(width: Int = 0) {
    Box(modifier = Modifier
        .width(width.dp)
        .fillMaxHeight())
}