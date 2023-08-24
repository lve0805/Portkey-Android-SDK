package io.aelf.portkey.ui.basic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

open class Style {
    var marginLeft: Dp = 0.dp
    var marginRight: Dp = 0.dp
    var marginTop: Dp = 0.dp
    var marginBottom: Dp = 0.dp
    var paddingLeft: Dp = 0.dp
    var paddingRight: Dp = 0.dp
    var paddingTop: Dp = 0.dp
    var paddingBottom: Dp = 0.dp
    var width: Dp = 0.dp
    var height: Dp = 0.dp
    var backgroundColor: Color = Color.Black
    var size: Dp = 0.dp
    var text: String = ""
    var textColor: Color = Color.Black
    var bgImage: String = "undefined"
}

internal val wrapperStyle = Style().apply {
    width = 320.dp
    height = 136.dp
}

internal fun isValid(num: Dp): Boolean {
    return num.value > 0
}

internal fun isValid(str: String?): Boolean {
    return !str.isNullOrEmpty()
}

enum class ZIndexConfig(private val z: Float) {
    Loading(100f),
    Dialog(50f),
    SubIcon(18f),
    MainIcon(15f),
    Modal(10f);
    internal fun getZIndex(): Float {
        return z
    }
}