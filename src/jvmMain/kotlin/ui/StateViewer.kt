package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
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
import data.DebuggerViewModel

/**
 * Displays the current state of the PartialEvaluator.
 * Showing the current instruction, the stack, the variables and the branches that still need to be evaluated.
 */
@Composable
fun StateViewer(viewModel: DebuggerViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val evaluation = viewModel.evaluation
            if (evaluation != null) {
                Text(
                    evaluation.instruction ?: "",
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
                    if (viewModel.evaluation?.isGeneralization != null) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = "Instruction has been generalized",
                            tint = Colors.Orange.value,
                        )
                        Text("Generalized", color = Colors.Orange.value)
                    }

                    if (viewModel.evaluation?.timesSeen != null) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "Amount of time instruction has been seen",

                        )
                        Text(viewModel.evaluation?.timesSeen.toString())
                    }
                }
            }
        }

        Category("Variables", maxHeight = 0.3F) {
            DisplayList(viewModel.evaluation?.variablesBefore ?: emptyList())
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Category("Stack", maxWidth = 0.7F) {
                DisplayList(viewModel.evaluation?.stackBefore?.reversed() ?: emptyList())
            }
            Category("Branches") {
                DisplayList(viewModel.currentBlockEvaluationStack.map { it.startOffset })
            }
        }
    }
}

@Composable
fun <T> DisplayList(list: List<T>) {
    LazyColumn(
        Modifier.fillMaxSize().border(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.medium,
        ).clip(MaterialTheme.shapes.medium),
    ) {
        itemsIndexed(list) { index, it ->
            val color =
                if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            Text("$index: $it", Modifier.fillMaxWidth().background(color).padding(8.dp))
        }
    }
}
