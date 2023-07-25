package ui.stateview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import data.InstructionRecord
import ui.Colors

/**
 * Displays the current instruction record.
 */
@Composable
fun Instruction(evaluation: InstructionRecord) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            evaluation.instruction,
            fontFamily = FontFamily.Monospace,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(Colors.LightGreen.value.copy(alpha = 0.2F))
                .border(
                    border = BorderStroke(1.dp, Colors.DarkGreen.value),
                    shape = MaterialTheme.shapes.medium,
                ).padding(8.dp),
        )
    }
}
