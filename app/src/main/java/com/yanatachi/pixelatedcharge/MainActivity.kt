package com.yanatachi.pixelatedcharge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("PixelatedChargePrefs", Context.MODE_PRIVATE)

        setContent {
            val context = LocalContext.current
            val isDark = isSystemInDarkTheme()

            // ★端末の現在のテーマカラー（ダイナミック・カラー）を動的に取得する
            // Android 12以降なら壁紙に応じた色が、対応していない端末やバージョンの場合は通常の配色にフォールバックします
            val dynamicColorScheme = remember(isDark) {
                try {
                    if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } catch (_: Exception) {
                    if (isDark) darkColorScheme() else lightColorScheme()
                }
            }

            MaterialTheme(
                colorScheme = dynamicColorScheme
            ) {
                var selectedFont by remember { mutableStateOf(prefs.getString("selected_font", "GoogleSansFlex") ?: "GoogleSansFlex") }
                var burnInInterval by remember { mutableIntStateOf(prefs.getInt("burn_in_interval", 5)) }

                var showFontDialog by remember { mutableStateOf(false) }
                var showBurnInDialog by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // 1段目: タイトルカード
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "PixelatedChargeScreenSaver",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // 2段目: 2つの正方形に近いタイル
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // フォントの設定タイル
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(130.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        tonalElevation = 2.dp,
                                        onClick = { showFontDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "フォントの設定",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = when(selectedFont) {
                                                    "GoogleSansFlex" -> "Google Sans"
                                                    "Ndot" -> "Ndot"
                                                    "Ntype" -> "Ntype"
                                                    else -> "Google Sans"
                                                },
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    // 焼き付き防止の設定タイル
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(130.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        tonalElevation = 2.dp,
                                        onClick = { showBurnInDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "焼き付き防止",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${burnInInterval}分ごとに変更",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                // 3段目: スクリーンセーバーの設定画面を開くボタンカード
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp,
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
                                        startActivity(intent)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "スクリーンセーバーの設定画面を開く",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // 下部クレジット
                            Text(
                                text = "By Yanatachi",
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // --- フォント選択ダイアログ ---
                        if (showFontDialog) {
                            AlertDialog(
                                onDismissRequest = { showFontDialog = false },
                                title = { Text("フォントを選択") },
                                text = {
                                    Column {
                                        listOf("GoogleSansFlex" to "Google Sans Flex", "Ndot" to "Ndot", "Ntype" to "Ntype").forEach { (id, name) ->
                                            TextButton(
                                                onClick = {
                                                    selectedFont = id
                                                    prefs.edit().putString("selected_font", id).apply()
                                                    showFontDialog = false
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = name, fontSize = 16.sp)
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showFontDialog = false }) {
                                        Text("閉じる")
                                    }
                                }
                            )
                        }

                        // --- 焼き付き防止（時間調整）ダイアログ ---
                        if (showBurnInDialog) {
                            AlertDialog(
                                onDismissRequest = { showBurnInDialog = false },
                                title = { Text("焼き付き防止の更新間隔") },
                                text = {
                                    Column {
                                        listOf(1, 3, 5, 10, 15).forEach { min ->
                                            TextButton(
                                                onClick = {
                                                    burnInInterval = min
                                                    prefs.edit().putInt("burn_in_interval", min).apply()
                                                    showBurnInDialog = false
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = "${min}分", fontSize = 16.sp)
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showBurnInDialog = false }) {
                                        Text("閉じる")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}