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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

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

// --- フォントの定義 ---
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

val NdotFont = FontFamily(Font(resId = R.font.ndot))
val NtypeFont = FontFamily(Font(resId = R.font.ntype))


@Composable
fun PixelChargingScreen(
    level: Int,
    minTo80: Long,
    minTo100: Long,
    isCharging: Boolean,
    hasAlarm: Boolean
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("") }

    var offsetX by remember { mutableIntStateOf(0) }
    var offsetY by remember { mutableIntStateOf(0) }

    val prefs = remember { context.getSharedPreferences("PixelatedChargePrefs", android.content.Context.MODE_PRIVATE) }
    var selectedFont by remember {
        mutableStateOf(prefs.getString("selected_font", "GoogleSansFlex") ?: "GoogleSansFlex")
    }

    // 時刻の更新
    LaunchedEffect(Unit) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        while (true) {
            currentTime = LocalTime.now().format(formatter)
            delay(1000L)
        }
    }

    // 焼き付き防止 & 設定の定期同期
    LaunchedEffect(Unit) {
        while (true) {
            selectedFont = prefs.getString("selected_font", "GoogleSansFlex") ?: "GoogleSansFlex"

            val intervalMinutes = prefs.getInt("burn_in_interval", 5)
            val intervalMillis = intervalMinutes * 60 * 1000L

            delay(intervalMillis)

            offsetX = Random.nextInt(-15, 16)
            offsetY = Random.nextInt(-15, 16)
        }
    }

    val activeFontFamily = when (selectedFont) {
        "Ndot" -> NdotFont
        "Ntype" -> NtypeFont
        else -> GoogleSansFlexRoundedSlimLight
    }

    val activeFontFamilyBold = when (selectedFont) {
        "Ndot" -> NdotFont
        "Ntype" -> NtypeFont
        else -> GoogleSansFlexRoundedSlimBold
    }

    val activeFontFamilyNormal = when (selectedFont) {
        "Ndot" -> NdotFont
        "Ntype" -> NtypeFont
        else -> GoogleSansFlexRoundedNormalLight
    }

    val isGoogleSans = selectedFont == "GoogleSansFlex"

    // 下の現在の％はそのまま
    val bottomTextEndPadding = 20.dp

    // 上の80%/100%ブロックの追加の右方向へのオフセット（Ndot/Ntypeのときだけもっと右へ寄せる：+12dp分）
    val topExtraOffsetX = if (isGoogleSans) 0.dp else 50.dp

    val statusText = when {
        level >= 100 -> "充電完了"
        isCharging -> "充電中"
        else -> "充電していません"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .offset(x = offsetX.dp, y = offsetY.dp)
            .padding(24.dp)
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.85f)
                .width(IntrinsicSize.Max)
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

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


            // 【上部】100% と 80% のブロック（offsetでさらに右側に押し出す）
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .offset(x = topExtraOffsetX), // ここでさらに右へ！
                horizontalAlignment = Alignment.End
            ) {

                Spacer(modifier = Modifier.height(20.dp))
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

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
                                fontFamily = activeFontFamilyBold,
                                modifier = if (isGoogleSans) Modifier.scale(scaleX = 1.0f, scaleY = 1.35f) else Modifier
                            )
                            if (minTo100 >= 0 && level < 100) {
                                Text(
                                    text = formatMinutes(minTo100),
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    fontFamily = activeFontFamilyNormal
                                )
                            }
                        }
                    }


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
                                fontFamily = activeFontFamilyBold,
                                modifier = if (isGoogleSans) Modifier.scale(scaleX = 1.0f, scaleY = 1.35f) else Modifier
                            )
                            if (minTo80 >= 0 && level < 80) {
                                Text(
                                    text = formatMinutes(minTo80),
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    fontFamily = activeFontFamilyNormal
                                )
                            }
                        }
                    }
                }
            }


            // 【下部】現在の % 表示（ここはそのまま）
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = bottomTextEndPadding),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$level",
                    color = Color.White,
                    fontSize = 86.sp,
                    fontFamily = activeFontFamily,
                    modifier = if (isGoogleSans) Modifier.scale(scaleX = 1.0f, scaleY = 1.55f) else Modifier
                )
                Text(
                    text = "%",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontFamily = activeFontFamily,
                    modifier = if (isGoogleSans) {
                        Modifier.padding(bottom = 12.dp, start = 4.dp).scale(scaleX = 1.0f, scaleY = 1.35f)
                    } else {
                        Modifier.padding(bottom = 12.dp, start = 4.dp)
                    }
                )
            }
        }


        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = activeFontFamilyNormal
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = statusText,
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = activeFontFamilyNormal
            )
        }
    }
}