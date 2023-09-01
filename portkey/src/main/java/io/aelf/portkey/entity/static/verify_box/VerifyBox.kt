package io.aelf.portkey.entity.static.verify_box

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.tools.friendly.UseKeyboardVisibleState

@Composable
private fun VerifyCodeInputBoxItem(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    color: Color = Color(0xFF162736),
    fontWeight: FontWeight = FontWeight(700),
    text: String = "1",
    showAsCursor: Boolean = false
) {
    Box(
        modifier = modifier
            .background(color = Color(0xFFFCFCFF), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFFEDEFF5), shape = RoundedCornerShape(8.dp))
    ) {
        if (showAsCursor) {
            // TODO animation should be added here
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .align(Alignment.Center)
                    .background(Color(0xFF4285F4))
            )
        } else {
            Text(
                text = text,
                fontSize = fontSize,
                color = color,
                fontWeight = fontWeight,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
internal fun useVerifyCodeInputBox(
    modifier: Modifier = Modifier,
    size: Int = 6,
    boxSpacerSize: Dp = 6.4.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    provideItem: @Composable RowScope.(text: String, showAsCursor: Boolean) -> Unit = { text: String, showAsCursor: Boolean ->
        VerifyCodeInputBoxItem(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxHeight(),
            text = text,
            showAsCursor = showAsCursor
        )
    },
    enable: Boolean = true,
    onTextChange: (String) -> Unit = {}
): VerifyCodeInputBoxInterface {
    var code by remember {
        mutableStateOf(
            TextFieldValue(text = "", selection = TextRange(Int.MAX_VALUE))
        )
    }
    val clearCodeInput: () -> Unit = {
        code = TextFieldValue(text = "", selection = TextRange(Int.MAX_VALUE))
    }

    val keyboardFocused by UseKeyboardVisibleState()
    BasicTextField(
        value = code,
        onValueChange = {
            if (it.text.length < code.text.length) {
                code = TextFieldValue(
                    code.text.substring(0, code.text.lastIndex),
                    selection = TextRange(Int.MAX_VALUE)
                )
                onTextChange(code.text)
            } else if (it.text.length <= size) {
                code = TextFieldValue(text = it.text, selection = TextRange(Int.MAX_VALUE))
                onTextChange(code.text)
            }
        },
        enabled = enable,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        decorationBox = {
            Box(modifier = Modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(boxSpacerSize)
                ) {
                    for (i in 0 until size) {
                        provideItem(
                            if (code.text.length > i) code.text.substring(i, i + 1) else "",
                            keyboardFocused && i == code.text.length
                        )
                    }
                }
            }
        }
    )
    return object : VerifyCodeInputBoxInterface {
        override val clearInput: () -> Unit
            get() = clearCodeInput
    }
}

internal interface VerifyCodeInputBoxInterface {
    val clearInput: () -> Unit
}

@Preview
@Composable
private fun VerifyBoxPreview() {
    useVerifyCodeInputBox(
        modifier = Modifier
            .height(56.dp)
            .width(DynamicWidth(paddingHorizontal = 20))
    )
}