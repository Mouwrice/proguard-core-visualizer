package ui.stateview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.DebuggerViewModel
import viewmodel.Display

/**
 * Displays the current state of the PartialEvaluator.
 * Showing the current instruction, the stack, the variables and the branches that still need to be evaluated.
 */
@Composable
fun StateViewer(viewModel: DebuggerViewModel) {
    Column(Modifier.fillMaxSize()) {
        when (viewModel.display) {
            Display.RESULTS -> viewModel.instruction?.let { Instruction(it) }
            Display.EVALUATIONS -> viewModel.evaluation?.let { InstructionEvaluation(it) }
        }

        Category("Variables", maxHeight = 0.3F) {
            when (viewModel.display) {
                Display.RESULTS -> DisplayList(viewModel.instruction?.finalVariablesBefore ?: emptyList())
                Display.EVALUATIONS -> DisplayList(viewModel.evaluation?.variablesBefore ?: emptyList())
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Category("Stack", maxWidth = 0.7F) {
                when (viewModel.display) {
                    Display.EVALUATIONS -> DisplayList(viewModel.evaluation?.stackBefore?.reversed() ?: emptyList())
                    Display.RESULTS -> DisplayList(viewModel.instruction?.finalStackBefore ?: emptyList())
                }
            }
            Category("Branches") {
                when (viewModel.display) {
                    Display.EVALUATIONS -> DisplayList(
                        viewModel.evaluationBlock?.branchEvaluationStack?.map { it.startOffset } ?: emptyList(),
                    )

                    Display.RESULTS -> {
                        viewModel.instruction?.let { instruction ->
                            DisplayList(
                                (instruction.finalTargetInstructions ?: emptyList()) +
                                    (instruction.finalOriginInstructions ?: emptyList()),
                            )
                        }
                    }
                }
            }
        }
    }
}
