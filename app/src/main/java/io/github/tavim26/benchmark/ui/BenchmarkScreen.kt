package io.github.tavim26.benchmark.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.tavim26.benchmark.benchmarks.InfoSection
import io.github.tavim26.benchmark.model.Category
import io.github.tavim26.benchmark.model.TestResult
import java.util.Locale

@Composable
fun BenchmarkScreen(
    modifier: Modifier = Modifier,
    viewModel: BenchmarkViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Header(state = state, onRunAll = viewModel::runAll)
        }

        state.error?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(Category.entries) { category ->
            CategoryCard(
                category = category,
                results = state.results[category].orEmpty(),
                score = state.categoryScores[category],
                isRunning = state.running == category,
                canRun = state.running == null,
                onRun = { viewModel.run(category) }
            )
        }

        item {
            Text(
                text = "Hardware",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(state.hardwareInfo) { section ->
            HardwareCard(section)
        }
    }
}

@Composable
private fun Header(state: BenchmarkUiState, onRunAll: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mobile Benchmark", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        val overallScore = state.overallScore
        if (overallScore != null) {
            Text("$overallScore", style = MaterialTheme.typography.displayMedium)
            Text("Overall score", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(
                text = "Run all benchmarks to get an overall score",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = "1000 points = estimated reference device. Higher is better.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))
        Button(onClick = onRunAll, enabled = state.running == null) {
            Text("Run all benchmarks")
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    results: List<TestResult>,
    score: Int?,
    isRunning: Boolean,
    canRun: Boolean,
    onRun: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.displayName, style = MaterialTheme.typography.titleMedium)
                    if (score != null) {
                        Text("Score: $score", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    OutlinedButton(onClick = onRun, enabled = canRun) {
                        Text(if (results.isEmpty()) "Run" else "Run again")
                    }
                }
            }

            results.forEach { result ->
                Spacer(Modifier.height(8.dp))
                ResultRow(result)
            }
        }
    }
}

@Composable
private fun ResultRow(result: TestResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(result.test.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = result.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(formatMillis(result.timeMs), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun HardwareCard(section: InfoSection) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(section.title, style = MaterialTheme.typography.titleMedium)
            section.items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun formatMillis(timeMs: Double): String =
    String.format(Locale.US, "%.1f ms", timeMs)