package com.yanatachi.pixelatedcharge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun formatMinutes(minutes: Long): String {
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (remainingMinutes > 0) {
            "${hours}時間${remainingMinutes}分"
        } else {
            "${hours}時間"
        }
    } else {
        "${minutes}分"
    }
}

// 充電数値用のスリムフォント（wdth = 25f）
@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexRoundedSlimLight = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(300),
            FontVariation.Setting("ROND", 100f),
            FontVariation.Setting("wdth", 25f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexRoundedSlimBold = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.Setting("ROND", 100f),
            FontVariation.Setting("wdth", 25f)
        )
    )
)

// 時計や残り時間用の通常のフォントファミリー（wdth = 100f）
@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexRoundedNormalLight = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(300),
            FontVariation.Setting("ROND", 100f),
            FontVariation.Setting("wdth", 100f)
        )
    )
)

@Composable
fun PixelChargingScreen(
    level: Int,
    minTo80: Long,
    minTo100: Long,
    isCharging: Boolean,
    hasAlarm: Boolean
) {
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        while (true) {
            currentTime = LocalTime.now().format(formatter)
            delay(1000L)
        }
    }

    val statusText = when {
        level >= 100 -> "充電完了"
        isCharging -> "充電中"
        else -> "充電していません"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // --- 画面右側：プログレスバーと数値 ---
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.85f)
                .width(IntrinsicSize.Max)
        ) {
            // 1. アラームアイコン ＆ 充電バーをまとめた列（右端に配置）
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // アラームアイコン（バーの真上に配置）
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasAlarm) {
                        Icon(
                            imageVector = Icons.Rounded.Alarm,
                            contentDescription = "Alarm Set",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 充電バー本体
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .width(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val topProgress = ((level - 80).coerceIn(0, 20)) / 20f
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                    ) {
                        if (topProgress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(topProgress)
                                    .align(Alignment.BottomCenter)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    val bottomProgress = (level.coerceIn(0, 80)) / 80f
                    Box(
                        modifier = Modifier
                            .weight(0.75f)
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                    ) {
                        if (bottomProgress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(bottomProgress)
                                    .align(Alignment.BottomCenter)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            // 2. テキスト情報（バーの構造と比率を完全に同期）
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 20.dp),
                horizontalAlignment = Alignment.End
            ) {
                // アイコンとSpacerの高さ分を空けてバーの開始位置に合わせる
                Spacer(modifier = Modifier.height(20.dp))
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 上のバーに対応するエリア (100% -> バーの一番上の真左)
                    Box(
                        modifier = Modifier
                            .weight(0.25f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "100%",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontFamily = GoogleSansFlexRoundedSlimBold,
                                modifier = Modifier.scale(scaleX = 1.0f, scaleY = 1.35f)
                            )
                            if (minTo100 >= 0 && level < 100) {
                                Text(
                                    text = formatMinutes(minTo100),
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    fontFamily = GoogleSansFlexRoundedNormalLight
                                )
                            }
                        }
                    }

                    // 下のバーに対応するエリア (80% -> 区切りの先端＝下のバーの最上部に沿わせる)
                    Box(
                        modifier = Modifier
                            .weight(0.75f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "80%",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontFamily = GoogleSansFlexRoundedSlimBold,
                                modifier = Modifier.scale(scaleX = 1.0f, scaleY = 1.35f)
                            )
                            if (minTo80 >= 0 && level < 80) {
                                Text(
                                    text = formatMinutes(minTo80),
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    fontFamily = GoogleSansFlexRoundedNormalLight
                                )
                            }
                        }
                    }
                }
            }

            // 3. 下部: 現在のパーセンテージ
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$level",
                    color = Color.White,
                    fontSize = 86.sp,
                    fontFamily = GoogleSansFlexRoundedSlimLight,
                    modifier = Modifier.scale(scaleX = 1.0f, scaleY = 1.55f)
                )
                Text(
                    text = "%",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontFamily = GoogleSansFlexRoundedSlimLight,
                    modifier = Modifier
                        .padding(bottom = 12.dp, start = 4.dp)
                        .scale(scaleX = 1.0f, scaleY = 1.35f)
                )
            }
        }

        // --- 画面下部：時刻 & ステータス文言（そのまま） ---
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = GoogleSansFlexRoundedNormalLight
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = statusText,
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = GoogleSansFlexRoundedNormalLight
            )
        }
    }
}