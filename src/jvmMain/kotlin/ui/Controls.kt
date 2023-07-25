package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import viewmodel.DebuggerViewModel
import viewmodel.Display

@Composable
fun Controls(viewModel: DebuggerViewModel?, setShowFilePicker: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { setShowFilePicker(true) }) {
            Text("Open json file")
        }

        OutlinedButton(enabled = viewModel?.hasPrevious == true, onClick = { viewModel?.previous() }) {
            Text("Previous")
        }

        OutlinedButton(enabled = viewModel?.hasNext == true, onClick = { viewModel?.next() }) {
            Text("Next")
        }

        OutlinedButton(enabled = viewModel != null, onClick = {
            when (viewModel?.display ?: Display.EVALUATIONS) {
                Display.EVALUATIONS -> viewModel?.display = Display.RESULTS
                Display.RESULTS -> viewModel?.display = Display.EVALUATIONS
            }
        }) {
            when (viewModel?.display ?: Display.EVALUATIONS) {
                Display.EVALUATIONS -> Text("Show results")
                Display.RESULTS -> Text("Show evaluations")
            }
        }
    }
}
