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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ui.Colors
import viewmodel.CodeAttributeViewModel
import viewmodel.Display
import java.lang.Integer.max

/**
 * Display all methods and their instructions from the parsed code attributes.
 */
@Composable
fun CodeViewer(viewModel: CodeAttributeViewModel) {
    val state = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val aimOffset = mapOf(
        Pair(Display.RESULTS, viewModel.instruction?.offset ?: 0),
        Pair(Display.EVALUATIONS, viewModel.evaluation?.instructionOffset ?: 0),
    )

    var prevOffset by remember {
        mutableStateOf(aimOffset)
    }

    if (prevOffset[viewModel.display] != aimOffset[viewModel.display] &&
        !state.layoutInfo.visibleItemsInfo.any { it.key == aimOffset[viewModel.display] }
    ) {
        prevOffset = HashMap(aimOffset)

        coroutineScope.launch {
            // Animate scroll to the first item
            val index = viewModel.codeAttribute.instructions.withIndex()
                .find { it.value.offset == aimOffset[viewModel.display] }
            if (index != null) {
                val visibleItemCount = state.layoutInfo.visibleItemsInfo.size
                state.animateScrollToItem(max(0, index.index - visibleItemCount / 2))
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(state = state) {
            // Get the length of the offset as string of the last instruction of the current code attribute
            val maxOffsetLength = viewModel.codeAttribute.instructions.last().offset.toString().length

            // Display the instructions of the current code attribute
            viewModel.codeAttribute.instructions.forEachIndexed { instructionIndex, instruction ->
                val isCurrent = when (viewModel.display) {
                    Display.EVALUATIONS -> viewModel.evaluation?.instructionOffset == instruction.offset
                    Display.RESULTS -> viewModel.instructionIndex == instructionIndex
                }

                var inCatch = false
                // Display a try-catch block, if any
                viewModel.evaluationBlock?.exceptionHandlerInfo?.let { exceptionHandler ->
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
                item(key = instruction.offset) {
                    // Highlight if the instruction is the current one
                    var color = MaterialTheme.colorScheme.surface
                    if (isCurrent) {
                        color = when (viewModel.display) {
                            Display.EVALUATIONS -> Colors.Red.value.copy(alpha = 0.5F)
                            Display.RESULTS -> Colors.LightGreen.value.copy(alpha = 0.5F)
                        }
                    } else if (viewModel.display == Display.RESULTS) {
                        // Highlight the instruction if it is a target of the current instruction
                        val currentInstruction = viewModel.codeAttribute.instructions[viewModel.instructionIndex]
                        if (currentInstruction.finalTargetInstructions?.contains(instruction.offset) == true) {
                            color = Colors.Yellow.value.copy(alpha = 0.5F)
                        } else if (currentInstruction.finalOriginInstructions?.contains(instruction.offset) == true) {
                            color = Colors.Blue.value.copy(alpha = 0.5F)
                        }
                    }

                    InstructionViewer(viewModel, instruction, maxOffsetLength, color, inCatch)
                }

                // There is an error to display at the current instruction
                viewModel.codeAttribute.error?.let { error ->
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
