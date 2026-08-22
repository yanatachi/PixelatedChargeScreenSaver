package com.yanatachi.pixelatedcharge

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // アプリを開いた瞬間にOSのスクリーンセーバー設定へ飛ばす
        val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
        startActivity(intent)

        // 自アプリの画面は開かずにそのまま閉じる
        finish()
    }
}