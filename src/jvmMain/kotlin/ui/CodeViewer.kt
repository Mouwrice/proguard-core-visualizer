package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        file.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    // An IconButton is currently fixed to 48.dp, so we need to make our own.
                    // https://github.com/androidx/androidx/blob/androidx-main/compose/material/material/src/commonMain/kotlin/androidx/compose/material/IconButton.kt
                    Box(
                        modifier = Modifier.size(16.dp)
                            .clickable(
                                onClick = { viewModel.reset() },
                                role = Role.Button,
                                interactionSource = MutableInteractionSource(),
                                indication = rememberRipple(bounded = false, radius = 12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close file")
                    }
                }
            }

            val stateTracker = viewModel.stateTracker
            LazyColumn {
                stateTracker?.codeAttributes?.forEachIndexed { index, codeAttribute ->
                    item {
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
                    codeAttribute.instructions.forEach {
                        val isCurrent =
                            viewModel.currentCodeAttribute == index && viewModel.currentInstructionOffset == it.offset
                        item {
                            // Highlight the current instruction
                            val color =
                                if (isCurrent) Colors.Red.value.copy(alpha = 0.5F) else MaterialTheme.colorScheme.surface
                            Row(
                                Modifier.fillMaxWidth().background(color).padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    it.offset.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp).width(16.dp),
                                    textAlign = TextAlign.End,
                                )
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(1.dp),
                                )

                                Text(
                                    it.instruction,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            }
                        }

                        // There is an error to display at the current instruction
                        val error = codeAttribute.error
                        if (isCurrent && error != null && error.instructionOffset == it.offset) {
                            item {
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
                        }
                    }
                }
            }
        }
    }
}
