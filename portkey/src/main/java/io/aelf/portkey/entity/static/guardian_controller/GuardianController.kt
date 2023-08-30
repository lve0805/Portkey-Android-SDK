package io.aelf.portkey.entity.static.guardian_controller

import android.text.TextUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import io.aelf.portkey.behaviour.guardian.GuardianBehaviourEntity
import io.aelf.portkey.internal.model.common.AccountOriginalType
import io.aelf.portkey.internal.model.guardian.GuardianDTO
import io.aelf.portkey.sdk.R
import io.aelf.portkey.tools.friendly.DynamicWidth
import io.aelf.portkey.ui.basic.Distance
import io.aelf.portkey.ui.basic.ZIndexConfig
import io.aelf.portkey.ui.button.ButtonConfig
import io.aelf.portkey.ui.button.TinyButton
import io.aelf.portkey.utils.log.GLogger

@Composable
internal fun GuardianController(info: GuardianInfo, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(DynamicWidth(paddingHorizontal = 20))
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF7F9FD)),
    ) {
        val guardianDTO = info.guardianEntity!!.originalGuardianInfo
        BoxTag(guardianDTO)
        Content(info)
    }
}

@Composable
private fun BoxTag(info: GuardianDTO) {
    if (info.isLoginGuardian) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "Login Account",
                style = TextStyle(
                    color = Color(0xFFF5B331),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    fontSize = 10.sp,
                    fontWeight = FontWeight(500)
                ),
                modifier = Modifier
                    .width(83.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(bottomEnd = 8.dp))
                    .background(Color(0xFFF9ECD2)),
            )
        }
    }
}

@Composable
private fun Content(info: GuardianInfo) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val guardianDTO = info.guardianEntity!!.originalGuardianInfo
        Row(
            modifier = Modifier
                .width(DynamicWidth(paddingHorizontal = 31))
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icons(guardianDTO)
            Texts(guardianDTO, info.state)
            Actions(info)
        }
    }

}

