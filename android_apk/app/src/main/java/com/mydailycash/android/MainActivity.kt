package com.mydailycash.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mydailycash.android.ui.theme.MyDailyCashAndroidTheme
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyDailyCashAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    DailyCashApp()
                }
            }
        }
    }
}

private data class Draw(
    val issue: String,
    val rocDate: String,
    val numbers: List<Int>,
)

private data class DrawResult(
    val draw: Draw,
    val hits: List<Int>,
    val packCovered: List<Int>,
    val matchLabel: String,
    val tagLabel: String,
    val prizeName: String,
    val detail: String,
    val prize: Int,
)

private data class AnalysisSummary(
    val selectionLabel: String,
    val drawCount: Int,
    val wins: Int,
    val costPerDraw: Int,
    val costTotal: Int,
    val prizeTotal: Int,
    val netTotal: Int,
)

private enum class SelectionMode(
    val label: String,
    val visibleCount: Int,
) {
    SINGLE("單注 / 包牌", 5),
    COMBO6("6連碰", 6),
    COMBO7("7連碰", 7),
    COMBO8("8連碰", 8),
    COMBO9("9連碰", 9),
}

private data class Selection(
    val mode: SelectionMode,
    val numbers: List<Int>,
    val packIndex: Int = -1,
    val displayLabel: String,
)

private val prizes = mapOf(
    0 to 0,
    1 to 0,
    2 to 50,
    3 to 300,
    4 to 20_000,
    5 to 8_000_000,
)

private const val ticketPrice = 50
private const val packTicketCount = 35

