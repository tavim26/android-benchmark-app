package com.example.mobilebenchmarkapp.main

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobilebenchmarkapp.ui.theme.MobileBenchmarkAppTheme
import com.example.mobilebenchmarkapp.benchmarks.*
import java.io.File
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileBenchmarkAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BenchmarkScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BenchmarkScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var cpuResults by remember { mutableStateOf("CPU Benchmark: \n") }
    var gpuResults by remember { mutableStateOf("GPU Benchmark: \n") }
    var memoryResults by remember { mutableStateOf("Memory Benchmark: \n") }
    var hardwareInfoResults by remember { mutableStateOf("Hardware Information: \n") }
    var finalScore by remember { mutableStateOf("") }

    var isCpuTestDone by remember { mutableStateOf(false) }
    var isGpuTestDone by remember { mutableStateOf(false) }
    var isMemoryTestDone by remember { mutableStateOf(false) }
    var isHardwareInfoDone by remember { mutableStateOf(false) }

    val cpuBenchmark = CpuBenchmark(context) { result ->
        cpuResults += "\n$result"
        isCpuTestDone = true
        calculateAndSetScore(
            isCpuTestDone, isGpuTestDone, isMemoryTestDone, isHardwareInfoDone,
            cpuResults, gpuResults, memoryResults, hardwareInfoResults
        ) { score ->
            finalScore = "Score: $score%"
        }
    }

    val gpuBenchmark = GpuBenchmark(context) { result ->
        gpuResults += "\n$result"
        isGpuTestDone = true
        calculateAndSetScore(
            isCpuTestDone, isGpuTestDone, isMemoryTestDone, isHardwareInfoDone,
            cpuResults, gpuResults, memoryResults, hardwareInfoResults
        ) { score ->
            finalScore = "Score: $score%"
        }
    }

    val memoryBenchmark = MemoryBenchmark(context) { result ->
        memoryResults += "\n$result"
        isMemoryTestDone = true
        calculateAndSetScore(
            isCpuTestDone, isGpuTestDone, isMemoryTestDone, isHardwareInfoDone,
            cpuResults, gpuResults, memoryResults, hardwareInfoResults
        ) { score ->
            finalScore = "Score: $score%"
        }
    }

    val hardwareInfoProvider = HardwareInfo(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mobile Benchmark App", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        BenchmarkButtonRow(
            buttonText1 = "Run CPU Benchmark",
            onClick1 = {
                cpuResults = "CPU Benchmark: \n"
                isCpuTestDone = false
                cpuBenchmark.run()
            },
            buttonText2 = "Run GPU Benchmark",
            onClick2 = {
                gpuResults = "GPU Benchmark: \n"
                isGpuTestDone = false
                gpuBenchmark.run()
            }
        )

        BenchmarkButtonRow(
            buttonText1 = "Run Memory Benchmark",
            onClick1 = {
                memoryResults = "Memory Benchmark: \n"
                isMemoryTestDone = false
                memoryBenchmark.run()
            },
            buttonText2 = "Show Hardware Info",
            onClick2 = {
                hardwareInfoResults = "Hardware Information: \n${hardwareInfoProvider.getInfoAsString()}"
                isHardwareInfoDone = true
                calculateAndSetScore(
                    isCpuTestDone, isGpuTestDone, isMemoryTestDone, isHardwareInfoDone,
                    cpuResults, gpuResults, memoryResults, hardwareInfoResults
                ) { score ->
                    finalScore = "Score: $score%"
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            item { Text(cpuResults, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge) }
            item { Text(gpuResults, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge) }
            item { Text(memoryResults, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge) }
            item { Text(hardwareInfoResults, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge) }
        }

        if (finalScore.isNotEmpty()) {
            Text(finalScore, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

fun calculateAndSetScore(
    isCpuTestDone: Boolean,
    isGpuTestDone: Boolean,
    isMemoryTestDone: Boolean,
    isHardwareInfoDone: Boolean,
    cpuResults: String,
    gpuResults: String,
    memoryResults: String,
    hardwareInfoResults: String,
    onScoreCalculated: (Int) -> Unit
) {

    if (isCpuTestDone && isGpuTestDone && isMemoryTestDone && isHardwareInfoDone)
    {

        val cpuScore = extractPerformanceScore(cpuResults, weight = 30)
        val gpuScore = extractPerformanceScore(gpuResults, weight = 25)
        val memoryScore = extractPerformanceScore(memoryResults, weight = 20)
        val hardwareScore = extractPerformanceScore(hardwareInfoResults, weight = 15)


        val totalScore = cpuScore + gpuScore + memoryScore + hardwareScore

        val finalScore = totalScore.coerceAtLeast(50)


        onScoreCalculated(finalScore)
    }
}

fun extractPerformanceScore(resultText: String, weight: Int): Int
{
    val numbers = "\\d+".toRegex().findAll(resultText).map { it.value.toInt() }.toList()

    val average = if (numbers.isNotEmpty()) numbers.average() else 50.0

    return ((average / 100) * weight).toInt().coerceIn(0, weight)
}


@Composable
fun BenchmarkButtonRow(
    buttonText1: String,
    onClick1: () -> Unit,
    buttonText2: String,
    onClick2: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onClick1,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(buttonText1)
        }

        Button(
            onClick = onClick2,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(buttonText2)
        }
    }
}
