package ui.codeview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.CodeAttributeRecord
import data.ErrorRecord
import data.InstructionRecord
import ui.Colors
import viewmodel.DebuggerViewModel

/**
 * Display the name of the current method and its parameters.
 */
@Composable
fun MethodHeader(codeAttribute: CodeAttributeRecord) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 10.dp)
            .background(Colors.LightGreen.value.copy(alpha = 0.2F)),
    ) {
        Text(
            "${codeAttribute.clazz}::${codeAttribute.method}",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Text(
            "parameters: ${codeAttribute.parameters.joinToString(", ")}",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        Divider(
            color = Colors.DarkGreen.value,
            modifier = Modifier.padding(top = 8.dp).shadow(4.dp),
        )
    }
}

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
fun InstructionViewer(instruction: InstructionRecord, isCurrent: Boolean) {
    // Highlight if the instruction is the current one
    val color =
        if (isCurrent) Colors.Red.value.copy(alpha = 0.5F) else MaterialTheme.colorScheme.surface

    Row(
        Modifier.fillMaxWidth().background(color).padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            instruction.offset.toString(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 8.dp).width(32.dp),
            textAlign = TextAlign.End,
        )
        Divider(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    LazyColumn {
        viewModel.codeAttributes.forEachIndexed { index, codeAttribute ->

            item {
                MethodHeader(codeAttribute)
            }

            // Display the instructions of the current code attribute
            codeAttribute.instructions.forEach {
                val isCurrent =
                    viewModel.currentCodeAttribute == index && viewModel.evaluation?.instructionOffset == it.offset

                item {
                    InstructionViewer(it, isCurrent)
                }

                // There is an error to display at the current instruction
                val error = codeAttribute.error
                if (isCurrent && error != null && error.instructionOffset == it.offset) {
                    item {
                        ErrorViewer(error)
                    }
                }
            }
        }
    }
}