private val draws = listOf(
    Draw("115000079", "115/03/30", listOf(6, 8, 20, 22, 32)),
    Draw("115000078", "115/03/28", listOf(6, 9, 11, 16, 17)),
    Draw("115000077", "115/03/27", listOf(8, 18, 24, 34, 35)),
    Draw("115000076", "115/03/26", listOf(14, 17, 20, 24, 37)),
    Draw("115000075", "115/03/25", listOf(3, 13, 31, 33, 36)),
    Draw("115000074", "115/03/24", listOf(10, 20, 28, 29, 36)),
    Draw("115000073", "115/03/23", listOf(7, 12, 24, 29, 35)),
    Draw("115000072", "115/03/21", listOf(7, 14, 15, 19, 22)),
    Draw("115000071", "115/03/20", listOf(3, 11, 15, 33, 39)),
    Draw("115000070", "115/03/19", listOf(5, 23, 25, 30, 37)),
    Draw("115000069", "115/03/18", listOf(21, 22, 31, 32, 35)),
    Draw("115000068", "115/03/17", listOf(11, 13, 19, 22, 27)),
    Draw("115000067", "115/03/16", listOf(17, 19, 21, 29, 34)),
    Draw("115000066", "115/03/14", listOf(8, 10, 18, 20, 34)),
    Draw("115000065", "115/03/13", listOf(2, 5, 11, 12, 15)),
    Draw("115000064", "115/03/12", listOf(4, 5, 7, 23, 35)),
    Draw("115000063", "115/03/11", listOf(5, 15, 26, 37, 38)),
    Draw("115000062", "115/03/10", listOf(11, 12, 14, 17, 32)),
    Draw("115000061", "115/03/09", listOf(7, 12, 15, 32, 38)),
    Draw("115000060", "115/03/07", listOf(15, 17, 18, 34, 36)),
    Draw("115000059", "115/03/06", listOf(19, 24, 29, 32, 34)),
    Draw("115000058", "115/03/05", listOf(1, 4, 8, 12, 36)),
    Draw("115000057", "115/03/04", listOf(4, 8, 12, 16, 17)),
    Draw("115000056", "115/03/03", listOf(2, 19, 21, 32, 35)),
    Draw("115000055", "115/03/02", listOf(3, 12, 20, 21, 27)),
    Draw("115000054", "115/03/01", listOf(2, 8, 15, 29, 31)),
    Draw("115000053", "115/02/28", listOf(2, 4, 13, 26, 27)),
    Draw("115000052", "115/02/27", listOf(1, 22, 23, 37, 39)),
    Draw("115000051", "115/02/26", listOf(3, 6, 9, 31, 39)),
    Draw("115000050", "115/02/25", listOf(5, 22, 28, 35, 36)),
    Draw("115000049", "115/02/24", listOf(16, 23, 25, 32, 36)),
    Draw("115000048", "115/02/23", listOf(3, 10, 12, 27, 36)),
    Draw("115000047", "115/02/22", listOf(8, 13, 16, 24, 25)),
    Draw("115000046", "115/02/21", listOf(1, 8, 19, 20, 25)),
    Draw("115000045", "115/02/20", listOf(4, 11, 22, 23, 27)),
    Draw("115000044", "115/02/19", listOf(8, 15, 19, 25, 27)),
    Draw("115000043", "115/02/18", listOf(8, 10, 12, 32, 33)),
    Draw("115000042", "115/02/17", listOf(6, 8, 11, 20, 21)),
    Draw("115000041", "115/02/16", listOf(5, 7, 15, 18, 34)),
    Draw("115000040", "115/02/15", listOf(11, 13, 18, 22, 34)),
    Draw("115000039", "115/02/14", listOf(1, 3, 13, 31, 36)),
    Draw("115000038", "115/02/13", listOf(4, 28, 31, 33, 34)),
    Draw("115000037", "115/02/12", listOf(1, 12, 21, 35, 37)),
    Draw("115000036", "115/02/11", listOf(11, 15, 18, 29, 33)),
    Draw("115000035", "115/02/10", listOf(10, 11, 17, 22, 36)),
    Draw("115000034", "115/02/09", listOf(16, 21, 25, 31, 35)),
    Draw("115000033", "115/02/07", listOf(3, 8, 22, 27, 32)),
    Draw("115000032", "115/02/06", listOf(1, 6, 29, 32, 34)),
    Draw("115000031", "115/02/05", listOf(8, 9, 13, 32, 35)),
    Draw("115000030", "115/02/04", listOf(8, 17, 22, 27, 28)),
    Draw("115000029", "115/02/03", listOf(3, 5, 11, 15, 23)),
    Draw("115000028", "115/02/02", listOf(6, 8, 31, 37, 38)),
    Draw("115000027", "115/01/31", listOf(5, 12, 16, 21, 32)),
    Draw("115000026", "115/01/30", listOf(16, 17, 29, 30, 36)),
    Draw("115000025", "115/01/29", listOf(6, 11, 28, 36, 37)),
    Draw("115000024", "115/01/28", listOf(10, 11, 23, 24, 29)),
    Draw("115000023", "115/01/27", listOf(5, 17, 18, 23, 32)),
    Draw("115000022", "115/01/26", listOf(6, 15, 23, 26, 30)),
    Draw("115000021", "115/01/24", listOf(6, 7, 15, 35, 37)),
    Draw("115000020", "115/01/23", listOf(3, 11, 12, 21, 31)),
    Draw("115000019", "115/01/22", listOf(3, 6, 11, 30, 34)),
    Draw("115000018", "115/01/21", listOf(4, 15, 23, 27, 38)),
    Draw("115000017", "115/01/20", listOf(16, 19, 23, 25, 34)),
    Draw("115000016", "115/01/19", listOf(12, 16, 23, 24, 29)),
    Draw("115000015", "115/01/17", listOf(2, 10, 11, 24, 37)),
    Draw("115000014", "115/01/16", listOf(18, 19, 22, 27, 29)),
    Draw("115000013", "115/01/15", listOf(1, 2, 3, 19, 36)),
    Draw("115000012", "115/01/14", listOf(1, 2, 16, 33, 35)),
    Draw("115000011", "115/01/13", listOf(6, 16, 17, 19, 31)),
    Draw("115000010", "115/01/12", listOf(3, 13, 18, 24, 30)),
    Draw("115000009", "115/01/10", listOf(11, 25, 26, 34, 38)),
    Draw("115000008", "115/01/09", listOf(1, 12, 14, 22, 34)),
    Draw("115000007", "115/01/08", listOf(3, 8, 10, 21, 30)),
    Draw("115000006", "115/01/07", listOf(5, 10, 14, 15, 28)),
    Draw("115000005", "115/01/06", listOf(1, 2, 6, 11, 33)),
    Draw("115000004", "115/01/05", listOf(10, 16, 18, 34, 39)),
    Draw("115000003", "115/01/03", listOf(22, 23, 31, 32, 38)),
    Draw("115000002", "115/01/02", listOf(17, 18, 25, 36, 39)),
    Draw("115000001", "115/01/01", listOf(15, 16, 18, 29, 36)),
)

