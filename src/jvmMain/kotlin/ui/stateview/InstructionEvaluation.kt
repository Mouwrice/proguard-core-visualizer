package ui.stateview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import data.InstructionEvaluationRecord
import ui.Colors

/**
 * Displays the current instruction that is being evaluated.
 * Also shows if the instruction has been generalized or skipped.
 * And how many times the instruction has been seen.
 */
@Composable
fun InstructionEvaluation(evaluation: InstructionEvaluationRecord) {
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

        // Indicators for generalization and times seen
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (evaluation.skipEvaluation) {
                Icon(
                    Icons.Rounded.FastForward,
                    contentDescription = "Instruction has been skipped",
                    tint = Colors.Orange.value,
                )
                Text("Skipped", color = Colors.Orange.value)
            }

            if (evaluation.isGeneralization) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = "Instruction has been generalized",
                    tint = Colors.Orange.value,
                )
                Text("Generalized", color = Colors.Orange.value)
            }

            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = "Amount of time instruction has been seen",
            )
            Text((evaluation.evaluationCount + 1).toString())
        }
    }
}
