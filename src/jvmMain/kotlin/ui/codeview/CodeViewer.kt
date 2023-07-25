package ui.codeview

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.ErrorRecord
import data.InstructionRecord
import kotlinx.coroutines.launch
import ui.Colors
import viewmodel.DebuggerViewModel
import viewmodel.Display

/**
 * Display the exception that occurred during the evaluation of the current instruction.
 */
@Composable
fun ErrorViewer(error: ErrorRecord) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 10.dp)
            .background(Colors.Red.value.copy(alpha = 0.2F)),
    ) {
        Divider(
            color = Colors.Red.value,
            modifier = Modifier.shadow(4.dp),
        )
        Text(
            "error: ${error.message}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

/**
 * Display the current instruction. Highlight it if it is the current one.
 */
@Composable
fun InstructionViewer(instruction: InstructionRecord, maxOffsetLength: Int, color: Color, inCatch: Boolean) {
    val dividerColor = if (inCatch) Colors.Red.value else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        Modifier.fillMaxWidth().background(color).padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pad with whitespaces to align the offsets
        // Compose seems to trim the whitespaces, so we use a special whitespace character
        val offset = instruction.offset.toString().padStart(maxOffsetLength, ' ')
        Text(
            offset,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp, end = 8.dp),
            textAlign = TextAlign.End,
        )
        Divider(
            color = dividerColor,
            modifier = Modifier
                .height(20.dp)
                .width(1.dp),
        )

        Text(
            instruction.instruction,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

/**
 * Display all methods and their instructions from the parsed code attributes.
 */
@Composable
fun CodeViewer(viewModel: DebuggerViewModel) {
    val state = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val aimOffset = mapOf(
        Pair(Display.RESULTS, viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions[viewModel.currentInstruction].offset),
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
            val index = viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions.withIndex()
                .find { it.value.offset == aimOffset[viewModel.display] }
            if (index != null) {
                val visibleItemCount = state.layoutInfo.visibleItemsInfo.size
                state.scrollToItem(if (index.index - visibleItemCount / 2 < 0) 0 else index.index - visibleItemCount / 2)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(state = state) {
            val codeAttribute = viewModel.codeAttributes[viewModel.currentCodeAttribute]
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
                item(key = instruction.offset) {
                    // Highlight if the instruction is the current one
                    var color = MaterialTheme.colorScheme.surface
                    if (isCurrent) {
                        color = when (viewModel.display) {
                            Display.EVALUATIONS -> Colors.Red.value.copy(alpha = 0.5F)
                            Display.RESULTS -> Colors.LightGreen.value.copy(alpha = 0.5F)
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