@Composable
private fun DailyCashApp() {
    val inputs = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var mode by remember { mutableStateOf(SelectionMode.SINGLE) }
    var packIndex by remember { mutableStateOf(-1) }
    var status by remember { mutableStateOf("請輸入 5 個 1 到 39 的不重複號碼，可切換包牌或 6 到 9 連碰，並比對今年 1 月 1 日起的資料。") }
    var results by remember { mutableStateOf<List<DrawResult>>(emptyList()) }
    var summary by remember {
        mutableStateOf(
            AnalysisSummary(
                selectionLabel = "尚未分析",
                drawCount = 0,
                wins = 0,
                costPerDraw = ticketPrice,
                costTotal = 0,
                prizeTotal = 0,
                netTotal = 0,
            ),
        )
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "MyDailyCash Android APK",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "支援 4 個號碼加包牌、6連碰、7連碰、8連碰、9連碰，並比對今年 1 月 1 日起的開獎資料。",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "選號",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )

                        ModeSelector(
                            currentMode = mode,
                            onModeChange = { nextMode ->
                                mode = nextMode
                                if (nextMode != SelectionMode.SINGLE) {
                                    packIndex = -1
                                }
                                results = emptyList()
                                summary = summary.copy(
                                    selectionLabel = "尚未分析",
                                    drawCount = 0,
                                    wins = 0,
                                    costTotal = 0,
                                    prizeTotal = 0,
                                    netTotal = 0,
                                    costPerDraw = costPerDrawFor(nextMode, false),
                                )
                                status = if (nextMode == SelectionMode.SINGLE) {
                                    "請輸入 5 個號碼，或用 4 個固定號碼加包牌。"
                                } else {
                                    "請輸入 ${nextMode.visibleCount} 個不重複號碼進行 ${nextMode.visibleCount} 連碰分析。"
                                }
                            },
                        )

                        Text(
                            text = if (mode == SelectionMode.SINGLE) {
                                "請輸入 5 個不重複號碼，或啟用其中一欄包牌，改用 4 個固定號碼加 35 注包牌試算。"
                            } else {
                                "請輸入 ${mode.visibleCount} 個不重複號碼，系統會自動計算 ${mode.visibleCount} 連碰的組合成本與獎金。"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )

                        inputs.take(mode.visibleCount).forEachIndexed { index, value ->
                            OutlinedTextField(
                                value = value,
                                onValueChange = { next ->
                                    if (!(mode == SelectionMode.SINGLE && packIndex == index)) {
                                        inputs[index] = next.filter(Char::isDigit).take(2)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(
                                        if (mode == SelectionMode.SINGLE && packIndex == index) {
                                            "包牌"
                                        } else {
                                            "號碼 ${index + 1}"
                                        },
                                    )
                                },
                                singleLine = true,
                                enabled = !(mode == SelectionMode.SINGLE && packIndex == index),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )

                            if (mode == SelectionMode.SINGLE) {
                                TextButton(
                                    onClick = {
                                        packIndex = if (packIndex == index) {
                                            -1
                                        } else {
                                            inputs[index] = ""
                                            index
                                        }
                                        results = emptyList()
                                        summary = summary.copy(
                                            selectionLabel = "尚未分析",
                                            drawCount = 0,
                                            wins = 0,
                                            costPerDraw = costPerDrawFor(mode, packIndex != -1),
                                            costTotal = 0,
                                            prizeTotal = 0,
                                            netTotal = 0,
                                        )
                                        status = if (packIndex == -1) {
                                            "已取消包牌。"
                                        } else {
                                            "已啟用第 ${index + 1} 欄包牌，請輸入另外 4 個固定號碼。"
                                        }
                                    },
                                ) {
                                    Text(if (packIndex == index) "取消包牌" else "包牌")
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    runCatching { parseSelection(inputs, mode, packIndex) }
                                        .onSuccess { selection ->
                                            val analyzedResults = analyzeSelection(selection)
                                            results = analyzedResults
                                            summary = buildSummary(selection, analyzedResults)
                                            status = "已分析 ${analyzedResults.size} 期資料，玩法為 ${selection.displayLabel}。"
                                        }
                                        .onFailure { error ->
                                            results = emptyList()
                                            summary = summary.copy(
                                                selectionLabel = "尚未分析",
                                                drawCount = 0,
                                                wins = 0,
                                                costTotal = 0,
                                                prizeTotal = 0,
                                                netTotal = 0,
                                            )
                                            status = error.message ?: "輸入內容有誤。"
                                        }
                                },
                            ) {
                                Text("開始分析")
                            }

                            TextButton(
                                onClick = {
                                    inputs.indices.forEach { inputs[it] = "" }
                                    packIndex = -1
                                    results = emptyList()
                                    summary = AnalysisSummary(
                                        selectionLabel = "尚未分析",
                                        drawCount = 0,
                                        wins = 0,
                                        costPerDraw = ticketPrice,
                                        costTotal = 0,
                                        prizeTotal = 0,
                                        netTotal = 0,
                                    )
                                    status = "已清除選號。"
                                },
                            ) {
                                Text("清除")
                            }
                        }

                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }

            item {
                SummaryCard(summary = summary)
            }

            item {
                MarriageReasonSection()
            }

            if (results.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(results) { result ->
                    ResultCard(result)
                }
            }
        }
    }
}

