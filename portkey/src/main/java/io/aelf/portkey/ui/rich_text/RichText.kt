package io.aelf.portkey.ui.rich_text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aelf.portkey.tools.friendly.dynamicWidth
import io.aelf.portkey.ui.dialog.Dialog
import io.aelf.portkey.ui.dialog.Dialog.PortkeyDialog
import io.aelf.portkey.ui.dialog.DialogProps
import org.jetbrains.annotations.Contract

internal const val RICH_TEXT_DIVIDER = "#"

@Contract(pure = true)
@Composable
internal fun RichText(
    modifier: Modifier = Modifier,
    text: String,
    maxLine: Int = 10,
    normalTextStyle: SpanStyle = normalStyle,
    specialTextStyle: SpanStyle = specialStyle,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = 22.sp,
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

@Contract(pure = true)
@Composable
internal fun RichText(
    modifier: Modifier = Modifier,
    text: RichTextDescriber,
    maxLine: Int = 10,
    normalTextStyle: SpanStyle = normalStyle,
    specialTextStyle: SpanStyle = specialStyle,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = 22.sp,
) {
    RichText(
        modifier = modifier,
        text = text.text,
        maxLine = maxLine,
        normalTextStyle = normalTextStyle,
        specialTextStyle = specialTextStyle,
        textAlign = textAlign,
        lineHeight = lineHeight
    )
}

@Composable
internal fun RichTextClickable(
    modifier: Modifier = Modifier,
    text: List<RichTextClickable>,
    maxLine: Int = 10,
    normalTextStyle: SpanStyle = normalStyle,
    specialTextStyle: SpanStyle = specialStyle,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = 22.sp,
    onClick: (tag: String) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        text.forEach { item ->
            val contentsTag = item.tag != "None"
            withStyle(style = if (contentsTag) specialTextStyle else normalTextStyle) {
                if (contentsTag) {
                    pushStringAnnotation("tag", item.tag)
                    append(item.text)
                    pop()
                } else {
                    append(item.text)
                }
            }
        }
    }

    ClickableText(
        text = annotatedString,
        maxLines = maxLine,
        modifier = modifier,
        onClick = { offset ->
            // Get the tag of the clicked text
            val annotations = annotatedString.getStringAnnotations("tag", offset, offset)
            if (annotations.isNotEmpty()) {
                onClick(annotations[0].item)
            }
        },
        style = TextStyle.Default.copy(lineHeight = lineHeight, textAlign = textAlign)
    )
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

internal data class RichTextDescriber(
    val text: String,
    val isSpecialText: Boolean = false
)

internal data class RichTextClickable(
    val text: String,
    val tag: String = "None",
)

internal infix fun RichTextDescriber.with(another: RichTextDescriber): RichTextDescriber {
    fun RichTextDescriber.wrap(): String {
        return if (this.isSpecialText) {
            RICH_TEXT_DIVIDER + this.text + RICH_TEXT_DIVIDER
        } else {
            this.text
        }
    }
    return RichTextDescriber(
        text = this.wrap() + another.wrap(),
        isSpecialText = false
    )
}


@Preview
@Composable
private fun RichTextPreview() {
    Column(
        modifier = Modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RichText(
            text = "Howdy #Chara#, have a good day ! =)\n Welcome #Frisk#, have a good day ! -_-",
            modifier = Modifier.width(dynamicWidth(paddingHorizontal = 20))
        )
        RichText(
            text = RichTextDescriber("Howdy ", isSpecialText = true)
                    with RichTextDescriber("Chara", isSpecialText = false)
                    with RichTextDescriber(
                ", have a good day ! =)\n Welcome ",
                isSpecialText = true
            )
                    with RichTextDescriber("Frisk", isSpecialText = false)
                    with RichTextDescriber(", have a good day ! -_-", isSpecialText = true),
            modifier = Modifier.width(dynamicWidth(paddingHorizontal = 20))
        )
        RichTextClickable(
            text = listOf(
                RichTextClickable("Howdy "),
                RichTextClickable("Chara", tag = "Chara"),
                RichTextClickable(", click to read your mind ! =)\n Welcome "),
                RichTextClickable("Frisk", tag = "Frisk"),
                RichTextClickable(", click to read your mind ! -_-")
            ),
            modifier = Modifier.width(dynamicWidth(paddingHorizontal = 20)),
            textAlign = TextAlign.Center,
            onClick = { tag ->
                Dialog.show(
                    DialogProps().apply {
                        mainTitle = "Mind Reader"
                        subTitle =
                            "$tag : Howdy! Your favourite food is : ${if (tag == "Chara") "Chocolate" else "Butterscotch Pie"} !"
                        useSingleConfirmButton = true
                        positiveText = "OK"
                    }
                )
            }
        )
    }
    PortkeyDialog()
}