@Composable
private fun Icons(info: GuardianDTO) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(28.dp),
        contentAlignment = Alignment.TopStart
    ) {
        if (!TextUtils.isEmpty(info.imageUrl)) {
            AsyncImage(
                model = info.imageUrl,
                contentDescription = "Guardian Image",
                modifier = Modifier
                    .height(28.dp)
                    .width(28.dp)
                    .zIndex(ZIndexConfig.MainIcon.getZIndex())
            )
        } else {
            Row(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5B8EF4)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.portkey_icon),
                    contentDescription = "guardian type icon : " + info.type,
                    modifier = Modifier
                        .size(16.dp)
                        .zIndex(ZIndexConfig.MainIcon.getZIndex()),
                    tint = Color.White
                )
            }
        }
        Row(
            modifier = Modifier
                .zIndex(ZIndexConfig.SubIcon.getZIndex())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(20.dp)
            )
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .width(28.dp)
                    .clip(CircleShape)
                    .border(width = 1.dp, color = Color(0xFFDFE4EC), shape = CircleShape)
                    .background(Color.White),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = getSubIconResource(info.type)),
                    contentDescription = "guardian type icon : " + info.type,
                    modifier = Modifier
                        .size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun Texts(info: GuardianDTO, state: OutsideStateEnum) {
    val guardianName = getGuardianName(info)
    val guardianIdentifier = getGuardianIdentifier(info)
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(start = 8.dp, end = 16.dp)
            .width(162.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state != OutsideStateEnum.Register) guardianName else guardianIdentifier,
            maxLines = if (TextUtils.isEmpty(guardianIdentifier)) 2 else 1,
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            lineHeight = 22.sp,
            color = Color(0xFF162736),
            fontWeight = FontWeight.ExtraBold,
            overflow = TextOverflow.Ellipsis,
        )
        if (!TextUtils.isEmpty(guardianIdentifier) && state != OutsideStateEnum.Register) {
            Text(
                text = guardianIdentifier,
                maxLines = 1,
                fontSize = 12.sp,
                textAlign = TextAlign.Start,
                lineHeight = 18.sp,
                color = Color(0xFF8F949C),
                fontWeight = FontWeight(500),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun getGuardianName(info: GuardianDTO): String {
    return when (info.type) {
        AccountOriginalType.Google.name -> {
            info.firstName ?: info.type
        }

        AccountOriginalType.Apple.name -> {
            // TODO
            info.firstName ?: info.type
        }

        else -> {
            info.name ?: info.type
        }
    }
}

private fun getGuardianIdentifier(info: GuardianDTO): String {
    return when (info.type) {
        AccountOriginalType.Google.name -> {
            info.thirdPartyEmail ?: info.type
        }

        AccountOriginalType.Apple.name -> {
            // TODO
            info.thirdPartyEmail ?: info.type
        }

        else -> {
            info.guardianIdentifier ?: info.type
        }
    }
}

@Composable
private fun Actions(info: GuardianInfo) {
    GLogger.i("HAHA")
    val state = info.state
    if (state != OutsideStateEnum.Normal && state != OutsideStateEnum.LimitReached) {
        when (state) {
            OutsideStateEnum.Expired -> {
                Text(
                    text = "Expired",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF8F949C),
                    fontWeight = FontWeight(500),
                    modifier = Modifier.padding(start = 20.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }

            OutsideStateEnum.Register,
            -> {
                // Do nothing
            }

            else -> {}
        }
    } else {
        val guardianDTO = info.guardianEntity!!.originalGuardianInfo
        val type = convertTypeStringToEnum(guardianDTO.type)
        val isVerify = (type == AccountOriginalType.Apple || type == AccountOriginalType.Google)
        val verified = info.guardianEntity!!.isVerified
        if (state == OutsideStateEnum.LimitReached) {
            if (verified) {
                Distance(width = 45)
                Icon(
                    painter = painterResource(id = R.drawable.verified),
                    contentDescription = "verified icon",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF20CD85)
                )
            }
        } else {
            TinyButton(
                ButtonConfig().apply {
                    text = if (isVerify) "Verify" else "Send"
                    onClick = info.buttonClick
                }
            )
        }
    }
}

private fun convertTypeStringToEnum(str: String?): AccountOriginalType {
    return when (str) {
        AccountOriginalType.Email.name -> AccountOriginalType.Email
        AccountOriginalType.Apple.name -> AccountOriginalType.Apple
        AccountOriginalType.Google.name -> AccountOriginalType.Google
        AccountOriginalType.Phone.name -> AccountOriginalType.Phone
        else -> AccountOriginalType.Email
    }
}

private fun getSubIconResource(str: String?): Int {
    return getSubIconResource(convertTypeStringToEnum(str = str))
}


private fun getSubIconResource(accountOriginalType: AccountOriginalType): Int {
    return when (accountOriginalType) {
        AccountOriginalType.Email -> R.drawable.email_small_icon
        AccountOriginalType.Apple -> R.drawable.apple_small_icon
        AccountOriginalType.Google -> R.drawable.google_small_icon_raw
        AccountOriginalType.Phone -> R.drawable.phone_small_icon
        else -> R.drawable.portkey_small_icon
    }
}

internal open class GuardianInfo {
    var guardianEntity: GuardianBehaviourEntity? = null
    var buttonClick: () -> Unit = {}
    var state: OutsideStateEnum = OutsideStateEnum.Normal
}

internal enum class OutsideStateEnum {
    Normal,
    Expired, // Expired after 1H
    LimitReached,  // Reached the limit, no need to verify now
    Register // Components in register page have no actions
}

@Preview
@Composable
private fun GuardianControllerPreview() {
    GuardianController(info = GuardianInfo().apply {
        guardianEntity = GuardianBehaviourEntity(
            GuardianDTO().apply {
                isLoginGuardian = true
                imageUrl = "https://portkey-did.s3.ap-northeast-1.amazonaws.com/img/Gauss.png"
                name = "gauss"
                guardianIdentifier = "lve@portkey.finance"

            },
            0,
            { },
            AccountOriginalType.Email,
            false
        )
        buttonClick = { }
//        state = OutsideStateEnum.Expired
    })
}