package com.example.mobilebenchmarkapp.benchmarks

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.app.ActivityManager
import android.os.Environment
import android.os.StatFs
import kotlin.math.pow
import kotlin.math.sqrt
import java.io.File
import java.io.FileWriter

class HardwareInfo(private val context: Context) {

    private var totalRamMb: Long = 0
    private var availableRamMb: Long = 0
    private var totalStorageMb: Long = 0
    private var availableStorageMb: Long = 0
    private var batteryCapacity: Int = 0
    private var healthStatus: String = "Unknown"
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var screenDensity: Int = 0
    private var screenSizeInches: Double = 0.0

    private val resultsFile = File(context.filesDir, "hardware_results.txt")

    init {
        calculateHardwareInfo()
    }

    private fun calculateHardwareInfo() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        totalRamMb = memoryInfo.totalMem / (1024 * 1024)
        availableRamMb = memoryInfo.availMem / (1024 * 1024)

        val statFs = StatFs(Environment.getDataDirectory().path)
        totalStorageMb = (statFs.blockCountLong * statFs.blockSizeLong) / (1024 * 1024)
        availableStorageMb = (statFs.availableBlocksLong * statFs.blockSizeLong) / (1024 * 1024)

        batteryCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        healthStatus = when (batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }

        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        screenDensity = displayMetrics.densityDpi
        screenSizeInches = sqrt(
            (screenWidth / displayMetrics.xdpi.toDouble()).pow(2.0) +
                    (screenHeight / displayMetrics.ydpi.toDouble()).pow(2.0)
        )
    }

    fun getInfoAsString(): String {
        val result = """
            Complete Hardware Overview:
            - Device Model: ${Build.MODEL} (${Build.MANUFACTURER})
            - Brand: ${Build.BRAND}
            - Product: ${Build.PRODUCT}
            - Hardware: ${Build.HARDWARE}
            - Device: ${Build.DEVICE}
            - Android Version: ${Build.VERSION.RELEASE} (API Level ${Build.VERSION.SDK_INT})
            - CPU Architecture: ${Build.SUPPORTED_ABIS.joinToString(", ")}
            - Processor Model: ${Build.HARDWARE}
            - Processor Frequency: ${getProcessorFrequency()} MHz
            - Number of Cores: ${getProcessorCores()}

            Memory:
            - Total RAM: ${totalRamMb} MB (${totalRamMb / 1024} GB)
            - Available RAM: ${availableRamMb} MB (${availableRamMb / 1024} GB)

            Storage:
            - Total Internal Storage: ${totalStorageMb} MB (${totalStorageMb / 1024} GB)
            - Available Internal Storage: ${availableStorageMb} MB (${availableStorageMb / 1024} GB)

            Battery:
            - Capacity: ${batteryCapacity}%
            - Health Status: $healthStatus

            Display:
            - Resolution: ${screenWidth} x ${screenHeight} pixels
            - Density: ${screenDensity} dpi
            - Approximate Screen Size: ${"%.2f".format(screenSizeInches)} inches
        """.trimIndent()

        logResult(result)
        return result
    }

    private fun getProcessorFrequency(): Int {
        return try {
            val reader = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq").bufferedReader()
            val freq = reader.readLine()
            reader.close()
            freq.toInt() / 1000
        } catch (e: Exception) {
            0
        }
    }

    private fun getProcessorCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    private fun logResult(result: String) {
        FileWriter(resultsFile, true).use { writer ->
            writer.append(result).append("\n")
        }
    }
}
