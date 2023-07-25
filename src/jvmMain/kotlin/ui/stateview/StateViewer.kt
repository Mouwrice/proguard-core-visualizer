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
fun StateViewer(viewModel: DebuggerViewModel?) {
    Column(Modifier.fillMaxSize()) {
        viewModel?.let { viewModel ->
            when (viewModel.display) {
                Display.RESULTS -> Instruction(viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions[viewModel.currentInstruction])
                Display.EVALUATIONS -> viewModel.evaluation?.let { InstructionEvaluation(it) }
            }
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

                        Display.RESULTS -> {
                            val instruction =
                                viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions[viewModel.currentInstruction]
                            DisplayList(
                                instruction.finalTargetInstructions?.plus(instruction.finalOriginInstructions ?: emptyList())
                                    ?: emptyList(),
                            )
                        }
                    }
                } else {
                    DisplayList(emptyList<String>())
                }
            }
        }
    }
}
