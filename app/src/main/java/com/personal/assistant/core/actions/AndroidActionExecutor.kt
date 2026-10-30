package com.personal.assistant.core.actions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InstalledAppInfo(
    val label: String,
    val packageName: String
)

class AndroidActionExecutor(private val context: Context) {

    // Cache of indexed installed apps for lightning-fast matching
    private var appIndexCache: List<InstalledAppInfo>? = null

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
                message = "Battery: $batteryPct% ($stateDesc)",
                details = "Scale: $scale | Status: $status"
            )
        } catch (e: Exception) {
            ActionResult.Error("Failed to read battery: ${e.localizedMessage}")
        }
    }

    fun getStorageStatus(): ActionResult {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - freeBytes

            val df = DecimalFormat("#.##")
            val totalGB = df.format(totalBytes / (1024.0 * 1024.0 * 1024.0))
            val freeGB = df.format(freeBytes / (1024.0 * 1024.0 * 1024.0))
            val usedGB = df.format(usedBytes / (1024.0 * 1024.0 * 1024.0))

            val freePct = ((freeBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

            ActionResult.Success(
                message = "Storage: $freeGB GB free of $totalGB GB ($freePct% remaining)",
                details = "Used: $usedGB GB | Free: $freeGB GB | Total: $totalGB GB"
            )
        } catch (e: Exception) {
            ActionResult.Error("Unable to calculate storage: ${e.localizedMessage}")
        }
    }

    fun getNetworkStatus(): ActionResult {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)

            if (capabilities == null) {
                return ActionResult.Success(
                    message = "Network: Offline / Disconnected",
                    details = "No active network interface detected."
                )
            }

            val typeStr = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi 📶"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Mobile Data 📡"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Active Network"
            }

            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            val internetStatus = if (hasInternet && validated) "Online (Verified)" else "Connected (Limited/No Internet)"

            ActionResult.Success(
                message = "Network: $typeStr ($internetStatus)",
                details = "Downlink bandwidth: ~${capabilities.linkDownstreamBandwidthKbps / 1000} Mbps"
            )
        } catch (e: Exception) {
            ActionResult.Error("Failed to inspect network: ${e.localizedMessage}")
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
            ActionResult.Success("Opened Internet / Wi-Fi settings panel")
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
        return ActionResult.Success(info, details = "Board: ${Build.BOARD} | Hardware: ${Build.HARDWARE}")
    }

    fun getCurrentTime(): ActionResult {
        val sdf = SimpleDateFormat("hh:mm a, EEEE, MMM dd", Locale.getDefault())
        val formatted = sdf.format(Date())
        return ActionResult.Success("Current time: $formatted")
    }

    fun indexInstalledApps(): List<InstalledAppInfo> {
        if (appIndexCache != null) return appIndexCache!!

        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)

        val list = resolvedApps.map {
            InstalledAppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName
            )
        }.distinctBy { it.packageName }

        appIndexCache = list
        return list
    }

    fun launchAppByName(query: String): ActionResult {
        val apps = indexInstalledApps()
        val cleanQuery = query.trim().lowercase()

        // 1. Exact match
        var matched = apps.firstOrNull { it.label.lowercase() == cleanQuery }

        // 2. Starts with query
        if (matched == null) {
            matched = apps.firstOrNull { it.label.lowercase().startsWith(cleanQuery) }
        }

        // 3. Contains match
        if (matched == null) {
            matched = apps.firstOrNull {
                it.label.lowercase().contains(cleanQuery) || it.packageName.lowercase().contains(cleanQuery)
            }
        }

        return if (matched != null) {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(matched.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionResult.Success("Launched ${matched.label} (${matched.packageName})")
            } else {
                ActionResult.Error("Found ${matched.label} but cannot launch directly.")
            }
        } else {
            ActionResult.Error("App '$query' not found among ${apps.size} installed apps.")
        }
    }

    fun requestClearDataSimulation(): ActionResult {
        return ActionResult.RequiresConfirmation(
            title = "⚠️ SENSITIVE ACTION",
            prompt = "R.E.X. received a request to purge temporary assistant cache and local logs. This action cannot be undone.",
            actionId = "ACTION_PURGE_CACHE",
            onConfirm = {
                appIndexCache = null
                ActionResult.Success("Cache and memory buffers purged successfully.", "Internal index reset complete.")
            }
        )
    }
}
