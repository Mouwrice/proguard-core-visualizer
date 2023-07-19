package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import data.DebuggerViewModel

@Composable
fun StateViewer(viewModel: DebuggerViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Instruction:", style = MaterialTheme.typography.titleMedium)
            Text(
                viewModel.currentInstruction,
                fontFamily = FontFamily.Monospace,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
            )
        }
        Category("Variables", maxHeight = 0.3F) {
            DisplayList(viewModel.variables)
        }

        Category("Stack") {
            DisplayList(viewModel.stack)
        }
    }
}

@Composable
fun <T> DisplayList(list: List<T>) {
    LazyColumn(
        Modifier.fillMaxSize().border(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.medium,
        ).clip(MaterialTheme.shapes.medium),
    ) {
        itemsIndexed(list) { index, it ->
            val color =
                if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            Text("$index: $it", Modifier.fillMaxWidth().background(color).padding(8.dp))
        }
    }
}
