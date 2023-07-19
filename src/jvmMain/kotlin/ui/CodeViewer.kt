package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.DebuggerViewModel

/**
 * Panel that displays all instructions and a pointer to the current instruction.
 * Also shows the parameters of a method and any errors that occur.
 * @param viewModel the [DebuggerViewModel] that contains the state of the debugger.
 */
@Composable
fun CodeViewer(viewModel: DebuggerViewModel) {
    Column(Modifier.fillMaxWidth(0.5f)) {
        Column(
            Modifier.fillMaxSize().border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.medium,
            ).clip(MaterialTheme.shapes.medium),
        ) {
            val file = viewModel.file
            if (file != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(Color.LightGray)
                        .padding(horizontal = 8.dp),
                ) {
                    Text(
                        file.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Close file")
                    }
                }
            }

            val stateTracker = viewModel.stateTracker
            LazyColumn {
                stateTracker?.codeAttributes?.forEachIndexed { index, codeAttribute ->
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(bottom = 10.dp).background(Color.Green.copy(alpha = 0.2F)),
                        ) {
                            Text(
                                "${codeAttribute.clazz}::${codeAttribute.method}",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                            Text(
                                "parameters: ${codeAttribute.parameters.joinToString(", ")}",
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                            Divider(color = Color.Green, modifier = Modifier.padding(top = 8.dp).shadow(4.dp))
                        }
                    }
                    codeAttribute.instructions.forEach {
                        val isCurrent =
                            viewModel.currentCodeAttribute == index && viewModel.currentInstructionOffset == it.offset
                        item {
                            // Highlight the current instruction
                            val color =
                                if (isCurrent) Colors.Red.color.copy(alpha = 0.5F) else MaterialTheme.colorScheme.surface
                            Row(
                                Modifier.background(color).padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    it.offset.toString(),
                                    Modifier.padding(horizontal = 8.dp).width(16.dp),
                                    textAlign = TextAlign.End,
                                )
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(1.dp),
                                )

                                Text(it.instruction, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(start = 16.dp))
                            }
                        }

                        // There is an error to display at the current instruction
                        if (isCurrent && viewModel.stateTracker?.error != null) {
                            val error = viewModel.stateTracker?.error
                            if (error != null && error.instructionOffset == it.offset && error.clazz == codeAttribute.clazz && error.method == codeAttribute.method) {
                                item {
                                    Column(
                                        Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                            .background(Colors.Red.color.copy(alpha = 0.2F)),
                                    ) {
                                        Divider(
                                            color = Colors.Red.color,
                                            modifier = Modifier.shadow(4.dp),
                                        )
                                        Text(
                                            "error: ${error.message}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
