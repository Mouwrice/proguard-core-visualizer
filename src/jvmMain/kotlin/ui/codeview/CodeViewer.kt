package ui.codeview

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ui.Colors
import viewmodel.DebuggerViewModel
import viewmodel.Display

/**
 * Display all methods and their instructions from the parsed code attributes.
 */
@Composable
fun CodeViewer(viewModel: DebuggerViewModel) {
    val state = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(state = state) {
            val codeAttribute = viewModel.codeAttributes[viewModel.currentCodeAttribute]
            item {
                MethodHeader(codeAttribute)
            }

            // Get the length of the offset as string of the last instruction of the current code attribute
            val maxOffsetLength = codeAttribute.instructions.last().offset.toString().length

            // Display the instructions of the current code attribute
            codeAttribute.instructions.forEachIndexed { instructionIndex, instruction ->
                val isCurrent = when (viewModel.display) {
                    Display.EVALUATIONS -> viewModel.evaluation?.instructionOffset == instruction.offset
                    Display.RESULTS -> viewModel.currentInstruction == instructionIndex
                }

                var inCatch = false
                // Display a try-catch block, if any
                viewModel.currentExceptionHandler?.let { exceptionHandler ->
                    // Display the start of a try-catch block
                    if (exceptionHandler.catchStartOffset == instruction.offset) {
                        item {
                            Text(
                                "Catch ${exceptionHandler.catchType}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Colors.Red.value.copy(alpha = 0.2F))
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                    // Display the end of a try-catch block
                    if (exceptionHandler.catchEndOffset == instruction.offset) {
                        item {
                            Text(
                                "End catch",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Colors.Red.value.copy(alpha = 0.2F))
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }

                    inCatch =
                        exceptionHandler.catchStartOffset <= instruction.offset && exceptionHandler.catchEndOffset > instruction.offset
                }

                // Display the current instruction
                item {
                    // Highlight if the instruction is the current one
                    var color = MaterialTheme.colorScheme.surface
                    if (isCurrent) {
                        color = when (viewModel.display) {
                            Display.EVALUATIONS -> Colors.Red.value.copy(alpha = 0.5F)
                            Display.RESULTS -> Colors.LightGreen.value.copy(alpha = 0.5F)
                        }
                    } else if (viewModel.display == Display.RESULTS) {
                        // Highlight the instruction if it is a target of the current instruction
                        val currentInstruction =
                            viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions[viewModel.currentInstruction]
                        if (currentInstruction.finalTargetInstructions?.contains(instruction.offset) == true) {
                            color = Colors.Yellow.value.copy(alpha = 0.5F)
                        } else if (currentInstruction.finalOriginInstructions?.contains(instruction.offset) == true) {
                            color = Colors.Blue.value.copy(alpha = 0.5F)
                        }
                    }

                    InstructionViewer(instruction, maxOffsetLength, color, inCatch)
                }

                // There is an error to display at the current instruction
                codeAttribute.error?.let { error ->
                    if (isCurrent && error.instructionOffset == instruction.offset) {
                        item {
                            ErrorViewer(error)
                        }
                    }
                }
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
