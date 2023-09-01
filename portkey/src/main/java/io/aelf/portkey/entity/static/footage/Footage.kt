package io.aelf.portkey.entity.static.footage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.aelf.portkey.sdk.R

@Composable
internal fun PortkeyFootage() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.portkey_footage),
            contentDescription = "portkey wallet's icon",
            tint = Color(0xFFDFE4EC)
        )
    }
}

@Preview
@Composable
fun PortkeyFootagePreview() {
    PortkeyFootage()
}