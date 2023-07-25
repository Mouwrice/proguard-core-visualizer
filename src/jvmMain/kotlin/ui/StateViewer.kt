package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
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
import viewmodel.DebuggerViewModel
import viewmodel.Display

/**
 * Displays the current instruction that is being evaluated.
 * Also shows if the instruction has been generalized or skipped.
 * And how many times the instruction has been seen.
 */
@Composable
private fun InstructionEvaluation(evaluation: InstructionEvaluationRecord) {
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

/**
 * Display a list in a column with indexes.
 * With a scrollbar on the right side.
 */
@Composable
private fun <T> DisplayList(list: List<T>) {
    val state = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            Modifier.fillMaxSize().border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.medium,
            ).clip(MaterialTheme.shapes.medium),
            state = state,
        ) {
            itemsIndexed(list) { index, it ->
                val color =
                    if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                Text("$index: $it", Modifier.fillMaxWidth().background(color).padding(8.dp))
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(all = 4.dp),
            adapter = rememberScrollbarAdapter(
                scrollState = state,
            ),
            style = defaultScrollbarStyle().copy(
                unhoverColor = MaterialTheme.colorScheme.outline,
                hoverColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

/**
 * Displays the current state of the PartialEvaluator.
 * Showing the current instruction, the stack, the variables and the branches that still need to be evaluated.
 */
@Composable
fun StateViewer(viewModel: DebuggerViewModel?) {
    Column(Modifier.fillMaxSize()) {
        viewModel?.evaluation?.let { evaluation ->
            InstructionEvaluation(evaluation)
        }

        Category("Variables", maxHeight = 0.3F) {
            if (viewModel != null) {
                when (viewModel.display) {
                    Display.RESULTS -> DisplayList(
                        viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions[viewModel.currentInstruction].finalVariablesBefore
                            ?: emptyList(),
                    )

                    Display.EVALUATIONS -> DisplayList(viewModel.evaluation?.variablesBefore ?: emptyList())
                }
            } else {
                DisplayList(emptyList<String>())
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Category("Stack", maxWidth = 0.7F) {
                if (viewModel != null) {
                    when (viewModel.display) {
                        Display.EVALUATIONS -> DisplayList(viewModel.evaluation?.stackBefore?.reversed() ?: emptyList())
                        Display.RESULTS -> DisplayList(
                            viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions[viewModel.currentInstruction].finalStackBefore
                                ?: emptyList(),
                        )
                    }
                } else {
                    DisplayList(emptyList<String>())
                }
            }
            Category("Branches") {
                if (viewModel != null) {
                    when (viewModel.display) {
                        Display.EVALUATIONS -> DisplayList(
                            viewModel.currentBlockEvaluationStack.map { it.startOffset },
                        )

                        Display.RESULTS -> DisplayList(
                            viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions[viewModel.currentInstruction].finalTargetInstructions
                                ?: emptyList(),
                        )
                    }
                } else {
                    DisplayList(emptyList<String>())
                }
            }
        }
    }
}
