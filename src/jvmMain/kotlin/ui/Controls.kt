package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import viewModel.DebuggerViewModel

@Composable
fun Controls(viewModel: DebuggerViewModel, setShowFilePicker: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { setShowFilePicker(true) }) {
            Text("Open json file")
        }

        OutlinedButton(onClick = { viewModel.previousEvaluation() }) {
            Text("Previous")
        }

        OutlinedButton(onClick = { viewModel.nextEvaluation() }) {
            Text("Next")
        }
    }
}

@Composable
@Preview
fun ControlsPreview() {
    Controls(DebuggerViewModel()) {}
}
