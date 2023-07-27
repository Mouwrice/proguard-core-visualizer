package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import viewmodel.DebuggerViewModel
import viewmodel.Display

@Composable
fun Controls(viewModel: DebuggerViewModel, setShowFilePicker: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.wrapContentSize(align = Alignment.Companion.CenterStart, unbounded = true)) {
        Button(onClick = { setShowFilePicker(true) }) {
            Text("Open file")
        }

        OutlinedButton(enabled = viewModel.hasPrevious, onClick = { viewModel.previous() }) {
            Text("Previous")
        }

        OutlinedButton(enabled = viewModel.hasNext, onClick = { viewModel.next() }) {
            Text("Next")
        }

        OutlinedButton(enabled = viewModel.codeAttribute != null, onClick = {
            viewModel.switchDisplay()
        }) {
            when (viewModel.display) {
                Display.EVALUATIONS -> Text("Show results")
                Display.RESULTS -> Text("Show evaluations")
            }
        }
    }
}
