package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import viewmodel.DebuggerViewModel

@Composable
fun Controls(viewModel: DebuggerViewModel?, setShowFilePicker: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { setShowFilePicker(true) }) {
            Text("Open json file")
        }

        OutlinedButton(enabled = viewModel == null || viewModel.hasPrevious, onClick = { viewModel?.previousEvaluation() }) {
            Text("Previous")
        }

        OutlinedButton(enabled = viewModel == null || viewModel.hasNext, onClick = { viewModel?.nextEvaluation() }) {
            Text("Next")
        }
    }
}
