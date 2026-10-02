package io.github.tavim26.benchmark.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.tavim26.benchmark.benchmarks.Benchmark
import io.github.tavim26.benchmark.benchmarks.CpuBenchmark
import io.github.tavim26.benchmark.benchmarks.HardwareInfo
import io.github.tavim26.benchmark.benchmarks.InfoSection
import io.github.tavim26.benchmark.benchmarks.MemoryBenchmark
import io.github.tavim26.benchmark.benchmarks.RenderingBenchmark
import io.github.tavim26.benchmark.model.Category
import io.github.tavim26.benchmark.model.TestResult
import io.github.tavim26.benchmark.score.ScoreCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

data class BenchmarkUiState(
    val results: Map<Category, List<TestResult>> = emptyMap(),
    val running: Category? = null,
    val hardwareInfo: List<InfoSection> = emptyList(),
    val error: String? = null
) {
    val categoryScores: Map<Category, Int>
        get() = results.mapNotNull { (category, categoryResults) ->
            ScoreCalculator.categoryScore(categoryResults)?.let { category to it }
        }.toMap()

    /** Available only after every category has been run. */
    val overallScore: Int?
        get() = if (results.keys.containsAll(Category.entries)) {
            ScoreCalculator.overallScore(results.values)
        } else {
            null
        }
}

class BenchmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val benchmarks: Map<Category, Benchmark> = listOf(
        CpuBenchmark(),
        RenderingBenchmark(),
        MemoryBenchmark(application.cacheDir)
    ).associateBy { it.category }

    private val hardwareInfo = HardwareInfo(application)

    private val _uiState = MutableStateFlow(BenchmarkUiState())
    val uiState: StateFlow<BenchmarkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val info = withContext(Dispatchers.IO) { hardwareInfo.collect() }
            _uiState.update { it.copy(hardwareInfo = info) }
        }
    }

    fun run(category: Category) {
        runSequentially(listOf(category))
    }

    fun runAll() {
        runSequentially(Category.entries)
    }

    /**
     * Runs categories one after another, never in parallel, so they do not
     * compete for CPU cores and distort each other's timings.
     */
    private fun runSequentially(categories: List<Category>) {
        if (_uiState.value.running != null) return

        viewModelScope.launch {
            for (category in categories) {
                _uiState.update { it.copy(running = category, error = null) }
                try {
                    val results = withContext(Dispatchers.Default) {
                        benchmarks.getValue(category).run()
                    }
                    _uiState.update { it.copy(results = it.results + (category to results)) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    val reason = e.message ?: e::class.java.simpleName
                    _uiState.update { it.copy(error = "${category.displayName} benchmark failed: $reason") }
                    break
                }
            }
            _uiState.update { it.copy(running = null) }
        }
    }
}