private fun parseSelection(inputs: List<String>, mode: SelectionMode, packIndex: Int): Selection {
    val rawValues = inputs.take(mode.visibleCount)

    if (mode != SelectionMode.SINGLE) {
        if (rawValues.any { it.isBlank() }) {
            error("請先完整輸入 ${mode.visibleCount} 個號碼。")
        }

        val numbers = rawValues.map { value ->
            value.toIntOrNull() ?: error("號碼必須是 1 到 39 的整數。")
        }

        if (numbers.any { it !in 1..39 }) {
            error("號碼必須是 1 到 39 的整數。")
        }

        if (numbers.toSet().size != mode.visibleCount) {
            error("${mode.visibleCount} 個號碼不可重複。")
        }

        val sorted = numbers.sorted()
        return Selection(
            mode = mode,
            numbers = sorted,
            displayLabel = "${mode.visibleCount}連碰 ${sorted.joinToString(" ") { it.pad() }}",
        )
    }

    if (packIndex == -1) {
        if (rawValues.any { it.isBlank() }) {
            error("請先完整輸入 5 個號碼。")
        }

        val numbers = rawValues.map { value ->
            value.toIntOrNull() ?: error("號碼必須是 1 到 39 的整數。")
        }

        if (numbers.any { it !in 1..39 }) {
            error("號碼必須是 1 到 39 的整數。")
        }

        if (numbers.toSet().size != 5) {
            error("5 個號碼不可重複。")
        }

        val sorted = numbers.sorted()
        return Selection(
            mode = mode,
            numbers = sorted,
            displayLabel = sorted.joinToString(" ") { it.pad() },
        )
    }

    val fixedNumbers = rawValues.mapIndexedNotNull { index, value ->
        if (index == packIndex) null else value
    }

    if (fixedNumbers.any { it.isBlank() } || fixedNumbers.size != 4) {
        error("啟用包牌時，請輸入另外 4 個固定號碼。")
    }

    val numbers = fixedNumbers.map { value ->
        value.toIntOrNull() ?: error("固定號碼必須是 1 到 39 的整數。")
    }

    if (numbers.any { it !in 1..39 }) {
        error("固定號碼必須是 1 到 39 的整數。")
    }

    if (numbers.toSet().size != 4) {
        error("包牌模式下，4 個固定號碼不可重複。")
    }

    val sorted = numbers.sorted()
    return Selection(
        mode = mode,
        numbers = sorted,
        packIndex = packIndex,
        displayLabel = "${sorted.joinToString(" ") { it.pad() }} + 包牌",
    )
}

