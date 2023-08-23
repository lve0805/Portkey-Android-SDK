package io.aelf.portkey.ui.basic

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HugeTitle(text:String){
    Text(
        text = text,
        style = TextStyle(
            fontSize = 20.sp,
            color = Color(0xFF162736),
            lineHeight = 28.sp
        ),
        fontWeight = FontWeight(700),
        textAlign = TextAlign.End
    )
}

@Composable
internal fun ErrorMsg(text:String){
    Text(
        text = text,
        style = TextStyle(
            fontSize = 12.sp,
            color = Color(0xFFFC5447),
            lineHeight = 18.sp
        ),
        fontWeight = FontWeight(400),
        textAlign = TextAlign.Start,
        modifier = Modifier
            .padding(top = 4.dp, bottom = 10.dp)
    )
}