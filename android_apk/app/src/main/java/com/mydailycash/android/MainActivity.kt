package com.mydailycash.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
    val prize: Int,
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
    val inputs = remember { mutableStateListOf("", "", "", "", "") }
    var status by remember { mutableStateOf("Enter 5 unique numbers from 1 to 39.") }
    var results by remember { mutableStateOf(analyzeSelection(listOf(6, 8, 20, 22, 32))) }

    val costTotal = results.size * ticketPrice
    val prizeTotal = results.sumOf { it.prize }
    val netTotal = prizeTotal - costTotal

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
                            text = "Pick 5 numbers and compare them against the latest draw history.",
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
                            text = "Selection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )

                        inputs.forEachIndexed { index, value ->
                            OutlinedTextField(
                                value = value,
                                onValueChange = { next ->
                                    inputs[index] = next.filter(Char::isDigit).take(2)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Number ${index + 1}") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    runCatching { parseSelection(inputs) }
                                        .onSuccess { numbers ->
                                            results = analyzeSelection(numbers)
                                            status = "Analyzed ${results.size} draws."
                                        }
                                        .onFailure { error ->
                                            status = error.message ?: "Invalid input."
                                        }
                                },
                            ) {
                                Text("Analyze")
                            }

                            TextButton(
                                onClick = {
                                    for (index in inputs.indices) {
                                        inputs[index] = ""
                                    }
                                    results = emptyList()
                                    status = "Selection cleared."
                                },
                            ) {
                                Text("Clear")
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
                    drawCount = results.size,
                    costTotal = costTotal,
                    prizeTotal = prizeTotal,
                    netTotal = netTotal,
                )
            }

            items(results) { result ->
                ResultCard(result)
            }
        }
    }
}

private fun parseSelection(inputs: List<String>): List<Int> {
    if (inputs.any { it.isBlank() }) {
        error("Please fill in all 5 numbers.")
    }

    val numbers = inputs.map { value ->
        value.toIntOrNull() ?: error("All inputs must be numeric.")
    }

    if (numbers.any { it !in 1..39 }) {
        error("Numbers must be between 1 and 39.")
    }

    if (numbers.toSet().size != numbers.size) {
        error("Numbers must be unique.")
    }

    return numbers
}

private fun analyzeSelection(selection: List<Int>): List<DrawResult> {
    val selected = selection.toSet()
    return draws.map { draw ->
        val hits = draw.numbers.filter { it in selected }
        DrawResult(
            draw = draw,
            hits = hits,
            prize = prizes[hits.size] ?: 0,
        )
    }
}

@Composable
private fun SummaryCard(
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
                text = "Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text("Draws analyzed: $drawCount")
            Text("Ticket cost: ${formatCurrency(costTotal)}")
            Text("Prize total: ${formatCurrency(prizeTotal)}")
            Text(
                text = "Net result: ${formatCurrency(netTotal)}",
                color = if (netTotal >= 0) Color(0xFF1B8C4A) else Color(0xFFC4473A),
            )
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
                text = "Issue ${result.draw.issue}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Date ${result.draw.rocDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                result.draw.numbers.forEach { number ->
                    NumberBall(number = number, hit = number in result.hits)
                }
            }

            Text("Hits: ${result.hits.size}")
            Text("Prize: ${formatCurrency(result.prize)}")
        }
    }
}

@Composable
private fun NumberBall(number: Int, hit: Boolean) {
    val background = if (hit) Color(0xFFC4473A) else MaterialTheme.colorScheme.primary

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
