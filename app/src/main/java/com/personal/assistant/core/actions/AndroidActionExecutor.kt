package com.personal.assistant.core.actions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidActionExecutor(private val context: Context) {

    fun getBatteryStatus(): ActionResult {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = (level * 100 / scale.toFloat()).toInt()

            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val stateDesc = if (isCharging) "Charging ⚡" else "Discharging"
            ActionResult.Success(
                message = "Battery level is $batteryPct% ($stateDesc)",
                details = "Scale: $scale, Status Code: $status"
            )
        } catch (e: Exception) {
            ActionResult.Error("Failed to read battery status: ${e.localizedMessage}")
        }
    }

    fun openWifiSettings(): ActionResult {
        return try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            ActionResult.Success("Opened Internet / Wi-Fi settings")
        } catch (e: Exception) {
            ActionResult.Error("Unable to open Wi-Fi settings: ${e.localizedMessage}")
        }
    }

    fun openBluetoothSettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("Opened Bluetooth settings")
        } catch (e: Exception) {
            ActionResult.Error("Unable to open Bluetooth settings: ${e.localizedMessage}")
        }
    }

    fun getDeviceInfo(): ActionResult {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val androidVer = Build.VERSION.RELEASE
        val sdkLevel = Build.VERSION.SDK_INT
        val info = "Device: $manufacturer $model | Android $androidVer (API $sdkLevel)"
        return ActionResult.Success(info)
    }

    fun getCurrentTime(): ActionResult {
        val sdf = SimpleDateFormat("hh:mm a, EEEE, MMM dd", Locale.getDefault())
        val formatted = sdf.format(Date())
        return ActionResult.Success("Current time is $formatted")
    }

    fun launchAppByName(query: String): ActionResult {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)

        val cleanQuery = query.trim().lowercase()

        // Match by app label or package name
        val matched = resolvedApps.firstOrNull { resolveInfo ->
            val label = resolveInfo.loadLabel(pm).toString().lowercase()
            val pkg = resolveInfo.activityInfo.packageName.lowercase()
            label.contains(cleanQuery) || pkg.contains(cleanQuery)
        }

        return if (matched != null) {
            val pkgName = matched.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(pkgName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                val appLabel = matched.loadLabel(pm).toString()
                ActionResult.Success("Launched $appLabel ($pkgName)")
            } else {
                ActionResult.Error("App found ($pkgName) but has no launchable entry point.")
            }
        } else {
            ActionResult.Error("Could not find an installed app matching '$query'.")
        }
    }
}
