package io.github.tavim26.benchmark.benchmarks

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

/** A titled group of label/value pairs shown in the hardware overview. */
data class InfoSection(
    val title: String,
    val items: List<Pair<String, String>>
)

/** Collects static device information. Not part of the score. */
class HardwareInfo(private val context: Context) {

    fun collect(): List<InfoSection> = listOf(
        deviceSection(),
        processorSection(),
        memorySection(),
        storageSection(),
        batterySection(),
        displaySection()
    )

    private fun deviceSection() = InfoSection(
        "Device",
        listOf(
            "Model" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "Brand" to Build.BRAND,
            "Product" to Build.PRODUCT,
            "Android version" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    )

    private fun processorSection(): InfoSection {
        val maxFrequency = readMaxCpuFrequencyMhz()
        return InfoSection(
            "Processor",
            listOf(
                "Hardware" to Build.HARDWARE,
                "Supported ABIs" to Build.SUPPORTED_ABIS.joinToString(", "),
                "Cores" to Runtime.getRuntime().availableProcessors().toString(),
                "Max frequency" to (maxFrequency?.let { "$it MHz" } ?: "Unavailable")
            )
        )
    }

    private fun memorySection(): InfoSection {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return InfoSection(
            "Memory",
            listOf(
                "Total RAM" to formatGigabytes(memoryInfo.totalMem),
                "Available RAM" to formatGigabytes(memoryInfo.availMem)
            )
        )
    }

    private fun storageSection(): InfoSection {
        val stat = StatFs(Environment.getDataDirectory().path)
        return InfoSection(
            "Internal storage",
            listOf(
                "Total" to formatGigabytes(stat.totalBytes),
                "Available" to formatGigabytes(stat.availableBytes)
            )
        )
    }

    private fun batterySection(): InfoSection {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val health = when (batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            else -> "Unknown"
        }

        return InfoSection(
            "Battery",
            listOf(
                "Level" to (if (level in 0..100) "$level%" else "Unavailable"),
                "Health" to health
            )
        )
    }

    private fun displaySection(): InfoSection {
        val metrics = context.resources.displayMetrics
        val widthInches = (metrics.widthPixels / metrics.xdpi).toDouble()
        val heightInches = (metrics.heightPixels / metrics.ydpi).toDouble()
        val diagonalInches = sqrt(widthInches.pow(2) + heightInches.pow(2))

        return InfoSection(
            "Display",
            listOf(
                "Resolution" to "${metrics.widthPixels} × ${metrics.heightPixels} px",
                "Density" to "${metrics.densityDpi} dpi",
                "Approximate size" to String.format(Locale.US, "%.1f in", diagonalInches)
            )
        )
    }

    /** Highest max frequency across all cores (on big.LITTLE chips, cpu0 is usually a small core). */
    private fun readMaxCpuFrequencyMhz(): Int? =
        (0 until Runtime.getRuntime().availableProcessors())
            .mapNotNull { core ->
                runCatching {
                    File("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq")
                        .readText()
                        .trim()
                        .toInt() / 1000
                }.getOrNull()
            }
            .maxOrNull()

    private fun formatGigabytes(bytes: Long): String =
        String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}