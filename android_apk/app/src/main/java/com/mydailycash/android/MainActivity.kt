package com.mydailycash.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mydailycash.android.ui.theme.MyDailyCashAndroidTheme
import kotlin.math.abs

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
)

@Composable
private fun DailyCashApp() {
    val inputs = remember { mutableStateListOf("", "", "", "", "", "", "", "", "") }
    var mode by remember { mutableStateOf(SelectionMode.SINGLE) }
    var packIndex by remember { mutableStateOf(-1) }
    var status by remember { mutableStateOf("請輸入 5 個 1 到 39 的不重複號碼，可切換包牌或 6 到 9 連碰。") }
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
                            text = "支援單注、包牌、6連碰、7連碰、8連碰、9連碰，比對最近開獎資料並計算損益。",
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
                                    "請輸入 5 個 1 到 39 的不重複號碼，可切換包牌。"
                                } else {
                                    "請輸入 ${nextMode.visibleCount} 個 1 到 39 的不重複號碼。"
                                }
                            },
                        )

                        Text(
                            text = if (mode == SelectionMode.SINGLE) {
                                "請輸入 5 個不重複號碼，或啟用一欄包牌，改用 4 固定號碼加 35 注配號收益試算。"
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
                                    val label = if (mode == SelectionMode.SINGLE && packIndex == index) {
                                        "包牌"
                                    } else {
                                        "號碼 ${index + 1}"
                                    }
                                    Text(label)
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
                                            "已取消包牌，回到單注模式。"
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
                                    for (index in inputs.indices) {
                                        inputs[index] = ""
                                    }
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
                SummaryCard(
                    summary = summary,
                    drawCount = summary.drawCount,
                    costTotal = summary.costTotal,
                    prizeTotal = summary.prizeTotal,
                    netTotal = summary.netTotal,
                )
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
        if (index == packIndex) {
            null
        } else {
            value
        }
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
private fun SummaryCard(
    summary: AnalysisSummary,
    drawCount: Int,
    costTotal: Int,
    prizeTotal: Int,
    netTotal: Int,
) {
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
            Text("分析期數：$drawCount")
            Text("中獎期數：${summary.wins}")
            Text("每期成本：${formatCurrency(summary.costPerDraw)}")
            Text("總成本：${formatCurrency(costTotal)}")
            Text("總獎金：${formatCurrency(prizeTotal)}")
            Text(
                text = "淨損益：${formatCurrency(netTotal)}",
                color = if (netTotal >= 0) Color(0xFF1B8C4A) else Color(0xFFC4473A),
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
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        },
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
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
            Text("獎金：${formatCurrency(result.prize)}")
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
