package com.yanatachi.pixelatedcharge

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.service.dreams.DreamService
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class PixelChargingDreamService : DreamService(), SavedStateRegistryOwner, LifecycleOwner, ViewModelStoreOwner {

    private val batteryLevel = mutableIntStateOf(0)
    private val timeTo80Min = mutableLongStateOf(-1L)
    private val timeTo100Min = mutableLongStateOf(-1L)
    private val isChargingState = mutableStateOf(false)
    private val hasAlarmState = mutableStateOf(false) // 【追加】アラーム有無の状態

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true

        val composeView = ComposeView(this).apply {
            setContent {
                PixelChargingScreen(
                    level = batteryLevel.intValue,
                    minTo80 = timeTo80Min.longValue,
                    minTo100 = timeTo100Min.longValue,
                    isCharging = isChargingState.value,
                    hasAlarm = hasAlarmState.value // 【追加】アラーム状態を渡す
                )
            }
        }

        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        setContentView(composeView)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || context == null) return

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val pct = (level * 100 / scale.toFloat()).toInt()
            batteryLevel.intValue = pct

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isChargingState.value = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL)

            // 【追加】アラームが設定されているか確認
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                hasAlarmState.value = (alarmManager.nextAlarmClock != null)
            } catch (_: Exception) {
                hasAlarmState.value = false
            }

            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val remainingMs = bm.computeChargeTimeRemaining()

            if (remainingMs > 0) {
                val totalMin100 = remainingMs / 1000 / 60
                timeTo100Min.longValue = totalMin100

                if (pct < 80) {
                    val ratio = (80f - pct) / (100f - pct)
                    timeTo80Min.longValue = (totalMin100 * ratio).toLong()
                } else {
                    timeTo80Min.longValue = 0L
                }
            } else {
                timeTo100Min.longValue = -1L
                timeTo80Min.longValue = -1L
            }
        }
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store
}