private fun analyzeSelection(selection: Selection): List<DrawResult> {
    val selected = selection.numbers.toSet()
    return draws.map { draw ->
        when {
            selection.packIndex != -1 -> {
                val fixedHits = draw.numbers.filter { it in selected }
                val packCovered = draw.numbers.filter { it !in selected }
                val fixedMatchCount = fixedHits.size
                val upgradedMatchCount = fixedMatchCount + 1
                val upgradedTickets = packCovered.size
                val baseTickets = packTicketCount - upgradedTickets
                val prize = (upgradedTickets * (prizes[upgradedMatchCount] ?: 0)) +
                    (baseTickets * (prizes[fixedMatchCount] ?: 0))

                DrawResult(
                    draw = draw,
                    hits = fixedHits,
                    packCovered = packCovered,
                    matchLabel = "固定中 $fixedMatchCount 個",
                    tagLabel = if (prize > 0) "包牌收益" else "未中獎",
                    prizeName = if (prize > 0) "包牌" else "未中獎",
                    detail = if (prize > 0) {
                        "包牌命中：${packCovered.joinToString(" ") { it.pad() }}"
                    } else {
                        "本期未中獎"
                    },
                    prize = prize,
                )
            }

            selection.mode != SelectionMode.SINGLE -> {
                val hits = draw.numbers.filter { it in selected }
                val hitCount = hits.size
                var prize = 0

                for (matchCount in 2..minOf(5, hitCount)) {
                    val ticketCount = combination(hitCount, matchCount) *
                        combination(selection.mode.visibleCount - hitCount, 5 - matchCount)
                    prize += ticketCount * (prizes[matchCount] ?: 0)
                }

                DrawResult(
                    draw = draw,
                    hits = hits,
                    packCovered = emptyList(),
                    matchLabel = "選中 $hitCount 個",
                    tagLabel = if (prize > 0) "${selection.mode.visibleCount}連碰" else "未中獎",
                    prizeName = if (prize > 0) "${selection.mode.visibleCount}連碰" else "未中獎",
                    detail = if (prize > 0) {
                        "命中號碼：${hits.joinToString(" ") { it.pad() }}"
                    } else {
                        "本期未中獎"
                    },
                    prize = prize,
                )
            }

            else -> {
                val hits = draw.numbers.filter { it in selected }
                val matchCount = hits.size
                DrawResult(
                    draw = draw,
                    hits = hits,
                    packCovered = emptyList(),
                    matchLabel = "對中 $matchCount 個",
                    tagLabel = prizeName(matchCount),
                    prizeName = prizeName(matchCount),
                    detail = if (hits.isNotEmpty()) {
                        "命中號碼：${hits.joinToString(" ") { it.pad() }}"
                    } else {
                        "本期未中獎"
                    },
                    prize = prizes[matchCount] ?: 0,
                )
            }
        }
    }
}

private fun buildSummary(selection: Selection, results: List<DrawResult>): AnalysisSummary {
    val costPerDraw = costPerDrawFor(selection.mode, selection.packIndex != -1)
    val prizeTotal = results.sumOf { it.prize }
    val costTotal = results.size * costPerDraw
    val wins = results.count { it.prize > 0 }
    return AnalysisSummary(
        selectionLabel = selection.displayLabel,
        drawCount = results.size,
        wins = wins,
        costPerDraw = costPerDraw,
        costTotal = costTotal,
        prizeTotal = prizeTotal,
        netTotal = prizeTotal - costTotal,
    )
}

