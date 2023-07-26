package ui.codeview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.InstructionRecord
import ui.Colors
import viewmodel.DebuggerViewModel
import viewmodel.Display

/**
 * Display the current instruction. Highlight it if it is the current one.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstructionViewer(viewModel: DebuggerViewModel, instruction: InstructionRecord, maxOffsetLength: Int, color: Color, inCatch: Boolean) {
    val dividerColor = if (inCatch) Colors.Red.value else MaterialTheme.colorScheme.onSurfaceVariant
    var expandedSelection by remember {
        mutableStateOf(false)
    }

    Row(
        Modifier.fillMaxWidth().background(color).padding(2.dp).onClick {
            expandedSelection = true
        },
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

        DropdownMenu(expandedSelection, { expandedSelection = false }) {
            DropdownMenuItem({ Text("Result") }, onClick = {
                expandedSelection = false
                val instructionIndex = viewModel.codeAttributes[viewModel.currentCodeAttribute].instructions.indexOf(instruction)
                viewModel.display = Display.RESULTS
                viewModel.currentInstruction = instructionIndex
            })

            // viewModel.codeAttributes[viewModel.currentCodeAttribute].blockEvaluations.withIndex().flatMap { blockIndex ->
            //     blockIndex.value.evaluations.filter { evaluationIndex ->
            //         evaluationIndex.instructionOffset == instruction.offset
            //     }.withIndex().map {evaluationIndex ->
            //         Pair(Pair(blockIndex.index, evaluationIndex.index), evaluationIndex.value)
            //     }
            // }.forEach() {
            //     val evalCount = it.second.evaluationCount
            //     DropdownMenuItem({ Text(evalCount.toString()) }, onClick = {
            //         viewModel.evaluation = it.first.second
            //     })
            // }
        }
    }
}
