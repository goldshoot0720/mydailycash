package com.mydailycash.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class MarriageReasonOption(
    val id: String,
    val title: String,
    val summary: String,
    val punchline: String,
    val tags: List<String>,
)

private val marriageReasonOptions = listOf(
    MarriageReasonOption(
        id = "feng-contract",
        title = "Feng says this is a jackpot contract",
        summary = "Feng insists Simin's Daily Cash numbers landed so perfectly that the payout felt like a signed relationship contract.",
        punchline = "He says refusing marriage would disrespect that suspiciously lucky sequence.",
        tags = listOf("Feng", "Simin", "Destiny"),
    ),
    MarriageReasonOption(
        id = "tu-called",
        title = "Tu says the fortune god called his name",
        summary = "Tu watched Huixuan's quick number pick slide right into the winning circle and took it as a direct cosmic instruction.",
        punchline = "If the heavens already tagged you, walking into the wedding hall is just basic manners.",
        tags = listOf("Tu", "Huixuan", "Lucky call"),
    ),
    MarriageReasonOption(
        id = "double-jackpot",
        title = "Two couples, one absurd jackpot theory",
        summary = "Feng with Simin and Tu with Huixuan both turned lottery luck into wedding evidence, and the whole banquet bought the bit.",
        punchline = "The logic is ridiculous, but somehow it still feels sweet.",
        tags = listOf("Double line", "Jackpot", "Banquet lore"),
    ),
    MarriageReasonOption(
        id = "love-math",
        title = "Is love luck or just loud math",
        summary = "A casual number pick became a full proof-of-love presentation the second the draw matched.",
        punchline = "They call it romance; everyone else calls it aggressive mathematics.",
        tags = listOf("Math", "Luck", "Hard sell"),
    ),
    MarriageReasonOption(
        id = "red-string",
        title = "Daily Cash 539 works harder than the matchmaker",
        summary = "Why ask a matchmaker for red string when a lottery broadcast can apparently do the whole job in public.",
        punchline = "This red string was basically tied by the draw machine itself.",
        tags = listOf("Matchmaker", "Red string", "Broadcast"),
    ),
    MarriageReasonOption(
        id = "compiled-heart",
        title = "The prize dropped, and so did his defenses",
        summary = "Feng says the prize money was only half the shock; the real twist was realizing his whole life plan changed mid-broadcast.",
        punchline = "Winning numbers were the trigger, but emotional surrender was the headline.",
        tags = listOf("Prize", "Compiled", "Life reroute"),
    ),
    MarriageReasonOption(
        id = "broadcast-jump",
        title = "The draw jumped, so Tu jumped too",
        summary = "The second Huixuan's numbers showed up, Tu reacted like the host had just announced his future family roster.",
        punchline = "At that point it was no longer a draw show, it was a wedding trailer.",
        tags = listOf("Live reaction", "Tu", "Trailer"),
    ),
    MarriageReasonOption(
        id = "lottery-vows",
        title = "He memorized the issue like wedding vows",
        summary = "Feng can repeat the winning sequence faster than most people can recite a ceremony script.",
        punchline = "Vows can be revised; jackpot issue numbers are eternal.",
        tags = listOf("Vows", "Life numbers", "Feng"),
    ),
    MarriageReasonOption(
        id = "banquet-joke",
        title = "Every banquet table repeated the same joke",
        summary = "The strongest wedding toast was still the line about the numbers coming from her hand.",
        punchline = "The weirdest reason somehow became the event's best brand message.",
        tags = listOf("Banquet", "Friends", "Retold joke"),
    ),
    MarriageReasonOption(
        id = "tomorrow-ticket",
        title = "This story makes bystanders want a ticket",
        summary = "Everyone says the story is nonsense, then quietly checks tomorrow's draw time just in case destiny freelances nearby.",
        punchline = "Ridiculous? Yes. Tempting enough to visit a lottery shop tomorrow? Also yes.",
        tags = listOf("Bystander", "Tomorrow", "Sweet chaos"),
    ),
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun MarriageReasonSection() {
    var expanded by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf("") }
    val selected = marriageReasonOptions.firstOrNull { it.id == selectedId }
    val tags = selected?.tags ?: listOf("Daily Cash 539", "Friends joking", "Sweet nonsense")

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E7)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Absurd Marriage Reason Menu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "Feng and Tu both turned Daily Cash 539 into relationship proof. Pick the wildest excuse and read the story card.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = selected?.title ?: "Pick one absurd reason",
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Reason menu") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    marriageReasonOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.title) },
                            onClick = {
                                selectedId = option.id
                                expanded = false
                            },
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF3)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = selected?.title ?: "No reason selected yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = selected?.summary ?: "Choose one story to see how a lottery draw somehow became a wedding argument.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = selected?.punchline ?: "Luck may explain the prize, but this confidence explains the proposal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9A6700),
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tags.forEach { tag ->
                            Text(
                                text = tag,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFFFE7B3))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7A5200),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}