@Composable
private fun SummaryCard(summary: AnalysisSummary) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "分析摘要",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text("玩法：${summary.selectionLabel}")
            Text("分析期數：${summary.drawCount}")
            Text("中獎期數：${summary.wins}")
            Text("每期成本：${formatCurrency(summary.costPerDraw)}")
            Text("總成本：${formatCurrency(summary.costTotal)}")
            Text("總獎金：${formatCurrency(summary.prizeTotal)}")
            Text(
                text = "淨損益：${formatCurrency(summary.netTotal)}",
                color = if (summary.netTotal >= 0) Color(0xFF1B8C4A) else Color(0xFFC4473A),
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "尚未分析",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "請先選擇玩法，再輸入對應數量的號碼後按下開始分析。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModeSelector(
    currentMode: SelectionMode,
    onModeChange: (SelectionMode) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SelectionMode.entries.forEach { mode ->
            val selected = mode == currentMode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(999.dp),
                    )
                    .clickable { onModeChange(mode) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = mode.label,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(result: DrawResult) {
    val isJackpot = result.prizeName == "頭獎"
    val transition = rememberInfiniteTransition(label = "jackpot")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isJackpot) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "jackpot-scale",
    )
    val glowAlpha by transition.animateFloat(
        initialValue = if (isJackpot) 0.35f else 0f,
        targetValue = if (isJackpot) 0.85f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "jackpot-glow",
    )
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isJackpot) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "jackpot-sweep",
    )

    val cardColors = if (isJackpot) {
        CardDefaults.cardColors(containerColor = Color(0xFFFFF4D6))
    } else {
        CardDefaults.cardColors()
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = cardColors,
        modifier = Modifier
            .graphicsLayer {
                if (isJackpot) {
                    shadowElevation = 26.dp.toPx()
                    spotShadowColor = Color(0xFFFFB300)
                    ambientShadowColor = Color(0xFFFFD54F)
                }
            }
            .scale(scale),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isJackpot) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFF7DB),
                                Color(0xFFFFE082).copy(alpha = 0.92f),
                                Color(0xFFFFFDE7),
                            ),
                            start = Offset.Zero,
                            end = Offset(1000f, 600f),
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.Transparent),
                        )
                    },
                )
                .border(
                    width = if (isJackpot) 2.dp else 1.dp,
                    brush = if (isJackpot) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFC107).copy(alpha = glowAlpha),
                                Color(0xFFFFECB3),
                                Color(0xFFFFA000).copy(alpha = glowAlpha),
                            ),
                            start = Offset(1200f * sweep, 0f),
                            end = Offset(1200f * (1f - sweep), 700f),
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            ),
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isJackpot) {
                JackpotBanner(glowAlpha = glowAlpha)
            }
            Text(
                text = "期號 ${result.draw.issue}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "日期 ${result.draw.rocDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                result.draw.numbers.forEach { number ->
                    NumberBall(
                        number = number,
                        hit = number in result.hits,
                        packHit = number in result.packCovered,
                    )
                }
            }

            Text(result.matchLabel)
            Text("標記：${result.tagLabel}")
            Text(result.detail)
            Text("獎項：${result.prizeName}")
            Text(
                text = "獎金：${formatCurrency(result.prize)}",
                color = if (isJackpot) Color(0xFF9A6700) else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isJackpot) FontWeight.ExtraBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun JackpotBanner(glowAlpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFC107).copy(alpha = 0.95f),
                        Color(0xFFFFE082),
                        Color(0xFFFFF8E1),
                    ),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        JackpotSparkles(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.9f),
            glowAlpha = glowAlpha,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "頭獎命中",
                color = Color(0xFF5D3A00),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "金色特效已啟動，這期就是全場焦點。",
                color = Color(0xFF7A5200),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun JackpotSparkles(modifier: Modifier = Modifier, glowAlpha: Float) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val sparkles = listOf(
            Triple(0.12f, 0.28f, 10f),
            Triple(0.22f, 0.7f, 7f),
            Triple(0.48f, 0.18f, 8f),
            Triple(0.76f, 0.32f, 9f),
            Triple(0.87f, 0.62f, 11f),
        )
        sparkles.forEachIndexed { index, (xFactor, yFactor, radius) ->
            val pulse = 0.55f + glowAlpha * (0.35f + index * 0.05f)
            val center = Offset(size.width * xFactor, size.height * yFactor)
            drawCircle(
                color = Color.White.copy(alpha = pulse),
                radius = radius,
                center = center,
            )
            repeat(4) { arm ->
                val angle = arm * (PI.toFloat() / 2f)
                val delta = Offset(cos(angle) * radius * 1.8f, sin(angle) * radius * 1.8f)
                drawCircle(
                    color = Color(0xFFFFF3C4).copy(alpha = glowAlpha * 0.9f),
                    radius = radius * 0.28f,
                    center = center + delta,
                )
            }
        }
    }
}

@Composable
private fun NumberBall(number: Int, hit: Boolean, packHit: Boolean) {
    val background = when {
        hit -> Color(0xFFC4473A)
        packHit -> Color(0xFFE49B32)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString().padStart(2, '0'),
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun formatCurrency(amount: Int): String {
    val absolute = abs(amount)
    val base = "NT$" + absolute.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (amount < 0) "-$base" else base
}

private fun prizeName(matchCount: Int): String {
    return when (matchCount) {
        5 -> "頭獎"
        4 -> "貳獎"
        3 -> "參獎"
        2 -> "肆獎"
        else -> "未中獎"
    }
}

private fun costPerDrawFor(mode: SelectionMode, packed: Boolean): Int {
    return when {
        packed -> ticketPrice * packTicketCount
        mode == SelectionMode.SINGLE -> ticketPrice
        else -> combination(mode.visibleCount, 5) * ticketPrice
    }
}

private fun combination(n: Int, k: Int): Int {
    if (k < 0 || k > n) return 0
    if (k == 0 || k == n) return 1
    val size = minOf(k, n - k)
    var result = 1L
    for (index in 1..size) {
        result = result * (n - size + index) / index
    }
    return result.toInt()
}

private fun Int.pad(): String = toString().padStart(2, '0')
