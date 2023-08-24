package io.aelf.portkey.ui.rich_text

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.aelf.portkey.tools.friendly.DynamicWidth
import org.jetbrains.annotations.Contract

internal const val RICH_TEXT_DIVIDER = "#"

@Contract(pure = true)
@Composable
internal fun RichText(
    text: String,
    maxLine: Int = 10,
    normalTextStyle: SpanStyle = normalStyle,
    specialTextStyle: SpanStyle = specialStyle,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = 22.sp,
    modifier: Modifier = Modifier,
) {
    if (!text.contains(RICH_TEXT_DIVIDER)) {
        Text(text = text)
    } else {
        Text(
            text = buildAnnotatedString {
                val split = text.split(RICH_TEXT_DIVIDER)
                for (i in split.indices) {
                    val item = split[i]
                    withStyle(style = if (i % 2 == 0) normalTextStyle else specialTextStyle) {
                        append(item)
                    }
                }
            },
            maxLines = maxLine,
            textAlign = textAlign,
            modifier = modifier,
            lineHeight = lineHeight
        )
    }
}

private val normalStyle = SpanStyle(
    color = Color(0xFF8F949C),
    background = Color.Transparent,
    fontSize = 14.sp,
    fontWeight = FontWeight(400),
)

private val specialStyle = SpanStyle(
    color = Color(0xFF4285F4),
    background = Color.Transparent,
    fontSize = 14.sp,
    fontWeight = FontWeight(400),
)

@Preview
@Composable
private fun RichTextPreview() {
    RichText(
        text = "Welcome #Chara#, have a good day ! =) Welcome #Frisk#, have a good day ! -_-",
        modifier = Modifier.width(DynamicWidth(paddingHorizontal = 20))
    )
}