package io.aelf.portkey.ui.button

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.component.global.NullableTools.dpOrDefault

@Composable
fun HugeButton(config: ButtonConfig, enable: Boolean = true) {
    Button(config.apply {
        height = 50.dp
        width = dpOrDefault(DynamicWidth(20), 320.dp)
    }, enable)
}

@Composable
fun MediumButton(config: ButtonConfig, enable: Boolean = true) {
    Button(config.apply {
        height = 44.dp
        width = 140.dp
    }, enable)
}

@Composable
fun TinyButton(config: ButtonConfig, enable: Boolean = true) {
    Button(config.apply {
        height = 38.dp
        width = 62.dp
        fontSize = 12.sp
        lineHeight = 22.sp
    }, enable)
}

@Composable
private fun Button(config: ButtonConfig, enable: Boolean) {
    TextButton(
        enabled = enable,
        onClick = config.onClick,
        modifier = Modifier
            .height(config.height)
            .width(config.width),
        colors = ButtonDefaults.textButtonColors(
            containerColor = config.bgColor,
            disabledContainerColor = Color(0xFF8F949C).copy(alpha = 0.1F)
        ),
        border = BorderStroke(config.borderWidth, config.borderColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (config.iconPath != -1 && config.iconSize > 0) {
            Image(
                painter = painterResource(id = config.iconPath),
                contentDescription = null,
                modifier = Modifier
                    .width(config.iconSize.dp)
                    .height(config.iconSize.dp)
                    .background(Color.Transparent)
                    .padding(end = 10.dp)
            )
        }
        Text(
            text = config.text,
            style = TextStyle(
                color = if (enable) config.textColor else Color(0xFF8F949C),
                fontSize = config.fontSize,
                fontWeight = FontWeight(config.fontWeight),
                lineHeight = config.lineHeight
            )
        )
    }
}


open class ButtonConfig {
    var text: String = "Yes"
    @DrawableRes
    var iconPath: Int = -1
    var iconSize: Int = 0
    var bgColor: Color = Color(0xFF4285F4)
    var textColor: Color = Color.White
    var fontWeight: Int = 500
    var fontSize: TextUnit = 14.sp
    var lineHeight: TextUnit = 22.sp
    var borderWidth: Dp = 1.dp
    var borderColor: Color = Color(0xFFEDEFF5)
    var hoverColor: Color = Color(0xCC4285F4)
    var onClick: () -> Unit = {}
    var height = (-1).dp
    var width = (-1).dp
}

@Preview
@Composable
fun PreviewButton() {
    val context = LocalContext.current
    Column {
        HugeButton(ButtonConfig().apply {
            onClick = {
                Toast.makeText(context, "Huge", Toast.LENGTH_SHORT).show()
            }
        })
        MediumButton(ButtonConfig().apply {
            onClick = {
                Toast.makeText(context, "Medium", Toast.LENGTH_SHORT).show()
            }
        })
        TinyButton(ButtonConfig().apply {
            onClick = {
                Toast.makeText(context, "Tiny", Toast.LENGTH_SHORT).show()
            }
        })
    }
}