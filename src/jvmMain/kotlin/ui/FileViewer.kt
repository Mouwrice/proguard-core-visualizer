package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.DebuggerViewModel

@Composable
fun FileViewer(viewModel: DebuggerViewModel) {
    Column(Modifier.fillMaxHeight().fillMaxWidth(0.5f)) {
        Column(Modifier.fillMaxSize()) {
            val file = viewModel.file
            if (file != null) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            val stateTracker = viewModel.stateTracker
            LazyColumn(
                Modifier.fillMaxSize().border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.medium,
                ),
                contentPadding = PaddingValues(8.dp),
            ) {
                stateTracker?.codeAttributes?.forEachIndexed { index, it ->
                    item { Text("${it.clazz}::${it.method}", Modifier.padding(all = 4.dp)) }
                    it.instructions.forEach {
                        item {
                            // Highlight the current instruction
                            val isCurrent =
                                viewModel.currentCodeAttribute == index && viewModel.currentInstructionOffset == it.offset
                            val color =
                                if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            Row(Modifier.height(20.dp).background(color)) {
                                Text(it.offset.toString(), Modifier.padding(horizontal = 8.dp))
                                Divider(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(1.dp),
                                )

                                Text(it.instruction, Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
